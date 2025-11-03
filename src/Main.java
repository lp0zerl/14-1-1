package org.skypro.skyshop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
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

class NoSuchProductException extends RuntimeException {
    public NoSuchProductException(String message) {
        super(message);
    }
}

class ShopError {
    private final String code;
    private final String message;

    public ShopError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}

@ControllerAdvice
class ShopControllerAdvice {
    @ExceptionHandler(NoSuchProductException.class)
    public ResponseEntity<ShopError> handleNoSuchProductException(NoSuchProductException ex) {
        ShopError error = new ShopError("PRODUCT_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}

// ==================== МОДЕЛИ ====================

interface Searchable {
    UUID getId();
    String getName();
}

class Product implements Searchable {
    private final UUID id;
    private final String name;
    private final double price;

    public Product(UUID id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    @Override public UUID getId() { return id; }
    @Override public String getName() { return name; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return name + " (" + price + " руб.)";
    }
}

class Article implements Searchable {
    private final UUID id;
    private final String name;
    private final String content;

    public Article(UUID id, String name, String content) {
        this.id = id;
        this.name = name;
        this.content = content;
    }

    @Override public UUID getId() { return id; }
    @Override public String getName() { return name; }
    public String getContent() { return content; }

    @Override
    public String toString() {
        return "Статья: " + name;
    }
}

class SearchResult {
    private final String id;
    private final String name;
    private final String contentType;

    public SearchResult(String id, String name, String contentType) {
        this.id = id;
        this.name = name;
        this.contentType = contentType;
    }

    public static SearchResult fromSearchable(Searchable searchable) {
        String contentType = (searchable instanceof Product) ? "PRODUCT" : "ARTICLE";
        return new SearchResult(
                searchable.getId().toString(),
                searchable.getName(),
                contentType
        );
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getContentType() { return contentType; }
}

class BasketItem {
    private final Product product;
    private final int quantity;

    public BasketItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return product.getPrice() * quantity; }
}

class UserBasket {
    private final List<BasketItem> items;
    private final double total;

    public UserBasket(List<BasketItem> items) {
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        this.total = items.stream()
                .mapToDouble(BasketItem::getTotalPrice)
                .sum();
    }

    public List<BasketItem> getItems() { return items; }
    public double getTotal() { return total; }
}

// ==================== СЕРВИСЫ ====================

@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
class ProductBasket {
    private final Map<UUID, Integer> items = new HashMap<>();

    public void addProduct(UUID productId) {
        items.put(productId, items.getOrDefault(productId, 0) + 1);
    }

    public Map<UUID, Integer> getItems() {
        return Collections.unmodifiableMap(new HashMap<>(items));
    }
}

@Service
class StorageService {
    private final Map<UUID, Product> products = new HashMap<>();
    private final Map<UUID, Article> articles = new HashMap<>();

    public StorageService() {
        initializeTestData();
    }

    private void initializeTestData() {
        UUID smartphoneId = UUID.randomUUID();
        UUID laptopId = UUID.randomUUID();
        UUID headphonesId = UUID.randomUUID();

        products.put(smartphoneId, new Product(smartphoneId, "Смартфон Samsung Galaxy", 50000.0));
        products.put(laptopId, new Product(laptopId, "Ноутбук игровой ASUS", 120000.0));
        products.put(headphonesId, new Product(headphonesId, "Наушники беспроводные Sony", 15000.0));

        articles.put(UUID.randomUUID(), new Article(UUID.randomUUID(),
                "Обзор нового смартфона Samsung", "Полный обзор..."));
        articles.put(UUID.randomUUID(), new Article(UUID.randomUUID(),
                "Лучшие игровые ноутбуки 2024", "Топ-10 игровых ноутбуков..."));
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

    public Optional<Product> getProductById(UUID id) {
        return Optional.ofNullable(products.get(id));
    }
}

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

@Service
class BasketService {
    private final ProductBasket productBasket;
    private final StorageService storageService;

    public BasketService(ProductBasket productBasket, StorageService storageService) {
        this.productBasket = productBasket;
        this.storageService = storageService;
    }

    public void addProductToBasket(UUID productId) {
        Optional<Product> product = storageService.getProductById(productId);
        if (product.isEmpty()) {
            throw new NoSuchProductException("Продукт с ID '" + productId + "' не найден");
        }
        productBasket.addProduct(productId);
    }

    public UserBasket getUserBasket() {
        Map<UUID, Integer> basketItems = productBasket.getItems();

        List<BasketItem> items = basketItems.entrySet().stream()
                .map(entry -> {
                    UUID productId = entry.getKey();
                    Integer quantity = entry.getValue();
                    Product product = storageService.getProductById(productId)
                            .orElseThrow(() -> new NoSuchProductException("Продукт с ID '" + productId + "' не найден"));
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

    @GetMapping("/basket/{id}")
    public String addProduct(@PathVariable("id") UUID id) {
        basketService.addProductToBasket(id);
        return "Продукт успешно добавлен";
    }

    @GetMapping("/basket")
    public UserBasket getUserBasket() {
        return basketService.getUserBasket();
    }
}

// ==================== ТЕСТЫ ====================

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
        basketItems.put(productId1, 2);
        basketItems.put(productId2, 1);

        Mockito.when(productBasket.getItems()).thenReturn(basketItems);
        Mockito.when(storageService.getProductById(productId1)).thenReturn(Optional.of(product1));
        Mockito.when(storageService.getProductById(productId2)).thenReturn(Optional.of(product2));

        // Act
        UserBasket userBasket = basketService.getUserBasket();

        // Assert
        assert userBasket.getItems().size() == 2 : "Должно быть 2 элемента в корзине";
        assert userBasket.getTotal() == 4000.0 : "Общая стоимость должна быть 4000";

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
        basketItems.put(nonExistentProductId, 1);

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
            assert e.getMessage().contains(nonExistentProductId.toString()) :
                    "Сообщение об ошибке должно содержать ID несуществующего продукта";
        }
    }
}