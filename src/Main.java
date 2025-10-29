// ==================== ПАКЕТ search ====================
// === ОТДЕЛЬНЫЙ ФАЙЛ: Searchable.java ===
// package search;

interface Searchable {
    String getName();
    String toString();
}

// ==================== ПАКЕТ products ====================
// === ОТДЕЛЬНЫЙ ФАЙЛ: Product.java ===
// package products;

// import search.Searchable;

import java.util.Objects;

class Product implements Searchable {
    private String name;
    private double price;

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return name + " (" + price + " руб.)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(name, product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

// ==================== ПАКЕТ articles ====================
// === ОТДЕЛЬНЫЙ ФАЙЛ: Article.java ===
// package articles;

// import search.Searchable;

import java.util.Objects;

class Article implements Searchable {
    private String name;
    private String content;

    public Article(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Статья: " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return Objects.equals(name, article.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

// ==================== ПАКЕТ search ====================
// === ОТДЕЛЬНЫЙ ФАЙЛ: SearchEngine.java ===
// package search;

// import products.Product;
// import articles.Article;

import java.util.*;
        import java.util.stream.Collectors;

class SearchEngine {
    private Set<Searchable> searchableItems;

    public SearchEngine() {
        this.searchableItems = new HashSet<>();
    }

    public void addProduct(Product product) {
        searchableItems.add(product);
    }

    public void addArticle(Article article) {
        searchableItems.add(article);
    }

    // ПЕРЕПИСАНО: использование Stream API вместо циклов
    public Set<Searchable> search(String query) {
        String lowerQuery = query.toLowerCase();

        // Компаратор для сортировки: сначала по длине имени (убывание), затем по алфавиту
        Comparator<Searchable> comparator = (item1, item2) -> {
            int lengthCompare = Integer.compare(item2.getName().length(), item1.getName().length());
            if (lengthCompare != 0) {
                return lengthCompare;
            }
            return item1.getName().compareTo(item2.getName());
        };

        // Использование Stream API: filter и collect с TreeSet
        return searchableItems.stream()
                .filter(item -> item.getName().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toCollection(() -> new TreeSet<>(comparator)));
    }
}

// ==================== ПАКЕТ products ====================
// === ОТДЕЛЬНЫЙ ФАЙЛ: ProductBasket.java ===
// package products;

import java.util.*;
        import java.util.stream.Collectors;

class ProductBasket {
    private Map<String, List<Product>> productsMap;

    public ProductBasket() {
        this.productsMap = new HashMap<>();
    }

    public void addProduct(Product product) {
        String productName = product.getName();
        productsMap.putIfAbsent(productName, new ArrayList<>());
        productsMap.get(productName).add(product);
    }

    public List<Product> removeProductsByName(String name) {
        List<Product> removedProducts = productsMap.remove(name);
        return removedProducts != null ? removedProducts : new ArrayList<>();
    }

    // ПЕРЕПИСАНО: использование Stream API для вычисления общей стоимости
    public double getTotalPrice() {
        return productsMap.values().stream()
                .flatMap(List::stream) // Преобразование Stream<List<Product>> в Stream<Product>
                .mapToDouble(Product::getPrice) // Преобразование в DoubleStream
                .sum(); // Суммирование всех цен
    }

    // ПЕРЕПИСАНО: использование Stream API для вывода корзины
    public void printBasket() {
        if (productsMap.isEmpty()) {
            System.out.println("Корзина пуста");
            return;
        }

        System.out.println("Содержимое корзины:");
        // Использование flatMap для преобразования вложенных списков в единый Stream
        productsMap.values().stream()
                .flatMap(List::stream)
                .forEach(product -> System.out.println("- " + product));

        // Дополнительная информация с использованием Stream API
        long totalItems = productsMap.values().stream()
                .flatMap(List::stream)
                .count();

        long specialItemsCount = getSpecialCount();

        System.out.println("Общее количество товаров: " + totalItems);
        System.out.println("Специальных товаров: " + specialItemsCount);
        System.out.println("Общая стоимость: " + getTotalPrice() + " руб.");
    }

    // ПЕРЕПИСАНО: приватный метод для подсчета специальных товаров с использованием Stream API
    private long getSpecialCount() {
        return productsMap.values().stream()
                .flatMap(List::stream)
                .filter(product -> product.getPrice() > 1000) // Товары дороже 1000 рублей считаем специальными
                .count();
    }
}

// ==================== ПАКЕТ main ====================
// === ОТДЕЛЬНЫЙ ФАЙЛ: Main.java ===
// package main;

// import products.Product;
// import products.ProductBasket;
// import articles.Article;
// import search.SearchEngine;
// import search.Searchable;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        demonstrateStreamAPI();
    }

