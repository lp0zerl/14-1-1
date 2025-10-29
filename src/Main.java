// SkyshopApplication.java
package org.skypro.skyshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;
import java.util.stream.Collectors;

// ==================== ГЛАВНЫЙ КЛАСС ПРИЛОЖЕНИЯ ====================
@SpringBootApplication
public class SkyshopApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkyshopApplication.class, args);
    }
}

// ==================== ИСКЛЮЧЕНИЯ И ОБРАБОТКА ОШИБОК ====================

// Собственное исключение для ненайденного продукта
class NoSuchProductException extends RuntimeException {
    public NoSuchProductException(String message) {
        super(message);
    }
}

// Модель ошибки для возврата в JSON
class ShopError {
    private final String code;
    private final String message;

    public ShopError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

// Обработчик исключений
@ControllerAdvice
class ShopControllerAdvice {

    @ExceptionHandler(NoSuchProductException.class)
    public ResponseEntity<ShopError> handleNoSuchProductException(NoSuchProductException ex) {
        ShopError error = new ShopError("PRODUCT_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}

// ==================== МОДЕЛИ ====================

// Интерфейс Searchable
interface Searchable {
    UUID getId();
    String getName();
}

// Класс Product
class Product implements Searchable {
    private final UUID id;
    private final String name;
    private final double price;

    public Product(UUID id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    @JsonIgnore
    public String getSearchTerm() {
        return name;
    }

    @JsonIgnore
    public String getContentType() {
        return "PRODUCT";
    }

    @Override
    public String toString() {
        return name + " (" + price + " руб.)";
    }
}

// Класс Article
class Article implements Searchable {
    private final UUID id;
    private final String name;
    private final String content;

    public Article(UUID id, String name, String content) {
        this.id = id;
        this.name = name;
        this.content = content;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    @JsonIgnore
    public String getSearchTerm() {
        return name;
    }

    @JsonIgnore
    public String getContentType() {
        return "ARTICLE";
    }

    @Override
    public String toString() {
        return "Статья: " + name;
    }
}

// Класс SearchResult
class SearchResult {
    private final String id;
    private final String name;
    private final String contentType;

    public SearchResult(String id, String name, String contentType) {
        this.id = id;
        this.name = name;
        this.contentType = contentType;
    }

    // Статический фабричный метод
    public static SearchResult fromSearchable(Searchable searchable) {
        String contentType = (searchable instanceof Product) ? "PRODUCT" : "ARTICLE";
        return new SearchResult(
                searchable.getId().toString(),
                searchable.getName(),
                contentType
        );
    }

    // Геттеры
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContentType() {
        return contentType;
    }
}

// ==================== КОРЗИНА ====================

// Класс BasketItem
class BasketItem {
    private final Product product;
    private final int quantity;

    public BasketItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }
}

// Класс UserBasket
class UserBasket {
    private final List<BasketItem> items;
    private final double total;

    public UserBasket(List<BasketItem> items) {
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        // Подсчет общей стоимости с помощью StreamAPI
        this.total = items.stream()
                .mapToDouble(BasketItem::getTotalPrice)
                .sum();
    }

    public List<BasketItem> getItems() {
        return items;
    }

    public double getTotal() {
        return total;
    }
}

// Компонент корзины с сессионным scope
@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
class ProductBasket {
    private final Map<UUID, Integer> items;

    public ProductBasket() {
        this.items = new HashMap<>();
    }

    // Метод добавления продукта в корзину
    public void addProduct(UUID productId) {
        items.put(productId, items.getOrDefault(productId, 0) + 1);
    }

    // Метод получения всех продуктов в корзине
    public Map<UUID, Integer> getItems() {
        return Collections.unmodifiableMap(new HashMap<>(items));
    }
}

// ==================== СЕРВИСЫ ====================

// Сервис хранения
@Service
class StorageService {
    private final Map<UUID, Product> products;
    private final Map<UUID, Article> articles;

    public StorageService() {
        this.products = new HashMap<>();
        this.articles = new HashMap<>();
        initializeTestData();
    }

    private void initializeTestData() {
        // Добавляем тестовые продукты
        UUID smartphoneId = UUID.randomUUID();
        UUID laptopId = UUID.randomUUID();
        UUID headphonesId = UUID.randomUUID();

        products.put(smartphoneId, new Product(smartphoneId, "Смартфон Samsung Galaxy", 50000.0));
        products.put(laptopId, new Product(laptopId, "Ноутбук игровой ASUS", 120000.0));
        products.put(headphonesId, new Product(headphonesId, "Наушники беспроводные Sony", 15000.0));
        products.put(UUID.randomUUID(), new Product(UUID.randomUUID(), "Планшет Apple iPad", 45000.0));
        products.put(UUID.randomUUID(), new Product(UUID.randomUUID(), "Смартфон Apple iPhone", 80000.0));
        products.put(UUID.randomUUID(), new Product(UUID.randomUUID(), "Телевизор LG OLED", 150000.0));

        // Добавляем тестовые статьи
        articles.put(UUID.randomUUID(), new Article(UUID.randomUUID(),
                "Обзор нового смартфона Samsung",
                "Полный обзор флагманского смартфона Samsung..."));
        articles.put(UUID.randomUUID(), new Article(UUID.randomUUID(),
                "Лучшие игровые ноутбуки 2024",
                "Топ-10 игровых ноутбуков этого года..."));
        articles.put(UUID.randomUUID(), new Article(UUID.randomUUID(),
                "Сравнение беспроводных наушников",
                "Детальное сравнение популярных моделей..."));
        articles.put(UUID.randomUUID(), new Article(UUID.randomUUID(),
                "Новости технологий",
                "Свежие новости из мира технологий..."));
    }

    public Collection<Product> getAllProducts() {
        return Collections.unmodifiableCollection(products.values());
    }

    public Collection<Article> getAllArticles() {
        return Collections.unmodifiableCollection(articles.values());
    }

    public Collection<Searchable> getAllSearchableItems() {
        List<Searchable> allItems = new ArrayList<>();
        allItems.addAll(products.values());
        allItems.addAll(articles.values());
        return Collections.unmodifiableCollection(allItems);
    }

    // Новый метод для получения продукта по ID
    public Optional<Product> getProductById(UUID id) {
        return Optional.ofNullable(products.get(id));
    }
}

// Сервис поиска
@Service
class SearchService {
    private final StorageService storageService;

    public SearchService(StorageService storageService) {
        this.storageService = storageService;
    }

    public Collection<SearchResult> search(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return storageService.getAllSearchableItems().stream()
                    .map(SearchResult::fromSearchable)
                    .collect(Collectors.toList());
        }

        String lowerPattern = pattern.toLowerCase();

        return storageService.getAllSearchableItems().stream()
                .filter(item -> item.getName().toLowerCase().contains(lowerPattern))
                .map(SearchResult::fromSearchable)
                .collect(Collectors.toList());
    }
}

// Сервис работы с корзиной
@Service
class BasketService {
    private final ProductBasket productBasket;
    private final StorageService storageService;

    public BasketService(ProductBasket productBasket, StorageService storageService) {
        this.productBasket = productBasket;
        this.storageService = storageService;
    }

    // Метод добавления товара в корзину по ID - теперь выбрасывает NoSuchProductException
    public void addProductToBasket(UUID productId) {
        Optional<Product> product = storageService.getProductById(productId);
        if (product.isEmpty()) {
            throw new NoSuchProductException("Продукт с ID '" + productId + "' не найден в каталоге");
        }
        productBasket.addProduct(productId);
    }

    // Метод получения корзины пользователя
    public UserBasket getUserBasket() {
        Map<UUID, Integer> basketItems = productBasket.getItems();

        // Преобразуем Map в список BasketItem с помощью StreamAPI
        List<BasketItem> items = basketItems.entrySet().stream()
                .map(entry -> {
                    UUID productId = entry.getKey();
                    Integer quantity = entry.getValue();
                    Product product = storageService.getProductById(productId)
                            .orElseThrow(() -> new NoSuchProductException("Продукт с ID '" + productId + "' не найден в каталоге"));
                    return new BasketItem(product, quantity);
                })
                .collect(Collectors.toList());

        return new UserBasket(items);
    }
}

// ==================== КОНТРОЛЛЕР ====================

@RestController
@RequestMapping("/shop")
class ShopController {
    private final StorageService storageService;
    private final SearchService searchService;
    private final BasketService basketService;

    public ShopController(StorageService storageService, SearchService searchService, BasketService basketService) {
        this.storageService = storageService;
        this.searchService = searchService;
        this.basketService = basketService;
    }

    @GetMapping("/products")
    public Collection<Product> getAllProducts() {
        return storageService.getAllProducts();
    }

    @GetMapping("/articles")
    public Collection<Article> getAllArticles() {
        return storageService.getAllArticles();
    }

    @GetMapping("/search")
    public Collection<SearchResult> search(@RequestParam String pattern) {
        return searchService.search(pattern);
    }

    // Новый метод для добавления продукта в корзину
    @GetMapping("/basket/{id}")
    public String addProduct(@PathVariable("id") UUID id) {
        basketService.addProductToBasket(id);
        return "Продукт успешно добавлен";
    }

    // Новый метод для отображения корзины
    @GetMapping("/basket")
    public UserBasket getUserBasket() {
        return basketService.getUserBasket();
    }
}

// ==================== ТЕСТЫ ====================

// Тесты для SearchService
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import java.util.*;

class SearchServiceTest {
    private StorageService storageService;
    private SearchService searchService;

    @BeforeEach
    void setUp() {
        storageService = Mockito.mock(StorageService.class);
        searchService = new SearchService(storageService);
    }

    @Test
    @DisplayName("Поиск при отсутствии объектов в StorageService")
    void search_WhenNoObjectsInStorage_ShouldReturnEmptyList() {
        // Arrange
        Mockito.when(storageService.getAllSearchableItems()).thenReturn(Collections.emptyList());

        // Act
        Collection<SearchResult> results = searchService.search("тест");

        // Assert
        assert results.isEmpty() : "Должен вернуться пустой список при отсутствии объектов";
    }

    @Test
    @DisplayName("Поиск когда объекты есть, но нет подходящих")
    void search_WhenObjectsExistButNoMatches_ShouldReturnEmptyList() {
        // Arrange
        List<Searchable> items = Arrays.asList(
                new Product(UUID.randomUUID(), "Ноутбук", 50000.0),
                new Article(UUID.randomUUID(), "Обзор компьютера", "Содержание...")
        );
        Mockito.when(storageService.getAllSearchableItems()).thenReturn(items);

        // Act
        Collection<SearchResult> results = searchService.search("смартфон");

        // Assert
        assert results.isEmpty() : "Должен вернуться пустой список при отсутствии совпадений";
    }

    @Test
    @DisplayName("Поиск когда есть подходящие объекты")
    void search_WhenMatchingObjectsExist_ShouldReturnResults() {
        // Arrange
        UUID productId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        List<Searchable> items = Arrays.asList(
                new Product(productId, "Смартфон Samsung", 50000.0),
                new Article(articleId, "Обзор смартфона", "Содержание..."),
                new Product(UUID.randomUUID(), "Ноутбук", 70000.0)
        );
        Mockito.when(storageService.getAllSearchableItems()).thenReturn(items);

        // Act
        Collection<SearchResult> results = searchService.search("смартфон");

        // Assert
        assert results.size() == 2 : "Должно вернуться 2 результата";

        // Проверяем, что результаты содержат правильные объекты
        Set<String> resultNames = results.stream()
                .map(SearchResult::getName)
                .collect(Collectors.toSet());
        assert resultNames.contains("Смартфон Samsung") : "Должен содержать продукт 'Смартфон Samsung'";
        assert resultNames.contains("Обзор смартфона") : "Должен содержать статью 'Обзор смартфона'";
    }

    @Test
    @DisplayName("Поиск с пустым запросом возвращает все объекты")
    void search_WithEmptyQuery_ShouldReturnAllItems() {
        // Arrange
        List<Searchable> items = Arrays.asList(
                new Product(UUID.randomUUID(), "Товар 1", 1000.0),
                new Article(UUID.randomUUID(), "Статья 1", "Содержание...")
        );
        Mockito.when(storageService.getAllSearchableItems()).thenReturn(items);

        // Act
        Collection<SearchResult> results = searchService.search("");

        // Assert
        assert results.size() == 2 : "Должен вернуть все объекты при пустом запросе";
    }

    @Test
    @DisplayName("Поиск с null запросом возвращает все объекты")
    void search_WithNullQuery_ShouldReturnAllItems() {
        // Arrange
        List<Searchable> items = Arrays.asList(
                new Product(UUID.randomUUID(), "Товар 1", 1000.0),
                new Article(UUID.randomUUID(), "Статья 1", "Содержание...")
        );
        Mockito.when(storageService.getAllSearchableItems()).thenReturn(items);

        // Act
        Collection<SearchResult> results = searchService.search(null);

        // Assert
        assert results.size() == 2 : "Должен вернуть все объекты при null запросе";
    }

    @Test
    @DisplayName("Поиск должен быть регистронезависимым")
    void search_ShouldBeCaseInsensitive() {
        // Arrange
        List<Searchable> items = Arrays.asList(
                new Product(UUID.randomUUID(), "Смартфон Samsung", 50000.0)
        );
        Mockito.when(storageService.getAllSearchableItems()).thenReturn(items);

        // Act
        Collection<SearchResult> results1 = searchService.search("СМАРТФОН");
        Collection<SearchResult> results2 = searchService.search("смартфон");
        Collection<SearchResult> results3 = searchService.search("Смартфон");

        // Assert
        assert results1.size() == 1 : "Должен находить при верхнем регистре";
        assert results2.size() == 1 : "Должен находить при нижнем регистре";
        assert results3.size() == 1 : "Должен находить при смешанном регистре";
    }
}

// Тесты для BasketService
class BasketServiceTest {
    private ProductBasket productBasket;
    private StorageService storageService;
    private BasketService basketService;

    @BeforeEach
    void setUp() {
        productBasket = Mockito.mock(ProductBasket.class);
        storageService = Mockito.mock(StorageService.class);
        basketService = new BasketService(productBasket, storageService);
    }

    @Test
    @DisplayName("Добавление несуществующего товара в корзину приводит к исключению")
    void addProductToBasket_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        UUID nonExistentProductId = UUID.randomUUID();
        Mockito.when(storageService.getProductById(nonExistentProductId))
                .thenReturn(Optional.empty());

        // Act & Assert
        try {
            basketService.addProductToBasket(nonExistentProductId);
            assert false : "Должно было быть выброшено исключение";
        } catch (NoSuchProductException e) {
            // Ожидаемое поведение
            assert e.getMessage().contains(nonExistentProductId.toString()) :
                    "Сообщение об ошибке должно содержать ID продукта";
        }
    }

    @Test
    @DisplayName("Добавление существующего товара вызывает метод addProduct у корзины")
    void addProductToBasket_WhenProductExists_ShouldCallAddProduct() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "Тестовый продукт", 1000.0);
        Mockito.when(storageService.getProductById(productId))
                .thenReturn(Optional.of(product));

        // Act
        basketService.addProductToBasket(productId);

        // Assert
        Mockito.verify(productBasket, Mockito.times(1)).addProduct(productId);
    }

