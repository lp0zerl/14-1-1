// src/test/java/ru/example/service/SearchServiceTest.java
package ru.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.example.model.Product;
import ru.example.storage.StorageService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private StorageService storageService;

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchService(storageService);
    }

    @Test
    void search_WhenNoProductsInStorage_ShouldReturnEmptyList() {
        // Arrange
        when(storageService.getAllProducts()).thenReturn(Collections.emptyList());

        // Act
        List<Product> result = searchService.search("laptop");

        // Assert
        assertTrue(result.isEmpty());
        verify(storageService, times(1)).getAllProducts();
    }

    @Test
    void search_WhenProductsExistButNoMatch_ShouldReturnEmptyList() {
        List<Product> products = Arrays.asList(
                createProduct("1", "phone", 500.0),
                createProduct("2", "tablet", 300.0)
        );
        when(storageService.getAllProducts()).thenReturn(products);

        // Act
        List<Product> result = searchService.search("laptop");

        // Assert
        assertTrue(result.isEmpty());
        verify(storageService, times(1)).getAllProducts();
    }

    @Test
    void search_WhenMatchingProductExists_ShouldReturnFilteredList() {
        // Arrange
        Product laptop1 = createProduct("1", "gaming laptop", 1000.0);
        Product laptop2 = createProduct("2", "ultrabook laptop", 1200.0);
        Product phone = createProduct("3", "smartphone", 500.0);

        List<Product> products = Arrays.asList(laptop1, laptop2, phone);
        when(storageService.getAllProducts()).thenReturn(products);

        // Act
        List<Product> result = searchService.search("laptop");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(laptop1));
        assertTrue(result.contains(laptop2));
        assertFalse(result.contains(phone));
        verify(storageService, times(1)).getAllProducts();
    }

    @Test
    void search_WhenSearchTermIsEmpty_ShouldReturnAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(
                createProduct("1", "laptop", 1000.0),
                createProduct("2", "phone", 500.0)
        );
        when(storageService.getAllProducts()).thenReturn(products);

        // Act
        List<Product> result = searchService.search("");

        // Assert
        assertEquals(2, result.size());
        verify(storageService, times(1)).getAllProducts();
    }

    @Test
    void search_WhenSearchTermIsNull_ShouldReturnAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(
                createProduct("1", "laptop", 1000.0),
                createProduct("2", "phone", 500.0)
        );
        when(storageService.getAllProducts()).thenReturn(products);

        // Act
        List<Product> result = searchService.search(null);

        // Assert
        assertEquals(2, result.size());
        verify(storageService, times(1)).getAllProducts();
    }

    @Test
    void search_ShouldBeCaseInsensitive() {
        // Arrange
        Product laptop = createProduct("1", "LAPTOP Gaming", 1000.0);
        Product phone = createProduct("2", "phone", 500.0);

        List<Product> products = Arrays.asList(laptop, phone);
        when(storageService.getAllProducts()).thenReturn(products);

        // Act
        List<Product> result = searchService.search("laptop");

        // Assert
        assertEquals(1, result.size());
        assertEquals(laptop, result.get(0));
    }

    // Вспомогательный метод для создания продуктов
    private Product createProduct(String id, String name, double price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        return product;
    }
}

// src/test/java/ru/example/service/BasketServiceTest.java
package ru.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.example.model.Product;
import ru.example.model.ProductBasket;
import ru.example.storage.StorageService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasketServiceTest {

    @Mock
    private ProductBasket productBasket;

    @Mock
    private StorageService storageService;

    private BasketService basketService;

    @BeforeEach
    void setUp() {
        basketService = new BasketService(productBasket, storageService);
    }

    @Test
    void addProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        String productId = "non-existent-id";
        when(storageService.getProductById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> basketService.addProduct(productId));

        verify(storageService, times(1)).getProductById(productId);
        verify(productBasket, never()).addProduct(any(Product.class));
    }

    @Test
    void addProduct_WhenProductExists_ShouldCallAddProductOnBasket() {
        // Arrange
        String productId = "1";
        Product product = createProduct(productId, "laptop", 1000.0);
        when(storageService.getProductById(productId)).thenReturn(Optional.of(product));

        // Act
        basketService.addProduct(productId);

        // Assert
        verify(storageService, times(1)).getProductById(productId);
        verify(productBasket, times(1)).addProduct(product);
    }

    @Test
    void getUserBasket_WhenBasketIsEmpty_ShouldReturnEmptyBasket() {
        // Arrange
        when(productBasket.getProducts()).thenReturn(Collections.emptyList());

        // Act
        List<Product> result = basketService.getUserBasket();

        // Assert
        assertTrue(result.isEmpty());
        verify(productBasket, times(1)).getProducts();
    }

    @Test
    void getUserBasket_WhenBasketHasProducts_ShouldReturnCorrectBasket() {
        // Arrange
        List<Product> expectedProducts = Arrays.asList(
                createProduct("1", "laptop", 1000.0),
                createProduct("2", "mouse", 50.0)
        );
        when(productBasket.getProducts()).thenReturn(expectedProducts);

        // Act
        List<Product> result = basketService.getUserBasket();

        // Assert
        assertEquals(2, result.size());
        assertEquals(expectedProducts, result);
        verify(productBasket, times(1)).getProducts();
    }

    @Test
    void removeProduct_WhenProductExistsInBasket_ShouldRemoveIt() {
        // Arrange
        String productId = "1";
        Product product = createProduct(productId, "laptop", 1000.0);
        when(productBasket.removeProduct(productId)).thenReturn(true);

        // Act
        boolean result = basketService.removeProduct(productId);

        // Assert
        assertTrue(result);
        verify(productBasket, times(1)).removeProduct(productId);
    }

    @Test
    void removeProduct_WhenProductDoesNotExistInBasket_ShouldReturnFalse() {
        // Arrange
        String productId = "non-existent-id";
        when(productBasket.removeProduct(productId)).thenReturn(false);

        // Act
        boolean result = basketService.removeProduct(productId);

        // Assert
        assertFalse(result);
        verify(productBasket, times(1)).removeProduct(productId);
    }

    @Test
    void clearBasket_ShouldCallClearOnBasket() {
        // Act
        basketService.clearBasket();

        // Assert
        verify(productBasket, times(1)).clear();
    }

    @Test
    void getTotalPrice_WhenBasketHasProducts_ShouldReturnCorrectTotal() {
        // Arrange
        List<Product> products = Arrays.asList(
                createProduct("1", "laptop", 1000.0),
                createProduct("2", "mouse", 50.0)
        );
        when(productBasket.getProducts()).thenReturn(products);
        when(productBasket.getTotalPrice()).thenReturn(1050.0);

        // Act
        double totalPrice = basketService.getTotalPrice();

        // Assert
        assertEquals(1050.0, totalPrice);
        verify(productBasket, times(1)).getTotalPrice();
    }

    @Test
    void getTotalPrice_WhenBasketIsEmpty_ShouldReturnZero() {
        // Arrange
        when(productBasket.getProducts()).thenReturn(Collections.emptyList());
        when(productBasket.getTotalPrice()).thenReturn(0.0);

        // Act
        double totalPrice = basketService.getTotalPrice();

        // Assert
        assertEquals(0.0, totalPrice);
        verify(productBasket, times(1)).getTotalPrice();
    }

    // Вспомогательный метод для создания продуктов
    private Product createProduct(String id, String name, double price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        return product;
    }
}

