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
package com.siyeh.ig.controlflow;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.ConstantExpressionUtil;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.ComparisonUtils;
import com.siyeh.ig.psiutils.ParenthesesUtils;
import com.siyeh.ig.ui.SingleCheckboxOptionsPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class PointlessBooleanExpressionInspection extends ExpressionInspection{


    public boolean m_ignoreExpressionsContainingConstants = false;

    public JComponent createOptionsPanel(){
        return new SingleCheckboxOptionsPanel(
                "Ignore named constant in determinining pointless expressions",
                this, "m_ignoreExpressionsContainingConstants");
    }
    private final BooleanLiteralComparisonFix fix =
            new BooleanLiteralComparisonFix();

    public String getDisplayName(){
        return "Pointless boolean expression";
    }

    public String getGroupDisplayName(){
        return GroupNames.CONTROL_FLOW_GROUP_NAME;
    }

    public boolean isEnabledByDefault(){
        return true;
    }

    public BaseInspectionVisitor buildVisitor(){
        return new PointlessBooleanExpressionVisitor();
    }

    public String buildErrorString(PsiElement location){
        if(location instanceof PsiBinaryExpression){
            return "#ref can be simplified to " +
                           calculateSimplifiedBinaryExpression((PsiBinaryExpression) location) +
                           " #loc";
        } else{
            return "#ref can be simplified to " +
                           calculateSimplifiedPrefixExpression((PsiPrefixExpression) location) +
                           " #loc";
        }
    }

    @Nullable
    private  String calculateSimplifiedBinaryExpression(PsiBinaryExpression expression){
        final PsiJavaToken sign = expression.getOperationSign();
        final PsiExpression lhs = expression.getLOperand();

        final PsiExpression rhs = expression.getROperand();
        if(rhs == null){
            return null;
        }
        final IElementType tokenType = sign.getTokenType();
        final String rhsText = rhs.getText();
        final String lhsText = lhs.getText();
        if(tokenType.equals(JavaTokenType.ANDAND) ||
                   tokenType.equals(JavaTokenType.AND)){
            if(isTrue(lhs)){
                return rhsText;
            } else{
                return lhsText;
            }
        } else if(tokenType.equals(JavaTokenType.OROR) ||
                          tokenType.equals(JavaTokenType.OR)){
            if(isFalse(lhs)){
                return rhsText;
            } else{
                return lhsText;
            }
        } else if(tokenType.equals(JavaTokenType.XOR) ||
                          tokenType.equals(JavaTokenType.NE)){
            if(isFalse(lhs)){
                return rhsText;
            } else if(isFalse(rhs)){
                return lhsText;
            } else if(isTrue(lhs)){
                return createStringForNegatedExpression(rhs);
            } else{
                return createStringForNegatedExpression(lhs);
            }
        } else if(tokenType.equals(JavaTokenType.EQEQ)){
            if(isTrue(lhs)){
                return rhsText;
            } else if(isTrue(rhs)){
                return lhsText;
            } else if(isFalse(lhs)){
                return createStringForNegatedExpression(rhs);
            } else{
                return createStringForNegatedExpression(lhs);
            }
        } else{
            return "";
        }
    }

    private  String createStringForNegatedExpression(PsiExpression exp){
         if(ComparisonUtils.isComparison(exp)){
            final PsiBinaryExpression binaryExpression =
                    (PsiBinaryExpression) exp;
            final PsiJavaToken sign = binaryExpression.getOperationSign();
            final String operator = sign.getText();
            final String negatedComparison =
                    ComparisonUtils.getNegatedComparison(operator);
            final PsiExpression lhs = binaryExpression.getLOperand();
            final PsiExpression rhs = binaryExpression.getROperand();
            assert rhs != null;
            return lhs.getText() + negatedComparison + rhs.getText();
        } else{
            if(ParenthesesUtils.getPrecendence(exp) >
                    ParenthesesUtils.PREFIX_PRECEDENCE){
                return  "!(" + exp.getText() + ')';
            } else{
                return '!' + exp.getText();
            }
        }
    }

    private  String calculateSimplifiedPrefixExpression(PsiPrefixExpression expression){
        final PsiExpression operand = expression.getOperand();
        if(isTrue(operand)){
          return PsiKeyword.FALSE;
        } else{
          return PsiKeyword.TRUE;
        }
    }

    public InspectionGadgetsFix buildFix(PsiElement location){
        return fix;
    }

    private  class BooleanLiteralComparisonFix
            extends InspectionGadgetsFix{
        public String getName(){
            return "Simplify";
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                                                                         throws IncorrectOperationException{
            final PsiElement element = descriptor.getPsiElement();
            if(element instanceof PsiBinaryExpression){
                final PsiBinaryExpression expression =
                        (PsiBinaryExpression) element;
                final String replacementString =
                        calculateSimplifiedBinaryExpression(expression);
                replaceExpression(expression, replacementString);
            } else{
                final PsiPrefixExpression expression =
                        (PsiPrefixExpression) element;
                final String replacementString =
                        calculateSimplifiedPrefixExpression(expression);
                replaceExpression(expression, replacementString);
            }
        }
    }

    private  class PointlessBooleanExpressionVisitor
            extends BaseInspectionVisitor{
        private  final Set<IElementType> booleanTokens =
                new HashSet<IElementType>(10); 
        {
            booleanTokens.add(JavaTokenType.ANDAND);
            booleanTokens.add(JavaTokenType.AND);
            booleanTokens.add(JavaTokenType.OROR);
            booleanTokens.add(JavaTokenType.OR);
            booleanTokens.add(JavaTokenType.XOR);
            booleanTokens.add(JavaTokenType.EQEQ);
            booleanTokens.add(JavaTokenType.NE);
        }

        public void visitClass(@NotNull PsiClass aClass){
            //to avoid drilldown
        }

        public void visitBinaryExpression(@NotNull PsiBinaryExpression expression){
            super.visitBinaryExpression(expression);
            if(!(expression.getROperand() != null)){
                return;
            }
            final PsiJavaToken sign = expression.getOperationSign();
            final IElementType tokenType = sign.getTokenType();
            if(!booleanTokens.contains(tokenType))
            {
                return;
            }

            final PsiExpression rhs = expression.getROperand();
            if(rhs == null)
            {
                return;
            }
            final PsiType rhsType = rhs.getType();
            if(rhsType == null){
                return;
            }
            if(!rhsType.equals(PsiType.BOOLEAN)&&
                       !rhsType.equalsToText("java.lang.Boolean")){
                return;
            }
            final PsiExpression lhs = expression.getLOperand();
            final PsiType lhsType = lhs.getType();
            if(lhsType == null){
                return;
            }
            if(!lhsType.equals(PsiType.BOOLEAN) &&
                       !lhsType.equalsToText("java.lang.Boolean")){
                return;
            }
            final boolean isPointless;
            if(tokenType.equals(JavaTokenType.EQEQ) ||
                       tokenType.equals(JavaTokenType.NE)){
                isPointless = equalityExpressionIsPointless(lhs, rhs);
            } else if(tokenType.equals(JavaTokenType.ANDAND) ||
                              tokenType.equals(JavaTokenType.AND)){
                isPointless = andExpressionIsPointless(lhs, rhs);
            } else if(tokenType.equals(JavaTokenType.OROR) ||
                              tokenType.equals(JavaTokenType.OR)){
                isPointless = orExpressionIsPointless(lhs, rhs);
            } else if(tokenType.equals(JavaTokenType.XOR)){
                isPointless = xorExpressionIsPointless(lhs, rhs);
            } else{
                isPointless = false;
            }
            if(!isPointless){
                return;
            }
            registerError(expression);
        }

        public void visitPrefixExpression(@NotNull PsiPrefixExpression expression){
            super.visitPrefixExpression(expression);
            final PsiJavaToken sign = expression.getOperationSign();
            if(sign == null){
                return;
            }
            final PsiExpression operand = expression.getOperand();
            final IElementType tokenType = sign.getTokenType();
            if(!(!tokenType.equals(JavaTokenType.EXCL) ||
                    !notExpressionIsPointless(operand))){
                registerError(expression);
            }
        }
    }

    private  boolean equalityExpressionIsPointless(PsiExpression lhs,
                                                         PsiExpression rhs){
        return isTrue(lhs) || isTrue(rhs) || isFalse(lhs) || isFalse(rhs);
    }

    private  boolean andExpressionIsPointless(PsiExpression lhs,
                                                    PsiExpression rhs){
        return isTrue(lhs) || isTrue(rhs);
    }

    private  boolean orExpressionIsPointless(PsiExpression lhs,
                                                   PsiExpression rhs){
        return isFalse(lhs) || isFalse(rhs);
    }

    private  boolean xorExpressionIsPointless(PsiExpression lhs,
                                                    PsiExpression rhs){
        return isTrue(lhs) || isTrue(rhs) || isFalse(lhs) || isFalse(rhs);
    }

    private  boolean notExpressionIsPointless(PsiExpression arg){
        return isFalse(arg) || isTrue(arg);
    }

    private  boolean isTrue(PsiExpression expression){
        if(m_ignoreExpressionsContainingConstants &&
                !(expression instanceof PsiLiteralExpression))
        {
            return false;
        }

        if(expression == null){
            return false;
        }
        final Boolean value =
                (Boolean) ConstantExpressionUtil.computeCastTo(expression,
                                                               PsiType.BOOLEAN);
        return value != null && value;
    }

    private  boolean isFalse(PsiExpression expression){
        if(m_ignoreExpressionsContainingConstants &&
                !(expression instanceof PsiLiteralExpression)){
            return false;
        }
        if(expression == null){
            return false;
        }
        final Boolean value =
                (Boolean) ConstantExpressionUtil.computeCastTo(expression,
                                                               PsiType.BOOLEAN);
        return value != null && !value;
    }
}
