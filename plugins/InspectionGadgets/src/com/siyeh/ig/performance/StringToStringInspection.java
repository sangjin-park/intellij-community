/*
 * Copyright 2003-2005 Dave Griffith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siyeh.ig.performance;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.HardcodedMethodConstants;
import org.jetbrains.annotations.NotNull;

public class StringToStringInspection extends ExpressionInspection {
    private final StringToStringFix fix = new StringToStringFix();

    public String getID(){
        return "RedundantStringToString";
    }

    public String getDisplayName() {
        return "Redundant 'String.toString()'";
    }

    public String getGroupDisplayName() {
        return GroupNames.PERFORMANCE_GROUP_NAME;
    }

    public boolean isEnabledByDefault(){
        return true;
    }
    public String buildErrorString(PsiElement location) {
        return "#ref is redundant #loc";
    }

    public BaseInspectionVisitor buildVisitor() {
        return new StringToStringVisitor();
    }

    public InspectionGadgetsFix buildFix(PsiElement location) {
        return fix;
    }

    private static class StringToStringFix extends InspectionGadgetsFix {
        public String getName() {
            return "Simplify";
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                                                                         throws IncorrectOperationException{
            final PsiMethodCallExpression call = (PsiMethodCallExpression) descriptor.getPsiElement();
            final PsiReferenceExpression expression = call.getMethodExpression();
            final PsiExpression qualifier = expression.getQualifierExpression();
            final String qualifierText = qualifier.getText();
            replaceExpression(call, qualifierText);
        }
    }

    private static class StringToStringVisitor extends BaseInspectionVisitor {

        public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression) {
            super.visitMethodCallExpression(expression);
            final PsiReferenceExpression methodExpression = expression.getMethodExpression();
            if (methodExpression == null) {
                return;
            }
            final String methodName = methodExpression.getReferenceName();
            if (!HardcodedMethodConstants.TO_STRING.equals(methodName)) {
              return;
            }

            final PsiMethod method = expression.resolveMethod();
            if (method == null) {
                return;
            }
            final PsiParameterList paramList = method.getParameterList();
            if (paramList == null) {
                return;
            }
            final PsiParameter[] parameters = paramList.getParameters();
            if (parameters.length != 0) {
                return;
            }
            final PsiClass aClass = method.getContainingClass();
            if(aClass == null)
            {
                return;
            }
            final String className = aClass.getQualifiedName();
            if (!"java.lang.String".equals(className)) {
                return;
            }
            registerError(expression);
        }
    }

}
