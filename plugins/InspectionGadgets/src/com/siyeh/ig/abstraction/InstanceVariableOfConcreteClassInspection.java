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
package com.siyeh.ig.abstraction;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiTypeElement;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.FieldInspection;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class InstanceVariableOfConcreteClassInspection extends FieldInspection {

    public String getDisplayName() {
        return InspectionGadgetsBundle.message("instance.variable.of.concrete.class.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.ABSTRACTION_GROUP_NAME;
    }

    public String buildErrorString(Object arg) {
        return InspectionGadgetsBundle.message("instance.variable.of.concrete.class.problem.descriptor", arg);
    }

    public BaseInspectionVisitor buildVisitor() {
        return new InstanceVariableOfConcreteClassVisitor();
    }

    private static class InstanceVariableOfConcreteClassVisitor extends BaseInspectionVisitor {

        public void visitField(@NotNull PsiField field) {
            super.visitField(field);
            if (field.hasModifierProperty(PsiModifier.STATIC)) {
                return;
            }
            final PsiTypeElement typeElement = field.getTypeElement();
            if (!ConcreteClassUtil.typeIsConcreteClass(typeElement)) {
                return;
            }
            final String variableName = field.getName();
            registerError(typeElement, variableName);
        }
    }

}
