package co.solidworx.idea.php.phake.type.provider;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class PhakeWhenTypeProvider implements PhpTypeProvider4 {
    private static final char KEY = '\u0253';
    private static final char TRIM_KEY = '\u0254';

    @Override
    public char getKey() {
        return KEY;
    }

    @Override
    public @Nullable PhpType getType(PsiElement psiElement) {
        if (DumbService.getInstance(psiElement.getProject()).isDumb()) {
            return null;
        }

        if (psiElement instanceof MethodReference) {
            MethodReference methodRef = (MethodReference) psiElement;
            PhpExpression phpExpression = methodRef.getClassReference();

            if (
                methodRef.isStatic() &&
                phpExpression != null &&
                Objects.equals(phpExpression.getName(), "Phake") &&
                Objects.equals(methodRef.getName(), "when")
            ) {
                PsiElement[] parameters = methodRef.getParameters();
                String refSignature = methodRef.getSignature();
                PsiElement parameter = parameters[0];

                if ((parameter instanceof Variable)) {
                    String signature = ((Variable) parameter).getSignature();

                    if (StringUtil.isNotEmpty(signature)) {
                        int lastIndexOf = signature.lastIndexOf(PhakeMockTypeProvider.TRIM_KEY);

                        if (lastIndexOf != -1) {
                            return new PhpType().add("#" + getKey() + refSignature + TRIM_KEY + signature.substring(lastIndexOf + 1));
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public @Nullable PhpType complete(String s, Project project) {
        return null;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String expression, Set<String> set, int i, Project project) {
        // get back our original call
        int endIndex = expression.lastIndexOf(TRIM_KEY);

        if(endIndex == -1) {
            return null;
        }

        String parameter = expression.substring(endIndex + 1);

        Collection<PhpNamedElement> elements = new HashSet<>();

        PhpIndex phpIndex = PhpIndex.getInstance(project);

        if (!parameter.contains("|")) {
            String parameterResolved = resolvedParameter(phpIndex, parameter);
            if (parameterResolved == null) {
                return elements;
            }

            elements.addAll(phpIndex.getAnyByFQN(parameterResolved));
        } else {
            for (String s : parameter.split("\\|")) {
                String parameterResolved = resolvedParameter(phpIndex, s);
                if (parameterResolved == null) {
                    return elements;
                }

                elements.addAll(phpIndex.getAnyByFQN(parameterResolved));
            }
        }

        return elements;
    }

    @Nullable
    public static String resolvedParameter(@NotNull PhpIndex phpIndex, @NotNull String parameter) {

        // "Foo::class"
        if (parameter.startsWith("#K#C") && parameter.endsWith(".class")) {
            return StringUtils.stripStart(parameter.substring(4, parameter.length() - 6), "\\");
        }

        // #K#C\Class\Foo.property or #K#C\Class\Foo.CONST
        if (parameter.startsWith("#")) {
            Collection<? extends PhpNamedElement> signTypes = phpIndex.getBySignature(parameter);

            if (signTypes.size() == 0) {
                return null;
            }

            return convertToString(signTypes.iterator().next(), 0);

        }

        return parameter;
    }

    @Nullable
    private static String convertToString(@Nullable PsiElement psiElement, int depth) {
        if (psiElement == null || ++depth > 5) {
            return null;
        }

        if (psiElement instanceof StringLiteralExpression) {
            String resolvedString = ((StringLiteralExpression) psiElement).getContents();
            if (StringUtils.isEmpty(resolvedString)) {
                return null;
            }

            return resolvedString;
        }

        if (psiElement instanceof Field) {
            return convertToString(((Field) psiElement).getDefaultValue(), depth);
        }

        if (psiElement instanceof ClassConstantReference && "class".equals(((ClassConstantReference) psiElement).getName())) {
            // Foobar::class

            PhpExpression classReference = ((ClassConstantReference) psiElement).getClassReference();
            if (!(classReference instanceof PhpReference)) {
                return null;
            }

            String typeName = ((PhpReference) classReference).getFQN();

            return StringUtils.isNotBlank(typeName) ? StringUtils.stripStart(typeName, "\\") : null;
        }

        if (psiElement instanceof PhpReference) {
            PsiReference psiReference = psiElement.getReference();

            if (psiReference == null) {
                return null;
            }

            PsiElement ref = psiReference.resolve();

            if (ref instanceof PhpReference) {
                return convertToString(psiElement, depth);
            }

            if (ref instanceof Field) {
                return convertToString(((Field) ref).getDefaultValue(), 0);
            }
        }

        return null;
    }
}