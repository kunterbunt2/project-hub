package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.dto.Product;
import de.bushnaq.abdalla.projecthub.util.AbstractEntityGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class ProductTest extends AbstractEntityGenerator {
    Logger logger = LoggerFactory.getLogger(ProductTest.class);


    @Test
    public void create() throws Exception {

        addRandomProducts(1);

        printTables();
    }

    @Test
    public void delete() throws Exception {
        addRandomProducts(2);
        removeProduct(expectedProducts.get(0).getId());
        testProducts();
        printTables();
    }

    @Test
    public void getAll() throws Exception {
        addRandomProducts(3);
        List<Product> allProducts = productApi.getAllProducts();
        printTables();
    }

}