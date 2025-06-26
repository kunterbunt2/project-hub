/*
 *
 * Copyright (C) 2025-2025 Abdalla Bushnaq
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.bushnaq.abdalla.projecthub.ui.view.util;

import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;

public class TaskListViewTester {
    private final SeleniumHandler seleniumHandler;

    public TaskListViewTester(SeleniumHandler seleniumHandler) {
        this.seleniumHandler = seleniumHandler;
    }

//    public void selectTask(String name) {
//        seleniumHandler.selectGridRow(TASK_GRID_NAME_PREFIX, TaskView.class, name);
//    }
}