// src/main/java/ru/example/service/SearchService.java
package ru.example.service;

import ru.example.model.Product;
import ru.example.storage.StorageService;
import java.util.List;
import java.util.stream.Collectors;

public class SearchService {
    private final StorageService storageService;

    public SearchService(StorageService storageService) {
        this.storageService = storageService;
    }

    public List<Product> search(String searchTerm) {
        List<Product> allProducts = storageService.getAllProducts();

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return allProducts;
        }

        String lowerCaseSearchTerm = searchTerm.toLowerCase();
        return allProducts.stream()
                .filter(product -> product.getName().toLowerCase().contains(lowerCaseSearchTerm))
                .collect(Collectors.toList());
    }
}

// src/main/java/ru/example/service/BasketService.java
package ru.example.service;

import ru.example.model.Product;
import ru.example.model.ProductBasket;
import ru.example.storage.StorageService;
import java.util.List;
import java.util.Optional;

public class BasketService {
    private final ProductBasket productBasket;
    private final StorageService storageService;

    public BasketService(ProductBasket productBasket, StorageService storageService) {
        this.productBasket = productBasket;
        this.storageService = storageService;
    }

    public void addProduct(String productId) {
        Optional<Product> product = storageService.getProductById(productId);
        if (product.isEmpty()) {
            throw new IllegalArgumentException("Product with id " + productId + " not found");
        }
        productBasket.addProduct(product.get());
    }

    public boolean removeProduct(String productId) {
        return productBasket.removeProduct(productId);
    }

    public List<Product> getUserBasket() {
        return productBasket.getProducts();
    }

    public void clearBasket() {
        productBasket.clear();
    }

    public double getTotalPrice() {
        return productBasket.getTotalPrice();
    }
}

// src/main/java/ru/example/model/Product.java
package ru.example.model;

public class Product {
    private String id;
    private String name;
    private double price;

    public Product() {}

    public Product(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Double.compare(product.price, price) == 0 &&
                id.equals(product.id) &&
                name.equals(product.name);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, price);
    }
}

// src/main/java/ru/example/model/ProductBasket.java
package ru.example.model;

import java.util.ArrayList;
import java.util.List;

public class ProductBasket {
    private final List<Product> products = new ArrayList<>();

    public void addProduct(Product product) {
        products.add(product);
    }

    public boolean removeProduct(String productId) {
        return products.removeIf(product -> product.getId().equals(productId));
    }

    public List<Product> getProducts() {
        return new ArrayList<>(products);
    }

    public void clear() {
        products.clear();
    }

    public double getTotalPrice() {
        return products.stream()
                .mapToDouble(Product::getPrice)
                .sum();
    }
}

// src/main/java/ru/example/storage/StorageService.java
package ru.example.storage;

import ru.example.model.Product;
import java.util.List;
import java.util.Optional;

public interface StorageService {
    List<Product> getAllProducts();
    Optional<Product> getProductById(String id);
}

// pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>ru.example</groupId>
    <artifactId>shopping-app</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.9.2</junit.version>
        <mockito.version>5.1.1</mockito.version>
    </properties>

    <dependencies>
        <!-- JUnit 5 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
<version>${junit.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- Mockito -->
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
<version>${mockito.version}</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
<version>${mockito.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M9</version>
            </plugin>
        </plugins>
    </build>
</project>