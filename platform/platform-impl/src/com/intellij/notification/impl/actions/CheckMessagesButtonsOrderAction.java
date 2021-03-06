/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package com.intellij.notification.impl.actions;

import com.intellij.CommonBundle;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.impl.ProjectNewWindowDoNotAskOption;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;

/**
 * @author Denis Fokin
 */
public class CheckMessagesButtonsOrderAction  extends AnAction implements DumbAware {
  @Override
  public void actionPerformed(AnActionEvent e) {

    new Thread() {
      @Override
      public void run() {
        super.run();

        //noinspection SSBasedInspection
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            String message = "Proceed - focused, [space] ,Cancel - Nothing, Show ussages - default";
            String[] options = {"Show usages", "Cancel", "Proceed"};
            Messages.showDialog(message, "First Message", options, 0, 1, Messages.getQuestionIcon(), null);


            Messages.showYesNoCancelDialog("unchecked unfocused checkbox, Yes - focused, space, Cancel, No Text - default",
                                           IdeBundle.message("title.open.project"),
                                           "This window",
                                           "New Window",
                                           CommonBundle.getCancelButtonText(),
                                           Messages.getQuestionIcon(),
                                           new ProjectNewWindowDoNotAskOption());
          }
        });

      }
    }.start();

  }
}
