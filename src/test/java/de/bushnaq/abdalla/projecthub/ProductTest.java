package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.util.AbstractTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class ProductTest extends AbstractTestUtil {
    Logger logger = LoggerFactory.getLogger(ProductTest.class);


    @Test
    public void create() throws Exception {
        generateBasicProduct();

        printTables();
    }

    @Test
    public void delete() throws Exception {
        generateBasicProduct();
        removeProduct(products.get(0).getId());
        List<Product> allProducts = productApi.getAllProducts();
        printTables();
    }

    private void generateBasicProduct() {
        User user1 = addUser();

        for (int i = 0; i < 1; i++) {
            Product product = addProduct("Product " + i);
            Version version = addVersion(product, String.format("1.%d.0", i));
            Project project = addProject(version);
            Sprint  sprint  = addSprint(project);
            Task    task1   = addTask(sprint, null, "Project Phase 1", LocalDateTime.now(), Duration.ofDays(10), null, null);
            Task    task2   = addTask(null, task1, "Design", LocalDateTime.now(), Duration.ofDays(4), user1, null);
            Task    task3   = addTask(null, task1, "Implementation", LocalDateTime.now().plusDays(4), Duration.ofDays(6), user1, task1);
        }

        //test the saved product
        for (int i = 0; i < 1; i++) {
            Product retrievedProduct = productApi.getProduct(products.get(i).getId());
            assertProductEquals(products.get(i), retrievedProduct);
        }
    }

    @Test
    public void getAll() throws Exception {
        generateBasicProduct();
        List<Product> allProducts = productApi.getAllProducts();
        printTables();
    }

}