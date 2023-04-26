package co.solidworx.idea.php.phake.type.provider;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.Nullable;
import java.util.*;

class PhakeMockTypeProvider extends PhakeTypeProvider {

    public static final char KEY = 'ɑ';
    public static final char TRIM_KEY = 'ɒ';

    final private List<String> methods = Arrays.asList("mock", "partialMock", "partMock");

    @Override
    public char getKey() {
        return KEY;
    }

    @Override
    public @Nullable PhpType getType(PsiElement psiElement) {
        if (DumbService.getInstance(psiElement.getProject()).isDumb()) {
            return null;
        }

        if (
            psiElement instanceof MethodReference methodRef &&
            ((MethodReference) psiElement).isStatic() &&
            Objects.equals(Objects.requireNonNull(((MethodReference) psiElement).getClassReference()).getName(), className) &&
            methods.contains(((MethodReference) psiElement).getName())
        ) {
            String refSignature = methodRef.getSignature();

            if (StringUtil.isEmpty(refSignature)) {
                return null;
            }

            PsiElement[] parameters = methodRef.getParameters();

            if (parameters.length == 0) {
                return null;
            }

            PsiElement parameter = parameters[0];

            String signature = null;

            if ((parameter instanceof StringLiteralExpression)) {
                signature = ((StringLiteralExpression) parameter).getContents();
            } else if ((parameter instanceof ClassConstantReference || parameter instanceof FieldReference)) {
                signature = ((PhpReference) parameter).getSignature();
            }

            if (StringUtil.isNotEmpty(signature)) {
                return new PhpType().add("#" + getKey() + refSignature + TRIM_KEY + signature);
            }
        }

        return null;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String expression, Set<String> set, int i, Project project) {
        return getElementsBySignature(TRIM_KEY, expression, project);
    }
}