    @Test
    @DisplayName("Метод getUserBasket возвращает пустую корзину, если ProductBasket пуст")
    void getUserBasket_WhenBasketIsEmpty_ShouldReturnEmptyBasket() {
        // Arrange
        Mockito.when(productBasket.getItems()).thenReturn(Collections.emptyMap());

        // Act
        UserBasket userBasket = basketService.getUserBasket();

        // Assert
        assert userBasket.getItems().isEmpty() : "Корзина должна быть пустой";
        assert userBasket.getTotal() == 0.0 : "Общая стоимость должна быть 0";
    }

    @Test
    @DisplayName("Метод getUserBasket возвращает правильную корзину, когда в ProductBasket есть товары")
    void getUserBasket_WhenBasketHasItems_ShouldReturnCorrectBasket() {
        // Arrange
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();

        Product product1 = new Product(productId1, "Продукт 1", 1000.0);
        Product product2 = new Product(productId2, "Продукт 2", 2000.0);

        Map<UUID, Integer> basketItems = new HashMap<>();
        basketItems.put(productId1, 2); // 2 шт продукта 1
        basketItems.put(productId2, 1); // 1 шт продукта 2

        Mockito.when(productBasket.getItems()).thenReturn(basketItems);
        Mockito.when(storageService.getProductById(productId1)).thenReturn(Optional.of(product1));
        Mockito.when(storageService.getProductById(productId2)).thenReturn(Optional.of(product2));

        // Act
        UserBasket userBasket = basketService.getUserBasket();

        // Assert
        assert userBasket.getItems().size() == 2 : "Должно быть 2 элемента в корзине";
        assert userBasket.getTotal() == 4000.0 : "Общая стоимость должна быть 4000 (2*1000 + 1*2000)";

        // Проверяем содержимое корзины
        BasketItem item1 = userBasket.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId1))
                .findFirst()
                .orElse(null);
        assert item1 != null : "Должен содержать продукт 1";
        assert item1.getQuantity() == 2 : "Количество продукта 1 должно быть 2";

