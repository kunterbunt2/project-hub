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

import de.bushnaq.abdalla.projecthub.util.ProductViewTester;
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
public class VersionListViewTest extends AbstractUiTestUtil {
    private final String            newVersionName = "NewVersion-2";
    private final String            productName    = "Product-2";
    @Autowired
    private       ProductViewTester productViewTester;
    private final String            versionName    = "Version-2";
    @Autowired
    private       VersionViewTester versionViewTester;

    @BeforeEach
    public void createProduct() throws Exception {
        productViewTester.switchToProductListView();
        productViewTester.createProductConfirm(productName);
        productViewTester.selectProduct(productName);
    }

    @Test
    public void testCreateCancel() throws Exception {
        versionViewTester.createVersionCancel(versionName);
    }

    @Test
    public void testCreateConfirm() throws Exception {
        versionViewTester.createVersionConfirm(versionName);
    }

    @Test
    public void testDeleteCancel() throws Exception {
        versionViewTester.createVersionConfirm(versionName);
        versionViewTester.deleteVersionCancel(versionName);
    }

    @Test
    public void testDeleteConfirm() throws Exception {
        versionViewTester.createVersionConfirm(versionName);
        versionViewTester.deleteVersionConfirm(versionName);
    }

    @Test
    public void testEditCancel() throws Exception {
        versionViewTester.createVersionConfirm(versionName);
        versionViewTester.editVersionCancel(versionName, newVersionName);
    }

    @Test
    public void testEditConfirm() throws Exception {
        versionViewTester.createVersionConfirm(versionName);
        versionViewTester.editVersionConfirm(versionName, newVersionName);
    }
}
