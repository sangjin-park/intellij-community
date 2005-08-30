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
package com.siyeh.ig.memory;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.FieldInspection;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class StringBufferFieldInspection extends FieldInspection {

    public String getDisplayName() {
        return InspectionGadgetsBundle.message("stringbuffer.field.display.name");
    }

    public String getGroupDisplayName() {
        return GroupNames.MEMORY_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        final PsiField field = (PsiField) location.getParent();
        assert field != null;
        final PsiType type = field.getType();
        final String typeName = type.getPresentableText();
        return InspectionGadgetsBundle.message("stringbuffer.field.problem.descriptor", typeName);
    }

    public BaseInspectionVisitor buildVisitor() {
        return new StringBufferFieldVisitor();
    }

    private static class StringBufferFieldVisitor extends BaseInspectionVisitor {

        public void visitField(@NotNull PsiField field) {
            super.visitField(field);
            final PsiType type = field.getType();
            if(type == null)
            {
                return;
            }
            if (!type.equalsToText("java.lang.StringBuffer") &&
                        !type.equalsToText("java.lang.StringBuilder")) {
                return;
            }
            registerFieldError(field);

        }

    }

}