        BasketItem item2 = userBasket.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId2))
                .findFirst()
                .orElse(null);
        assert item2 != null : "Должен содержать продукт 2";
        assert item2.getQuantity() == 1 : "Количество продукта 2 должно быть 1";
    }

    @Test
    @DisplayName("Метод getUserBasket выбрасывает исключение, если продукт в корзине не найден в каталоге")
    void getUserBasket_WhenProductInBasketNotFound_ShouldThrowException() {
        // Arrange
        UUID existingProductId = UUID.randomUUID();
        UUID nonExistentProductId = UUID.randomUUID();

        Product existingProduct = new Product(existingProductId, "Существующий продукт", 1000.0);

        Map<UUID, Integer> basketItems = new HashMap<>();
        basketItems.put(existingProductId, 1);
        basketItems.put(nonExistentProductId, 1); // Несуществующий продукт

        Mockito.when(productBasket.getItems()).thenReturn(basketItems);
        Mockito.when(storageService.getProductById(existingProductId))
                .thenReturn(Optional.of(existingProduct));
        Mockito.when(storageService.getProductById(nonExistentProductId))
                .thenReturn(Optional.empty());

        // Act & Assert
        try {
            basketService.getUserBasket();
            assert false : "Должно было быть выброшено исключение";
        } catch (NoSuchProductException e) {
            // Ожидаемое поведение
            assert e.getMessage().contains(nonExistentProductId.toString()) :
                    "Сообщение об ошибке должно содержать ID несуществующего продукта";
        }
    }

    @Test
    @DisplayName("Добавление товара несколько раз увеличивает количество")
    void addProductToBasket_MultipleCalls_ShouldIncreaseQuantity() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Product product = new Product(productId, "Тестовый продукт", 1000.0);
        Mockito.when(storageService.getProductById(productId))
                .thenReturn(Optional.of(product));

        // Act
        basketService.addProductToBasket(productId);
        basketService.addProductToBasket(productId);
        basketService.addProductToBasket(productId);

        // Assert
        Mockito.verify(productBasket, Mockito.times(3)).addProduct(productId);
    }
}

