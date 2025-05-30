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

package de.bushnaq.abdalla.projecthub.util;

import de.bushnaq.abdalla.projecthub.ui.ProductListView;
import de.bushnaq.abdalla.projecthub.ui.VersionListView;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static de.bushnaq.abdalla.projecthub.ui.ProductListView.PRODUCT_GRID_NAME_PREFIX;

@Component
public class ProductViewTester {
    private final int             port;
    private final SeleniumHandler seleniumHandler;

    public ProductViewTester(SeleniumHandler seleniumHandler, @Value("${local.server.port:8080}") int port) {
        this.seleniumHandler = seleniumHandler;
        this.port            = port;
    }

    public void createProductCancel(String name) {
        seleniumHandler.click(ProductListView.CREATE_PRODUCT_BUTTON);
        seleniumHandler.setTextField(ProductListView.PRODUCT_NAME_FIELD, name);
        seleniumHandler.click(ProductListView.CANCEL_BUTTON);
        seleniumHandler.ensureIsNotInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, name);
    }

    public void createProductConfirm(String name) {
        seleniumHandler.click(ProductListView.CREATE_PRODUCT_BUTTON);
        seleniumHandler.setTextField(ProductListView.PRODUCT_NAME_FIELD, name);
        seleniumHandler.click(ProductListView.CONFIRM_BUTTON);
        seleniumHandler.ensureIsInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, name);
    }

    public void deleteProductCancel(String name) {
        seleniumHandler.click(ProductListView.PRODUCT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProductListView.PRODUCT_GRID_DELETE_BUTTON_PREFIX + name);
        seleniumHandler.click(ProductListView.CANCEL_BUTTON);
        seleniumHandler.ensureIsInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, name);
    }

    public void deleteProductConfirm(String name) {
        seleniumHandler.click(ProductListView.PRODUCT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProductListView.PRODUCT_GRID_DELETE_BUTTON_PREFIX + name);
        seleniumHandler.click(ProductListView.CONFIRM_BUTTON);
        seleniumHandler.ensureIsNotInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, name);
    }

    public void editProductCancel(String name, String newName) {
        seleniumHandler.click(ProductListView.PRODUCT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProductListView.PRODUCT_GRID_EDIT_BUTTON_PREFIX + name);
        seleniumHandler.setTextField(ProductListView.PRODUCT_NAME_FIELD, newName);
        seleniumHandler.click(ProductListView.CANCEL_BUTTON);
        seleniumHandler.ensureIsInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, name);
        seleniumHandler.ensureIsNotInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, newName);
    }

    public void editProductConfirm(String name, String newName) {
        seleniumHandler.click(ProductListView.PRODUCT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProductListView.PRODUCT_GRID_EDIT_BUTTON_PREFIX + name);
        seleniumHandler.setTextField(ProductListView.PRODUCT_NAME_FIELD, newName);
        seleniumHandler.click(ProductListView.CONFIRM_BUTTON);
        seleniumHandler.ensureIsInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, newName);
        seleniumHandler.ensureIsNotInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, name);
    }

    public void selectProduct(String name) {
        seleniumHandler.selectGridRow(PRODUCT_GRID_NAME_PREFIX, VersionListView.class, name);
    }

    public void switchToProductListView() {
        seleniumHandler.getAndCheck("http://localhost:" + port + "/" + ProductListView.ROUTE);
        seleniumHandler.waitUntil(ExpectedConditions.elementToBeClickable(By.id(ProductListView.PRODUCT_LIST_PAGE_TITLE)));
    }
}
