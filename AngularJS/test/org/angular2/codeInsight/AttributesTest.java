// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.daemon.impl.analysis.XmlUnboundNsPrefixInspection;
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.dialects.JSLanguageLevel;
import com.intellij.lang.javascript.psi.JSField;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.lang.javascript.psi.resolve.JSSimpleTypeProcessor;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
import com.intellij.lang.javascript.psi.types.JSNamedType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.ThrowableRunnable;
import com.intellij.xml.util.XmlInvalidIdInspection;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class AttributesTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "attributes";
  }

  private static String getDirectiveDefinitionText(PsiElement resolve) {
    return resolve.getParent().getText();
  }

  @NotNull
  private PsiElement resolveReference(@NotNull String signature) {
    int offsetBySignature = AngularTestUtil.findOffsetBySignature(signature, myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    return resolve;
  }

  private void assertUnresolvedReference(@NotNull String signature) {
    int offsetBySignature = AngularTestUtil.findOffsetBySignature(signature, myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    assertNull(ref.resolve());
  }

  public void testCustomAttributesResolve20TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom2.after.html", "custom.ts");
      PsiElement resolve = resolveReference("my-cus<caret>tomer");
      assertEquals("custom.ts", resolve.getContainingFile().getName());
      assertEquals("Directive({\n" +
                   "    selector: '[my-customer]',\n" +
                   "    properties: {\n" +
                   "        'id':'dependency'\n" +
                   "    },\n" +
                   "    templateUrl: '',\n" +
                   "    styleUrls: [''],\n" +
                   "})", getDirectiveDefinitionText(resolve));
    });
  }

  public void testCustomAttributesResolve20JavaScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom2.after.html", "custom2.js");
      PsiElement resolve = resolveReference("my-cus<caret>tomer");
      assertEquals("custom2.js", resolve.getContainingFile().getName());
      assertEquals("new angular.DirectiveAnnotation({\n" +
                   "    selector: '[my-customer]'\n" +
                   "  })", getDirectiveDefinitionText(resolve));
    });
  }

  public void testSrcBinding20() {
    myFixture.configureByFiles("srcBinding.html", "package.json");
    myFixture.enableInspections(RequiredAttributesInspection.class);
    myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
    myFixture.checkHighlighting();
  }

  public void testEventHandlers2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("event.html", "package.json");
      myFixture.enableInspections(RequiredAttributesInspection.class);
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.checkHighlighting();
    });
  }

  public void testEventHandlersStandardCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("event.html", "package.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "(mouseover)");
    });
  }

  public void testBindingStandardCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("bindingHtml.html", "package.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "[value]");
    });
  }

  public void testTemplateReferenceDeclarations2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("variable.html", "custom.ts", "package.json");
      myFixture.enableInspections(RequiredAttributesInspection.class);
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.checkHighlighting();
    });
  }

  public void testTemplateReferenceCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.html", "package.json");
      myFixture.completeBasic();
      myFixture.checkResultByFile("binding.after.html");
    });
  }

  public void testVariableCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("ngTemplate.html", "package.json");
      myFixture.completeBasic();
      myFixture.checkResultByFile("ngTemplate.after.html");
    });
  }

  public void testTemplateReferenceCompletion2Inline() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.ts", "package.json");
      myFixture.completeBasic();
      myFixture.checkResultByFile("binding.after.ts");
    });
  }

  public void testTemplateReferenceSmart2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.type.html", "package.json");
      final PsiFile file = myFixture.getFile();
      final int offset = AngularTestUtil.findOffsetBySignature("user<caret>name,", file);
      final JSReferenceExpression ref = PsiTreeUtil.getParentOfType(file.findElementAt(offset), JSReferenceExpression.class);
      final JSSimpleTypeProcessor processor = new JSSimpleTypeProcessor();
      JSTypeEvaluator.evaluateTypes(ref, file, processor);
      final JSType type = processor.getType();
      assertInstanceOf(type, JSNamedType.class);
      assertEquals("HTMLInputElement", type.getTypeText());
    });
  }

  public void testTemplateReferenceResolve2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.after.html", "package.json");
      PsiElement resolve = resolveReference("user<caret>name");
      assertEquals("binding.after.html", resolve.getContainingFile().getName());
      assertEquals("#username", resolve.getContainingFile().findElementAt(resolve.getParent().getTextOffset()).getText());
    });
  }

  public void testVariableResolve2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("ngTemplate.after.html", "package.json");
      PsiElement resolve = resolveReference("let-my_<caret>user");
      assertEquals("ngTemplate.after.html", resolve.getContainingFile().getName());
      assertEquals("let-my_user", resolve.getContainingFile().findElementAt(resolve.getParent().getTextOffset()).getText());

      PsiElement resolve2 = resolveReference("{{my_<caret>user");
      assertEquals("ngTemplate.after.html", resolve2.getContainingFile().getName());
      assertEquals("let-my_user", resolve2.getContainingFile().findElementAt(resolve2.getParent().getTextOffset()).getText());
    });
  }

  public void testTemplateReferenceResolve2Inline() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("binding.after.ts", "package.json");
      PsiElement resolve = resolveReference("in<caret>put_el.");
      assertEquals("binding.after.ts", resolve.getContainingFile().getName());
      assertEquals("#input_el", resolve.getContainingFile().findElementAt(resolve.getParent().getTextOffset()).getText());
    });
  }

  public void testBindingCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding.html", "package.json", "object.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("object_binding.after.html");
    });
  }

  public void testBindingResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding.after.html", "package.json", "object.ts");
      PsiElement resolve = resolveReference("[mod<caret>el]");
      assertEquals("object.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testBindingCompletionViaBase2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding_via_base.html", "package.json", "inheritor.ts", "object.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("object_binding_via_base.after.html");
    });
  }

  public void testBindingResolveViaBase2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding_via_base.after.html", "package.json", "inheritor.ts", "object.ts");
      PsiElement resolve = resolveReference("[mod<caret>el]");
      assertEquals("object.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testBindingOverride2CompletionTypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding.html", "package.json", "objectOverride.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("object_binding.after.html");
    });
  }

  public void testBindingOverrideResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_binding.after.html", "package.json", "objectOverride.ts");
      PsiElement resolve = resolveReference("[mod<caret>el]");
      assertEquals("objectOverride.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }
  public void testBindingAttributeCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_binding.html", "package.json", "object.ts");
      myFixture.completeBasic();
      myFixture.type('\n');
      myFixture.checkResultByFile("attribute_binding.after.html");
    });
  }

  public void testBindingAttributeResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_binding.after.html", "package.json", "object.ts");
      PsiElement resolve = resolveReference("[mod<caret>el]");
      assertEquals("object.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testOneTimeBindingAttributeCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_one_time_binding.html", "package.json", "object.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "model", "oneTime", "oneTimeList");
    });
  }

  public void testOneTimeBindingAttributeResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_one_time_binding.after.html", "package.json", "object.ts");
      PsiElement resolve = resolveReference("one<caret>Time");
      assertEquals("object.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testOneTimeBindingAttributeResolve2JavaScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("compiled_binding.after.html", "package.json", "button.metadata.json", "button.d.ts", "color.d.ts");
      PsiElement resolve = resolveReference("col<caret>or");
      assertEquals("color.d.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, TypeScriptPropertySignature.class);
      assertEquals("color: ThemePalette", resolve.getText());
    });
  }

  public void testOneTimeBindingAttributeCompletion2JavaScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("compiled_binding.html", "package.json", "button.metadata.json", "button.d.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(),  "disableRipple", "color");
    });
  }

  public void testOneTimeBindingAttributeCompletion2JavaScriptPrimeButton() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("primeButton.html", "package.json", "primeButton.metadata.json", "primeButton.d.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(),  "icon", "iconPos", "label");
    });
  }

  public void testOneTimeBindingAttributeCompletion2ES6() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("compiled_binding.html", "package.json", "button.metadata.json", "button.d.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(),  "disableRipple", "color");
      assertDoesntContain(myFixture.getLookupElementStrings(),  "tabIndex");
    });
  }

  public void testBindingAttributeFunctionCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_binding.html", "package.json", "object_with_function.ts");
      myFixture.completeBasic();
      myFixture.type('\n');
      myFixture.checkResultByFile("attribute_binding.after.html");
    });
  }

  public void testBindingAttributeFunctionResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("attribute_binding.after.html", "package.json", "object_with_function.ts");
      PsiElement resolve = resolveReference("[mod<caret>el]");
      assertEquals("object_with_function.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSFunction.class);
    });
  }

  public void testEventHandlerCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_event.html", "package.json", "object.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("object_event.after.html");
    });
  }

  public void testEventHandlerResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_event.after.html", "package.json", "object.ts");
      PsiElement resolve = resolveReference("(co<caret>mplete)");
      assertEquals("object.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testEventHandlerOverrideCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_event.html", "package.json", "objectOverride.ts");
      myFixture.completeBasic();
      myFixture.checkResultByFile("object_event.after.html");
    });
  }

  public void testEventHandlerOverrideResolve2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("object_event.after.html", "package.json", "objectOverride.ts");
      PsiElement resolve = resolveReference("(co<caret>mplete)");
      assertEquals("objectOverride.ts", resolve.getContainingFile().getName());
      assertInstanceOf(resolve, JSField.class);
    });
  }

  public void testForCompletion2TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("for2.html", "package.json");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ngF<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "ngFor", "[ngForOf]");
    });
  }

  public void testForOfResolve2Typescript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("for2.html", "package.json");
      PsiElement resolve = resolveReference("ngF<caret>");
      assertEquals("package.json", resolve.getContainingFile().getName());
      assertEquals("Directive({selector: '[ngFor][ngForOf]', properties: ['ngForOf'], lifecycle: [onCheck]})", getDirectiveDefinitionText(resolve));
    });
  }

  public void testForCompletion2Javascript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("for2.html", "common.metadata.json");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("ngF<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "ngFor", "[ngForOf]");
    });
  }

  public void testIfCompletion4JavascriptUmd() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("if4.html", "common.metadata.json");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("*<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "*ngIf");
      assertDoesntContain(myFixture.getLookupElementStrings(), "ngIf");
    });
  }

  public void testForTemplateCompletion2Javascript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("for2Template.html", "common.metadata.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "*ngFor");
    });
  }

  public void testForOfResolve2Javascript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("for2.html", "common.metadata.json");
      PsiElement resolve = resolveReference("ngF<caret>");
      assertEquals("common.metadata.json", resolve.getContainingFile().getName());
      assertEquals("\"[ngFor][ngForOf]\"", getDirectiveDefinitionText(((JSOffsetBasedImplicitElement)resolve).getElementAtOffset()));
    });
  }

  public void testTemplateUrl20Completion() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom.ts", "package.json", "custom.html");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("templateUrl: '<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "custom.ts", "package.json", "custom.html");
    });
  }

  public void testTemplateUrl20Resolve() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom.template.ts", "package.json", "custom.html");
      PsiElement resolve = resolveReference("templateUrl: '<caret>");
      assertInstanceOf(resolve, PsiFile.class);
      assertEquals("custom.html", ((PsiFile)resolve).getName());
    });
  }

  public void testStyleUrls20Completion() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom.ts", "package.json", "custom.html");
      int offsetBySignature = AngularTestUtil.findOffsetBySignature("styleUrls: ['<caret>", myFixture.getFile());
      myFixture.getEditor().getCaretModel().moveToOffset(offsetBySignature);
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "custom.ts", "package.json", "custom.html");
    });
  }


  public void testStyleUrls20Resolve() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("custom.style.ts", "package.json", "custom.html");
      PsiElement resolve = resolveReference("styleUrls: ['<caret>");
      assertInstanceOf(resolve, PsiFile.class);
      assertEquals("custom.html", ((PsiFile)resolve).getName());
    });
  }

  public void testTemplate20TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("template.html", "package.json", "template.ts");
      PsiElement resolve = resolveReference("*myHover<caret>List");
      assertEquals("template.ts", resolve.getContainingFile().getName());
      assertUnresolvedReference("myHover<caret>List");
    });
  }

  public void testNoTemplate20TypeScript() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("noTemplate.html", "package.json", "noTemplate.ts");
      PsiElement resolve = resolveReference("myHover<caret>List");
      assertEquals("noTemplate.ts", resolve.getContainingFile().getName());
      assertUnresolvedReference("*myHover<caret>List");
    });
  }
  public void testTemplate20JavaScript() {
    myFixture.configureByFiles("template.html", "angular2_compiled.js", "template.metadata.json");
    PsiElement resolve = resolveReference("*myHover<caret>List");
    assertEquals("template.metadata.json", resolve.getContainingFile().getName());
    assertUnresolvedReference("myHover<caret>List");
  }

  public void testNoTemplate20JavaScript() {
    myFixture.configureByFiles("noTemplate.html", "angular2_compiled.js", "noTemplate.metadata.json");
    PsiElement resolve = resolveReference("myHover<caret>List");
    assertEquals("noTemplate.metadata.json", resolve.getContainingFile().getName());
    assertUnresolvedReference("*myHover<caret>List");
  }

  public void testBindingNamespace() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("bindingNamespace.html", "package.json");
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.enableInspections(XmlUnboundNsPrefixInspection.class);
      myFixture.checkHighlighting();
    });
  }

  public void testEventNamespace() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("eventNamespace.html", "package.json");
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.enableInspections(XmlUnboundNsPrefixInspection.class);
      myFixture.checkHighlighting();
    });
  }

  public void testCssExternalReference20() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("cssExtRef.ts", "package.json", "css.css");
      PsiElement resolve = resolveReference("inDa<caret>Class");
      assertEquals("css.css", resolve.getContainingFile().getName());
    });
  }

  public void testCssInternalReference20() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("cssIntRef.ts", "package.json");
      resolveReference("inDa<caret>Class");
    });
  }

  public void testCssInternalReferenceWithHtmlTag20() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("cssIntRefHtmlTag.ts", "package.json");
      resolveReference("inDa<caret>Class");
    });
  }

  public void testCaseCompletion2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("case.html", "package.json");
      myFixture.completeBasic();
      myFixture.type('\n');
      myFixture.checkResultByFile("case.after.html");
    });
  }

  public void testRouterLink() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("routerLink.html", "package.json", "routerLink.metadata.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "routerLink", "routerLink2");
    });
  }

  public void testComplexSelectorList() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("material.html", "package.json", "button.metadata.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "mat-icon-button");
    });
  }

  public void testSelectorConcatenationList() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("material.html", "package.json", "button.metadata.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "mat-raised-button");
    });
  }

  public void testComplexSelectorList2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("ionic.html", "package.json", "ionic.metadata.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "ion-item");
    });
  }

  public void testSelectorListSpaces() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("spaces.html", "package.json", "spaces.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "other-attr");
    });
  }

  public void testSelectorListSpaces2() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("spaces.html", "package.json", "spaces.ts");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "other-attr");
    });
  }

  public void testSelectorListSpacesCompiled() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.configureByFiles("flexOrder.html", "package.json", "flexOrder.metadata.json");
      myFixture.completeBasic();
      assertContainsElements(myFixture.getLookupElementStrings(), "fxFlexOrder");
    });
  }

  public void testId() throws Exception {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, getProject(), (ThrowableRunnable<Exception>)() -> {
      myFixture.enableInspections(XmlInvalidIdInspection.class);
      myFixture.configureByFiles("id.html", "package.json", "object.ts");
      myFixture.checkHighlighting();
    });
  }

  public void testViewChildReferenceNavigation() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      PsiReference reference = myFixture.getReferenceAtCaretPosition("viewChildReference.ts", "package.json");
      assertNotNull(reference);
      PsiElement el = reference.resolve();
      assertNotNull(el);
      assertEquals("#area", el.getText());
    });
  }

  public void testViewChildReferenceContentAssist() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () ->
      assertEquals(Arrays.asList("area", "area2"),
                   myFixture.getCompletionVariants("viewChildReference.ts", "package.json"))
    );
  }

  public void testViewChildReferenceNavigationHTML() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      PsiReference reference =
        myFixture.getReferenceAtCaretPosition("viewChildReferenceHTML.ts", "viewChildReferenceHTML.html", "package.json");
      assertNotNull(reference);
      PsiElement el = reference.resolve();
      assertNotNull(el);
      assertEquals("viewChildReferenceHTML.html", el.getContainingFile().getName());
      assertEquals("#area", el.getText());
    });
  }

  public void testViewChildReferenceContentAssistHTML() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () ->
      assertEquals(Arrays.asList("area", "area2"),
                   myFixture.getCompletionVariants("viewChildReferenceHTML.ts", "viewChildReferenceHTML.html", "package.json"))
    );
  }

  public void testI18NAttr() {
    JSTestUtils.testWithinLanguageLevel(JSLanguageLevel.ES6, myFixture.getProject(), () -> {
      myFixture.enableInspections(HtmlUnknownAttributeInspection.class);
      myFixture.configureByFiles("i18n.html", "package.json");
      myFixture.checkHighlighting(true, false, true);
    });
  }

}