// Тестовый раннер для демонстрации работы тестов
class TestRunner {
    public static void main(String[] args) {
        System.out.println("=== Запуск тестов SearchService ===");
        runSearchServiceTests();

        System.out.println("\n=== Запуск тестов BasketService ===");
        runBasketServiceTests();

        System.out.println("\n=== Все тесты завершены ===");
    }

    private static void runSearchServiceTests() {
        SearchServiceTest searchTest = new SearchServiceTest();

        searchTest.setUp();
        searchTest.search_WhenNoObjectsInStorage_ShouldReturnEmptyList();
        System.out.println("✓ search_WhenNoObjectsInStorage_ShouldReturnEmptyList - ПРОЙДЕН");

        searchTest.setUp();
        searchTest.search_WhenObjectsExistButNoMatches_ShouldReturnEmptyList();
        System.out.println("✓ search_WhenObjectsExistButNoMatches_ShouldReturnEmptyList - ПРОЙДЕН");

        searchTest.setUp();
        searchTest.search_WhenMatchingObjectsExist_ShouldReturnResults();
        System.out.println("✓ search_WhenMatchingObjectsExist_ShouldReturnResults - ПРОЙДЕН");

        searchTest.setUp();
        searchTest.search_WithEmptyQuery_ShouldReturnAllItems();
        System.out.println("✓ search_WithEmptyQuery_ShouldReturnAllItems - ПРОЙДЕН");

        searchTest.setUp();
        searchTest.search_WithNullQuery_ShouldReturnAllItems();
        System.out.println("✓ search_WithNullQuery_ShouldReturnAllItems - ПРОЙДЕН");

        searchTest.setUp();
        searchTest.search_ShouldBeCaseInsensitive();
        System.out.println("✓ search_ShouldBeCaseInsensitive - ПРОЙДЕН");
    }

