package de.bushnaq.abdalla.projecthub;

import de.bushnaq.abdalla.projecthub.dao.ProductDAO;
import de.bushnaq.abdalla.projecthub.dao.VersionDAO;
import de.bushnaq.abdalla.projecthub.util.AbstractEntityGenerator;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * trying to find optimal entity relation for jpa to minimize db operations.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class DAOTest extends AbstractEntityGenerator {
    Logger logger = LoggerFactory.getLogger(DAOTest.class);


    @Test
    public void create() throws Exception {

        ProductDAO product = new ProductDAO();
        product.setName("Product 1");
        entityManager.persist(product);

        {
            VersionDAO version = new VersionDAO();
            version.setName("Version 1");
            version.setProduct(product);
            entityManager.persist(version);
            product.addVersion(version);
        }

        List<ProductDAO> list1 = getAllProducts();

        String jsonString1 = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list1.getFirst());
        System.out.println(jsonString1);
        ProductDAO p1 = objectMapper.readerFor(ProductDAO.class).readValue(jsonString1);

        String jsonString11 = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list1.getFirst().getVersions().getFirst());
        System.out.println(jsonString11);
        VersionDAO p11 = objectMapper.readerFor(VersionDAO.class).readValue(jsonString11);


        {
            VersionDAO version = new VersionDAO();
            version.setName("Version 2");
            version.setProduct(product);
            entityManager.persist(version);
            product.addVersion(version);

            String jsonString111 = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(version);
            System.out.println(jsonString111);
            VersionDAO p111 = objectMapper.readerFor(VersionDAO.class).readValue(jsonString111);

        }
        List<ProductDAO> list2 = getAllProducts();

        String jsonString2 = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list2.getFirst());
        System.out.println(jsonString2);
        ProductDAO p2 = objectMapper.readerFor(ProductDAO.class).readValue(jsonString2);

        printTables();
    }

    @Test
    public void delete() throws Exception {
//        addRandomProducts(2);
//        removeProduct(expectedProducts.get(0).getId());
//        testProducts();
//        printTables();
    }

    @Test
    public void getAll() throws Exception {
//        addRandomProducts(3);
//        List<Product> allProducts = productApi.getAllProducts();
//        printTables();
    }

    private List<ProductDAO> getAllProducts() {
        String jpqlQuery = "SELECT e FROM ProductDAO e";
        Query  query     = entityManager.createQuery(jpqlQuery, ProductDAO.class);
        return query.getResultList();
    }

}