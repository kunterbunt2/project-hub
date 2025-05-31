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

package de.bushnaq.abdalla.projecthub.ui;

import de.bushnaq.abdalla.projecthub.ui.util.AbstractUiTestUtil;
import de.bushnaq.abdalla.projecthub.util.ProductViewTester;
import de.bushnaq.abdalla.projecthub.util.ProjectViewTester;
import de.bushnaq.abdalla.projecthub.util.VersionViewTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "server.port=8080")
@AutoConfigureMockMvc
@Transactional
public class ProjectListViewTest extends AbstractUiTestUtil {
    private final String            newProjectName = "NewProject-2";
    private final String            productName    = "Product-2";
    @Autowired
    private       ProductViewTester productViewTester;
    private final String            projectName    = "Project-2";
    @Autowired
    private       ProjectViewTester projectViewTester;
    private final String            versionName    = "Version-2";
    @Autowired
    private       VersionViewTester versionViewTester;

    @BeforeEach
    public void setupEnvironment() throws Exception {
        // Navigate to product list and create a product
        productViewTester.switchToProductListView();
        productViewTester.createProductConfirm(productName);
        productViewTester.selectProduct(productName);

        // Create a version
        versionViewTester.createVersionConfirm(versionName);
        versionViewTester.selectVersion(versionName);
    }

    @Test
    public void testCreateCancel() throws Exception {
        projectViewTester.createProjectCancel(projectName);
    }

    @Test
    public void testCreateConfirm() throws Exception {
        projectViewTester.createProjectConfirm(projectName);
    }

    @Test
    public void testDeleteCancel() throws Exception {
        projectViewTester.createProjectConfirm(projectName);
        projectViewTester.deleteProjectCancel(projectName);
    }

    @Test
    public void testDeleteConfirm() throws Exception {
        projectViewTester.createProjectConfirm(projectName);
        projectViewTester.deleteProjectConfirm(projectName);
    }

    @Test
    public void testEditCancel() throws Exception {
        projectViewTester.createProjectConfirm(projectName);
        projectViewTester.editProjectCancel(projectName, newProjectName);
    }

    @Test
    public void testEditConfirm() throws Exception {
        projectViewTester.createProjectConfirm(projectName);
        projectViewTester.editProjectConfirm(projectName, newProjectName);
    }
}