    private static void runBasketServiceTests() {
        BasketServiceTest basketTest = new BasketServiceTest();

        basketTest.setUp();
        basketTest.addProductToBasket_WhenProductDoesNotExist_ShouldThrowException();
        System.out.println("✓ addProductToBasket_WhenProductDoesNotExist_ShouldThrowException - ПРОЙДЕН");

        basketTest.setUp();
        basketTest.addProductToBasket_WhenProductExists_ShouldCallAddProduct();
        System.out.println("✓ addProductToBasket_WhenProductExists_ShouldCallAddProduct - ПРОЙДЕН");

        basketTest.setUp();
        basketTest.getUserBasket_WhenBasketIsEmpty_ShouldReturnEmptyBasket();
        System.out.println("✓ getUserBasket_WhenBasketIsEmpty_ShouldReturnEmptyBasket - ПРОЙДЕН");

        basketTest.setUp();
        basketTest.getUserBasket_WhenBasketHasItems_ShouldReturnCorrectBasket();
        System.out.println("✓ getUserBasket_WhenBasketHasItems_ShouldReturnCorrectBasket - ПРОЙДЕН");

        basketTest.setUp();
        basketTest.getUserBasket_WhenProductInBasketNotFound_ShouldThrowException();
        System.out.println("✓ getUserBasket_WhenProductInBasketNotFound_ShouldThrowException - ПРОЙДЕН");

        basketTest.setUp();
        basketTest.addProductToBasket_MultipleCalls_ShouldIncreaseQuantity();
        System.out.println("✓ addProductToBasket_MultipleCalls_ShouldIncreaseQuantity - ПРОЙДЕН");
    }
}