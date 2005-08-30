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
package com.siyeh.ig.bugs;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class NonShortCircuitBooleanInspection extends ExpressionInspection {
    public String getID(){
        return "NonShortCircuitBooleanExpression";
    }
    private final InspectionGadgetsFix fix = new NonShortCircuitBooleanFix();

    public String getDisplayName() {
        return InspectionGadgetsBundle.message("non.short.circuit.boolean.expression.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return InspectionGadgetsBundle.message("non.short.circuit.boolean.expression.problem.descriptor");
    }

    public InspectionGadgetsFix buildFix(PsiElement location) {
        return fix;
    }

    private static class NonShortCircuitBooleanFix extends InspectionGadgetsFix {
        public String getName() {
            return InspectionGadgetsBundle.message("non.short.circuit.boolean.expression.replace.quickfix");
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                                                                         throws IncorrectOperationException{
            final PsiBinaryExpression expression = (PsiBinaryExpression) descriptor.getPsiElement();
            final PsiExpression lhs = expression.getLOperand();
            final PsiExpression rhs = expression.getROperand();
            final PsiJavaToken operationSign = expression.getOperationSign();
            final IElementType tokenType = operationSign.getTokenType();
            assert rhs != null;
            final String newExpression = lhs.getText() + getShortCircuitOperand(tokenType) + rhs.getText();
            replaceExpression(expression, newExpression);
        }

        private static String getShortCircuitOperand(IElementType tokenType) {
            if (tokenType.equals(JavaTokenType.AND)) {
                return "&&";
            }
            else {
                return "||";
            }
        }
    }

    public BaseInspectionVisitor buildVisitor() {
        return new NonShortCircuitBooleanVisitor();
    }

    private static class NonShortCircuitBooleanVisitor extends BaseInspectionVisitor {

        public void visitBinaryExpression(@NotNull PsiBinaryExpression expression) {
            super.visitBinaryExpression(expression);
            if(!(expression.getROperand() != null)){
                return;
            }

            final PsiJavaToken sign = expression.getOperationSign();
            final IElementType tokenType = sign.getTokenType();
            if (!tokenType.equals(JavaTokenType.AND) &&
                    !tokenType.equals(JavaTokenType.OR)) {
                return;
            }
            final PsiType type = expression.getType();
            if (type == null) {
                return;
            }
            if (!type.equals(PsiType.BOOLEAN)) {
                return;
            }
            registerError(expression);
        }
    }
}
