package co.solidworx.idea.php.phake.type.provider;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class PhakeWhenMethodCallTypeProvider extends PhakeTypeProvider {
    private static final char KEY = 'ɕ';

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
            PhpExpression expression = ((MethodReference) psiElement).getClassReference();

            if (
                expression instanceof MethodReference &&
                expression.getFirstChild() instanceof ClassReference &&
                Objects.requireNonNull(((ClassReference) expression.getFirstChild()).getName()).equals("Phake") &&
                Objects.requireNonNull(Objects.requireNonNull(expression).getName()).equals("when")
            ) {
                return new PhpType().add("#" + getKey() + "#K#C\\Phake_Proxies_AnswerBinderProxy.class|#K#C\\Phake\\Proxies\\AnswerBinderProxy.class");
            }
        }

        return null;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String expression, Set<String> set, int i, Project project) {
        return getElementsBySignature(expression, project);
    }
}