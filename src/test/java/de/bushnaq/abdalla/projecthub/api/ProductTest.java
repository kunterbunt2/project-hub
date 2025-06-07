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

package de.bushnaq.abdalla.projecthub.api;

import de.bushnaq.abdalla.projecthub.dto.Product;
import de.bushnaq.abdalla.projecthub.util.AbstractEntityGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerErrorException;

import java.util.List;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class ProductTest extends AbstractEntityGenerator {
    private static final long   FAKE_ID     = 999999L;
    private static final String SECOND_NAME = "SECOND_NAME";

    @Test
    public void anonymousSecurity() {
        {
            setUser("admin-user", "ROLE_ADMIN");
            addRandomProducts(1);
            SecurityContextHolder.clearContext();
        }

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            addRandomProducts(1);
        });

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            List<Product> allProducts = productApi.getAll();
        });
        {
            Product product = expectedProducts.getFirst();
            String  name    = product.getName();
            product.setName(SECOND_NAME);
            try {
                updateProduct(product);
                fail("should not be able to update");
            } catch (AuthenticationCredentialsNotFoundException e) {
                //expected
                product.setName(name);
            }
        }

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            removeProduct(expectedProducts.get(0).getId());
        });
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            Product product = productApi.getById(expectedProducts.getFirst().getId());
        });
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    public void create() throws Exception {
        addRandomProducts(1);
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    public void delete() throws Exception {
        addRandomProducts(2);
        removeProduct(expectedProducts.get(0).getId());
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    public void deleteUsingFakeId() throws Exception {
        addRandomProducts(2);
        try {
            removeProduct(FAKE_ID);
        } catch (ServerErrorException e) {
            //expected
        }
    }

    @Test
    public void getAll() throws Exception {
        setUser("admin-user", "ROLE_ADMIN");
        addRandomProducts(3);
        setUser("user", "ROLE_USER");
        List<Product> allProducts = productApi.getAll();
        assertEquals(3, allProducts.size());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getAllEmpty() throws Exception {
        List<Product> allProducts = productApi.getAll();
    }

    @Test
    public void getByFakeId() throws Exception {
        setUser("admin-user", "ROLE_ADMIN");
        addRandomProducts(1);
        setUser("user", "ROLE_USER");
        try {
            productApi.getById(FAKE_ID);
            fail("Product should not exist");
        } catch (ServerErrorException e) {
            //expected
        }
    }

    @Test
    public void getById() throws Exception {
        setUser("admin-user", "ROLE_ADMIN");
        addRandomProducts(1);
        setUser("user", "ROLE_USER");
        Product product = productApi.getById(expectedProducts.getFirst().getId());
        assertProductEquals(expectedProducts.getFirst(), product, true);//shallow test
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    public void update() throws Exception {
        addRandomProducts(2);
        Product product = expectedProducts.getFirst();
        product.setName(SECOND_NAME);
        updateProduct(product);
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    public void updateUsingFakeId() throws Exception {
        addRandomProducts(2);
        Product product = expectedProducts.getFirst();
        Long    id      = product.getId();
        String  name    = product.getName();
        product.setId(FAKE_ID);
        product.setName(SECOND_NAME);
        try {
            updateProduct(product);
            fail("should not be able to update");
        } catch (ServerErrorException e) {
            //restore fields to match db for later tests in @AfterEach
            product.setId(id);
            product.setName(name);
        }
    }

    @Test
    public void userSecurity() {
        {
            setUser("admin-user", "ROLE_ADMIN");
            addRandomProducts(1);
        }
        setUser("user", "ROLE_USER");
        assertThrows(AccessDeniedException.class, () -> {
            addRandomProducts(1);
        });
        {
            Product product = expectedProducts.getFirst();
            String  name    = product.getName();
            product.setName(SECOND_NAME);
            try {
                updateProduct(product);
                fail("should not be able to update");
            } catch (AccessDeniedException e) {
                //restore fields to match db for later tests in @AfterEach
                product.setName(name);
            }

        }
        assertThrows(AccessDeniedException.class, () -> {
            removeProduct(expectedProducts.get(0).getId());
        });
    }

}