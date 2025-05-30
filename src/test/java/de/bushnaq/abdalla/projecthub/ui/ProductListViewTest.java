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

import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import de.bushnaq.abdalla.projecthub.util.AbstractGanttTestUtil;
import de.bushnaq.abdalla.projecthub.util.ProductViewTester;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
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
public class ProductListViewTest extends AbstractGanttTestUtil {
    @Autowired
    ProductViewTester productViewTester;
    @Autowired
    private SeleniumHandler seleniumHandler;


    @Override
    protected void generateOneProduct(TestInfo testInfo) throws Exception {
        //no need to create default product and user
    }

    @Test
    public void testCreateCancel(TestInfo testInfo) throws Exception {
        productViewTester.switchToProductListView();
        String name = "Product-2";
        productViewTester.createProductCancel(name);
    }

    @Test
    public void testCreateConfirm(TestInfo testInfo) throws Exception {
        productViewTester.switchToProductListView();
        String name = "Product-2";
        productViewTester.createProductConfirm(name);
    }

    @Test
    public void testDeleteCancel(TestInfo testInfo) throws Exception {
        productViewTester.switchToProductListView();
        String name = "Product-2";
        productViewTester.createProductConfirm(name);
        productViewTester.deleteProductCancel(name);
    }

    @Test
    public void testDeleteConfirm(TestInfo testInfo) throws Exception {
        productViewTester.switchToProductListView();
        String name = "Product-2";
        productViewTester.createProductConfirm(name);
        productViewTester.deleteProductConfirm(name);
    }

    @Test
    public void testEditCancel(TestInfo testInfo) throws Exception {
        productViewTester.switchToProductListView();
        String name = "Product-2";
        productViewTester.createProductConfirm(name);
        String newName = "NewProduct-2";
        productViewTester.editProductCancel(name, newName);
    }

    @Test
    public void testEditConfirm(TestInfo testInfo) throws Exception {
        productViewTester.switchToProductListView();
        String name = "Product-2";
        productViewTester.createProductConfirm(name);
        String newName = "NewProduct-2";
        productViewTester.editProductConfirm(name, newName);
    }

}