    private static void demonstrateStreamAPI() {
        System.out.println("=== ДЕМОНСТРАЦИЯ STREAM API ===");

        // Демонстрация ProductBasket с Stream API
        System.out.println("\n--- ProductBasket с Stream API ---");
        ProductBasket basket = new ProductBasket();

        // Добавляем товары
        basket.addProduct(new Product("Смартфон", 25000.0));
        basket.addProduct(new Product("Ноутбук", 75000.0));
        basket.addProduct(new Product("Наушники", 5000.0));
        basket.addProduct(new Product("Чехол для телефона", 800.0));
        basket.addProduct(new Product("Смартфон", 30000.0)); // Другой смартфон
        basket.addProduct(new Product("Планшет", 45000.0));

        basket.printBasket();

        // Демонстрация удаления
        System.out.println("\n--- Удаление смартфонов ---");
        List<Product> removed = basket.removeProductsByName("Смартфон");
        System.out.println("Удалено товаров: " + removed.size());
        removed.forEach(product -> System.out.println("- " + product));

        System.out.println("\n--- Корзина после удаления ---");
        basket.printBasket();

        // Демонстрация SearchEngine с Stream API
        System.out.println("\n--- SearchEngine с Stream API ---");
        SearchEngine searchEngine = new SearchEngine();

        // Добавляем продукты и статьи
        searchEngine.addProduct(new Product("Смартфон Apple iPhone", 80000.0));
        searchEngine.addProduct(new Product("Смартфон Samsung Galaxy", 50000.0));
        searchEngine.addProduct(new Product("Ноутбук игровой", 120000.0));
        searchEngine.addProduct(new Product("Планшет графический", 45000.0));
        searchEngine.addProduct(new Product("Наушники беспроводные", 15000.0));

        searchEngine.addArticle(new Article("Обзор нового смартфона", "Содержание обзора..."));
        searchEngine.addArticle(new Article("Сравнение игровых ноутбуков", "Содержание сравнения..."));
        searchEngine.addArticle(new Article("Лучшие планшеты для рисования", "Содержание статьи..."));

        // Поиск с использованием Stream API
        System.out.println("\n--- Поиск 'смартфон' ---");
        Set<Searchable> smartphoneResults = searchEngine.search("смартфон");
        System.out.println("Найдено: " + smartphoneResults.size());
        smartphoneResults.forEach(System.out::println);

        System.out.println("\n--- Поиск 'ноутбук' ---");
        Set<Searchable> laptopResults = searchEngine.search("ноутбук");
        System.out.println("Найдено: " + laptopResults.size());
        laptopResults.forEach(System.out::println);

        System.out.println("\n--- Поиск 'новый' ---");
        Set<Searchable> newResults = searchEngine.search("новый");
        System.out.println("Найдено: " + newResults.size());
        newResults.forEach(System.out::println);

        // Демонстрация пустого поиска
        System.out.println("\n--- Поиск 'несуществующий' ---");
        Set<Searchable> emptyResults = searchEngine.search("несуществующий");
        System.out.println("Найдено: " + emptyResults.size());

        // Демонстрация всех элементов
        System.out.println("\n--- Все элементы ---");
        Set<Searchable> allResults = searchEngine.search("");
        System.out.println("Всего элементов: " + allResults.size());
        allResults.forEach(item -> System.out.println("- " + item + " (длина: " + item.getName().length() + ")"));
    }
}