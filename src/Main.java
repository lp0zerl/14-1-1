// SkyshopApplication.java
package org.skypro.skyshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
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
        products.put(UUID.randomUUID(), new Product(UUID.randomUUID(), "Смартфон Samsung Galaxy", 50000.0));
        products.put(UUID.randomUUID(), new Product(UUID.randomUUID(), "Ноутбук игровой ASUS", 120000.0));
        products.put(UUID.randomUUID(), new Product(UUID.randomUUID(), "Наушники беспроводные Sony", 15000.0));
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

// ==================== КОНТРОЛЛЕР ====================

@RestController
class ShopController {
    private final StorageService storageService;
    private final SearchService searchService;

    public ShopController(StorageService storageService, SearchService searchService) {
        this.storageService = storageService;
        this.searchService = searchService;
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
}