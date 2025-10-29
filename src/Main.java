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