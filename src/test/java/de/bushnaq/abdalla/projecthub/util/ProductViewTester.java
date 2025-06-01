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
import de.bushnaq.abdalla.projecthub.ui.common.ConfirmDialog;
import de.bushnaq.abdalla.projecthub.ui.common.ProductDialog;
import de.bushnaq.abdalla.projecthub.ui.util.selenium.SeleniumHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static de.bushnaq.abdalla.projecthub.ui.ProductListView.PRODUCT_GRID_NAME_PREFIX;

/**
 * Test helper class for interacting with the Product UI components.
 * <p>
 * This class provides methods to test product-related operations in the UI such as
 * creating, editing, deleting products and navigating between views. It uses
 * {@link SeleniumHandler} to interact with UI elements and validate results.
 */
@Component
public class ProductViewTester {
    private final int             port;
    private final SeleniumHandler seleniumHandler;

    /**
     * Constructs a new ProductViewTester with the given Selenium handler and server port.
     *
     * @param seleniumHandler the handler for Selenium operations
     * @param port            the port on which the application server is running
     */
    public ProductViewTester(SeleniumHandler seleniumHandler, @Value("${local.server.port:8080}") int port) {
        this.seleniumHandler = seleniumHandler;
        this.port            = port;
    }

    /**
     * Tests the creation of a product where the user cancels the operation.
     * <p>
     * Opens the product creation dialog, enters the given product name, then cancels
     * the dialog. Verifies that no product with the specified name appears in the product list.
     *
     * @param name the name of the product to attempt to create
     */
    public void createProductCancel(String name) {
        seleniumHandler.click(ProductListView.CREATE_PRODUCT_BUTTON);
        seleniumHandler.setTextField(ProductDialog.PRODUCT_NAME_FIELD, name);
        seleniumHandler.click(ProductDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsNotInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests the successful creation of a product.
     * <p>
     * Opens the product creation dialog, enters the given product name, then confirms
     * the dialog. Verifies that a product with the specified name appears in the product list.
     *
     * @param name the name of the product to create
     */
    public void createProductConfirm(String name) {
        seleniumHandler.click(ProductListView.CREATE_PRODUCT_BUTTON);
        seleniumHandler.setTextField(ProductDialog.PRODUCT_NAME_FIELD, name);
        seleniumHandler.click(ProductDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests product deletion where the user cancels the delete confirmation.
     * <p>
     * Opens the context menu for the specified product, selects the delete option,
     * then cancels the confirmation dialog. Verifies that the product still exists in the list.
     *
     * @param name the name of the product to attempt to delete
     */
    public void deleteProductCancel(String name) {
        seleniumHandler.click(ProductListView.PRODUCT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProductListView.PRODUCT_GRID_DELETE_BUTTON_PREFIX + name);
        seleniumHandler.click(ConfirmDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests the successful deletion of a product.
     * <p>
     * Opens the context menu for the specified product, selects the delete option,
     * then confirms the deletion in the confirmation dialog. Verifies that the product
     * is removed from the product list.
     *
     * @param name the name of the product to delete
     */
    public void deleteProductConfirm(String name) {
        seleniumHandler.click(ProductListView.PRODUCT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProductListView.PRODUCT_GRID_DELETE_BUTTON_PREFIX + name);
        seleniumHandler.click(ConfirmDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsNotInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, name);
    }

    /**
     * Tests product editing where the user cancels the edit operation.
     * <p>
     * Opens the context menu for the specified product, selects the edit option,
     * enters a new name, then cancels the edit dialog. Verifies that the product
     * still exists with its original name and no product with the new name exists.
     *
     * @param name    the original name of the product to edit
     * @param newName the new name to attempt to assign to the product
     */
    public void editProductCancel(String name, String newName) {
        seleniumHandler.click(ProductListView.PRODUCT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProductListView.PRODUCT_GRID_EDIT_BUTTON_PREFIX + name);
        seleniumHandler.setTextField(ProductDialog.PRODUCT_NAME_FIELD, newName);
        seleniumHandler.click(ProductDialog.CANCEL_BUTTON);
        seleniumHandler.ensureIsInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, name);
        seleniumHandler.ensureIsNotInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, newName);
    }

    /**
     * Tests the successful editing of a product.
     * <p>
     * Opens the context menu for the specified product, selects the edit option,
     * enters a new name, then confirms the edit. Verifies that the product with
     * the new name appears in the list and the product with the old name is gone.
     *
     * @param name    the original name of the product to edit
     * @param newName the new name to assign to the product
     */
    public void editProductConfirm(String name, String newName) {
        seleniumHandler.click(ProductListView.PRODUCT_GRID_ACTION_BUTTON_PREFIX + name);
        seleniumHandler.click(ProductListView.PRODUCT_GRID_EDIT_BUTTON_PREFIX + name);
        seleniumHandler.setTextField(ProductDialog.PRODUCT_NAME_FIELD, newName);
        seleniumHandler.click(ProductDialog.CONFIRM_BUTTON);
        seleniumHandler.ensureIsInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, newName);
        seleniumHandler.ensureIsNotInList(ProductListView.PRODUCT_GRID_NAME_PREFIX, name);
    }

    /**
     * Selects a product from the product grid and navigates to its versions.
     * <p>
     * Clicks on the specified product row in the product grid, which should
     * navigate to the VersionListView for that product.
     *
     * @param name the name of the product to select
     */
    public void selectProduct(String name) {
        seleniumHandler.selectGridRow(PRODUCT_GRID_NAME_PREFIX, VersionListView.class, name);
    }

    /**
     * Navigates to the ProductListView.
     * <p>
     * Opens the product list URL directly and waits for the page to load
     * by checking for the presence of the page title element.
     */
    public void switchToProductListView() {
        seleniumHandler.getAndCheck("http://localhost:" + port + "/" + ProductListView.ROUTE);
        seleniumHandler.waitUntil(ExpectedConditions.elementToBeClickable(By.id(ProductListView.PRODUCT_LIST_PAGE_TITLE)));
    }
}
