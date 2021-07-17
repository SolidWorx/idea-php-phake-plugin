package co.solidworx.idea.php.phake.type.provider;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.Nullable;
import java.util.*;

class PhakeWhenTypeProvider extends PhakeTypeProvider {
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
                (
                    Objects.equals(methodRef.getName(), "when") ||
                    Objects.equals(methodRef.getName(), "verify")
                )
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
    public Collection<? extends PhpNamedElement> getBySignature(String expression, Set<String> set, int i, Project project) {
        return getElementsBySignature(TRIM_KEY, expression, project);
    }
}