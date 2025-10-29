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

    // Реализация equals и hashCode только по имени
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

    // Реализация equals и hashCode только по имени
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

class SearchEngine {
    // ЗАМЕНА: List на Set для предотвращения дубликатов
    private Set<Searchable> searchableItems;

    public SearchEngine() {
        this.searchableItems = new HashSet<>(); // HashSet для уникальности
    }

    public void addProduct(Product product) {
        searchableItems.add(product); // Set автоматически предотвратит дубликаты
    }

    public void addArticle(Article article) {
        searchableItems.add(article); // Set автоматически предотвратит дубликаты
    }

    // ЗАМЕНА: возвращаем отсортированный Set вместо Map
    public Set<Searchable> search(String query) {
        // Компаратор для сортировки: сначала по длине имени (убывание), затем по алфавиту
        Comparator<Searchable> comparator = (item1, item2) -> {
            int lengthCompare = Integer.compare(item2.getName().length(), item1.getName().length());
            if (lengthCompare != 0) {
                return lengthCompare; // Сначала сортируем по убыванию длины
            }
            return item1.getName().compareTo(item2.getName()); // При равной длине - по алфавиту
        };

        Set<Searchable> results = new TreeSet<>(comparator);
        String lowerQuery = query.toLowerCase();

        for (Searchable item : searchableItems) {
            if (item.getName().toLowerCase().contains(lowerQuery)) {
                results.add(item);
            }
        }

        return results;
    }
}

// ==================== ПАКЕТ main ====================
// === ОТДЕЛЬНЫЙ ФАЙЛ: Main.java ===
// package main;

// import products.Product;
// import articles.Article;
// import search.SearchEngine;
// import search.Searchable;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        demonstrateNoDuplicates();
        demonstrateSearchWithComparator();
    }

    private static void demonstrateNoDuplicates() {
        System.out.println("=== ДЕМОНСТРАЦИЯ ОТСУТСТВИЯ ДУБЛИКАТОВ ===");

        SearchEngine searchEngine = new SearchEngine();

        // Пытаемся добавить продукты с одинаковыми именами
        searchEngine.addProduct(new Product("Смартфон", 15000.0));
        searchEngine.addProduct(new Product("Смартфон", 16000.0)); // Дубликат - не добавится
        searchEngine.addProduct(new Product("Ноутбук", 50000.0));
        searchEngine.addProduct(new Product("Ноутбук", 55000.0)); // Дубликат - не добавится

        // Пытаемся добавить статьи с одинаковыми именами
        searchEngine.addArticle(new Article("Обзор смартфона", "Содержание обзора..."));
        searchEngine.addArticle(new Article("Обзор смартфона", "Другое содержание...")); // Дубликат - не добавится

        // Поиск покажет только уникальные элементы
        System.out.println("\n--- Результаты поиска 'смартфон' ---");
        Set<Searchable> results = searchEngine.search("смартфон");
        System.out.println("Найдено уникальных элементов: " + results.size());
        for (Searchable item : results) {
            System.out.println("- " + item);
        }
    }

    private static void demonstrateSearchWithComparator() {
        System.out.println("\n\n=== ДЕМОНСТРАЦИЯ РАБОТЫ КОМПАРАТОРА ===");

        SearchEngine searchEngine = new SearchEngine();

        // Добавляем элементы с разной длиной имен для демонстрации сортировки
        searchEngine.addProduct(new Product("Смартфон новый флагман", 80000.0)); // Длинное имя
        searchEngine.addProduct(new Product("Телевизор", 45000.0)); // Короткое имя
        searchEngine.addProduct(new Product("Ноутбук игровой мощный", 120000.0)); // Длинное имя
        searchEngine.addProduct(new Product("Планшет", 25000.0)); // Короткое имя
        searchEngine.addProduct(new Product("Наушники беспроводные", 8000.0)); // Средняя длина
        searchEngine.addProduct(new Product("Часы умные", 15000.0)); // Средняя длина

        // Статьи с разной длиной названий
        searchEngine.addArticle(new Article("Обзор нового флагманского смартфона", "Содержание..."));
        searchEngine.addArticle(new Article("Сравнение телевизоров", "Содержание..."));
        searchEngine.addArticle(new Article("Лучшие игровые ноутбуки 2024 года", "Содержание..."));

        System.out.println("\n--- Результаты поиска (все элементы) ---");
        Set<Searchable> allResults = searchEngine.search("");
        System.out.println("Всего элементов: " + allResults.size());
        for (Searchable item : allResults) {
            System.out.println("- " + item + " (длина имени: " + item.getName().length() + ")");
        }

        System.out.println("\n--- Результаты поиска 'новый' ---");
        Set<Searchable> newResults = searchEngine.search("новый");
        for (Searchable item : newResults) {
            System.out.println("- " + item);
        }

        // Демонстрация сортировки при равной длине имен
        System.out.println("\n--- Демонстрация сортировки при равной длине ---");
        SearchEngine equalLengthDemo = new SearchEngine();
        equalLengthDemo.addProduct(new Product("Банан", 100.0));
        equalLengthDemo.addProduct(new Product("Ананас", 200.0));
        equalLengthDemo.addProduct(new Product("Яблоко", 150.0));
        equalLengthDemo.addProduct(new Product("Манго", 300.0));

        Set<Searchable> equalLengthResults = equalLengthDemo.search("");
        System.out.println("Товары с одинаковой длиной имени (5 символов):");
        for (Searchable item : equalLengthResults) {
            System.out.println("- " + item + " (длина: " + item.getName().length() + ")");
        }

        // Демонстрация смешанной сортировки
        System.out.println("\n--- Смешанная демонстрация сортировки ---");
        SearchEngine mixedDemo = new SearchEngine();
        mixedDemo.addProduct(new Product("А", 100.0));          // 1 символ
        mixedDemo.addProduct(new Product("Яблоко", 150.0));     // 6 символов
        mixedDemo.addProduct(new Product("Банан", 100.0));      // 5 символов
        mixedDemo.addProduct(new Product("Арбуз большой", 200.0)); // 13 символов
        mixedDemo.addProduct(new Product("Груша", 120.0));      // 5 символов
        mixedDemo.addProduct(new Product("Ананас", 200.0));     // 6 символов

        Set<Searchable> mixedResults = mixedDemo.search("");
        for (Searchable item : mixedResults) {
            System.out.println("- " + item + " (длина: " + item.getName().length() + ")");
        }
    }
}