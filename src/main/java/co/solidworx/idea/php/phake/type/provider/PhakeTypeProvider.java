package co.solidworx.idea.php.phake.type.provider;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

abstract public class PhakeTypeProvider implements PhpTypeProvider4 {
    @Override
    public @Nullable PhpType complete(String s, Project project) {
        return null;
    }

    @Nullable
    protected static String resolvedParameter(@NotNull PhpIndex phpIndex, @NotNull String parameter) {
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

    public Collection<? extends PhpNamedElement> getElementsBySignature(String expression, Project project) {
        Collection<PhpNamedElement> elements = new HashSet<>();

        PhpIndex phpIndex = PhpIndex.getInstance(project);

        if (!expression.contains("|")) {
            String parameterResolved = resolvedParameter(phpIndex, expression);
            if (parameterResolved == null) {
                return elements;
            }

            elements.addAll(phpIndex.getAnyByFQN(parameterResolved));
        } else {
            for (String s : expression.split("\\|")) {
                String parameterResolved = resolvedParameter(phpIndex, s);
                if (parameterResolved == null) {
                    return elements;
                }

                elements.addAll(phpIndex.getAnyByFQN(parameterResolved));
            }
        }

        return elements;
    }

    public Collection<? extends PhpNamedElement> getElementsBySignature(char key, String expression, Project project) {
        int endIndex = expression.lastIndexOf(key);
        if(endIndex == -1) {
            return null;
        }

        String parameter = expression.substring(endIndex + 1);

        return getElementsBySignature(parameter, project);
    }
}