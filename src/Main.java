// ==================== ПАКЕТ products ====================
// === ОТДЕЛЬНЫЙ ФАЙЛ: Product.java ===
// package products;

class Product {
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
}

// === ОТДЕЛЬНЫЙ ФАЙЛ: ProductBasket.java ===
// package products;

import java.util.*;

class ProductBasket {
    // ЗАМЕНА: List на Map, где ключ - имя продукта, значение - список продуктов с этим именем
    private Map<String, List<Product>> productsMap;

    public ProductBasket() {
        this.productsMap = new HashMap<>();
    }

    public void addProduct(Product product) {
        String productName = product.getName();
        // Если продукт с таким именем еще не добавлен, создаем новый список
        productsMap.putIfAbsent(productName, new ArrayList<>());
        // Добавляем продукт в список по соответствующему имени
        productsMap.get(productName).add(product);
    }

    public List<Product> removeProductsByName(String name) {
        // Удаляем и возвращаем весь список продуктов с данным именем
        List<Product> removedProducts = productsMap.remove(name);
        return removedProducts != null ? removedProducts : new ArrayList<>();
    }

    public void printBasket() {
        if (productsMap.isEmpty()) {
            System.out.println("Корзина пуста");
            return;
        }

        System.out.println("Содержимое корзины:");
        // Перебираем все значения Map и выводим все продукты
        for (List<Product> productList : productsMap.values()) {
            for (Product product : productList) {
                System.out.println("- " + product);
            }
        }
    }
}

// ==================== ПАКЕТ search ====================
// === ОТДЕЛЬНЫЙ ФАЙЛ: SearchEngine.java ===
// package search;

import java.util.*;

class SearchEngine {
    private List<Product> products;

    public SearchEngine() {
        this.products = new ArrayList<>();
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    // ИЗМЕНЕНИЕ: метод возвращает отсортированную Map вместо List
    public Map<String, Product> search(String query) {
        // Используем TreeMap для автоматической сортировки по ключам (именам продуктов)
        Map<String, Product> results = new TreeMap<>();
        String lowerQuery = query.toLowerCase();

        for (Product product : products) {
            if (product.getName().toLowerCase().contains(lowerQuery)) {
                // Добавляем в Map: ключ - имя продукта, значение - сам продукт
                results.put(product.getName(), product);
            }
        }

        return results;
    }
}

// ==================== ПАКЕТ main ====================
// === ОТДЕЛЬНЫЙ ФАЙЛ: Main.java ===
// package main;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        demonstrateProductBasket();
        demonstrateSearchEngine();
    }

    private static void demonstrateProductBasket() {
        System.out.println("=== ДЕМОНСТРАЦИЯ РАБОТЫ КОРЗИНЫ С MAP ===");

        // Создаем корзину и добавляем продукты
        ProductBasket basket = new ProductBasket();
        basket.addProduct(new Product("Яблоки", 150.0));
        basket.addProduct(new Product("Бананы", 80.0));
        basket.addProduct(new Product("Яблоки", 120.0)); // Дубликат имени
        basket.addProduct(new Product("Апельсины", 200.0));
        basket.addProduct(new Product("Бананы", 90.0)); // Дубликат имени

        System.out.println("\n--- Исходная корзина ---");
        basket.printBasket();

        // Демонстрация удаления существующего продукта
        System.out.println("\n--- Удаление яблок ---");
        List<Product> removed = basket.removeProductsByName("Яблоки");
        if (!removed.isEmpty()) {
            System.out.println("Удаленные продукты:");
            for (Product product : removed) {
                System.out.println("- " + product);
            }
        }

        System.out.println("\n--- Корзина после удаления ---");
        basket.printBasket();

        // Демонстрация удаления несуществующего продукта
        System.out.println("\n--- Попытка удалить груши ---");
        List<Product> removed2 = basket.removeProductsByName("Груши");
        if (removed2.isEmpty()) {
            System.out.println("Список удаленных продуктов пуст");
        }

        System.out.println("\n--- Корзина после второй попытки удаления ---");
        basket.printBasket();
    }

    private static void demonstrateSearchEngine() {
        System.out.println("\n\n=== ДЕМОНСТРАЦИЯ РАБОТЫ ПОИСКОВОГО ДВИЖКА С MAP ===");

        // Демонстрация работы поискового движка
        SearchEngine searchEngine = new SearchEngine();
        searchEngine.addProduct(new Product("Смартфон Samsung", 15000.0));
        searchEngine.addProduct(new Product("Наушники Sony", 5000.0));
        searchEngine.addProduct(new Product("Чехол для смартфона", 800.0));
        searchEngine.addProduct(new Product("Смартфон Xiaomi", 12000.0));
        searchEngine.addProduct(new Product("Телевизор LG", 35000.0));
        searchEngine.addProduct(new Product("Смартфон Apple iPhone", 80000.0));
        searchEngine.addProduct(new Product("Наушники Apple", 15000.0));

        System.out.println("\n--- Результаты поиска 'Смартфон' (отсортированные по имени) ---");
        Map<String, Product> searchResults = searchEngine.search("Смартфон");
        System.out.println("Найдено товаров: " + searchResults.size());
        // TreeMap автоматически сортирует по ключам (именам продуктов)
        for (Map.Entry<String, Product> entry : searchResults.entrySet()) {
            System.out.println("- " + entry.getValue());
        }

        System.out.println("\n--- Результаты поиска 'Наушники' (отсортированные по имени) ---");
        Map<String, Product> headphonesResults = searchEngine.search("Наушники");
        System.out.println("Найдено товаров: " + headphonesResults.size());
        for (Map.Entry<String, Product> entry : headphonesResults.entrySet()) {
            System.out.println("- " + entry.getValue());
        }

        System.out.println("\n--- Результаты поиска 'Apple' (отсортированные по имени) ---");
        Map<String, Product> appleResults = searchEngine.search("Apple");
        System.out.println("Найдено товаров: " + appleResults.size());
        for (Map.Entry<String, Product> entry : appleResults.entrySet()) {
            System.out.println("- " + entry.getValue());
        }

        System.out.println("\n--- Результаты поиска 'Несуществующий' ---");
        Map<String, Product> emptyResults = searchEngine.search("Несуществующий");
        System.out.println("Найдено товаров: " + emptyResults.size());
        if (emptyResults.isEmpty()) {
            System.out.println("Товары не найдены");
        }

        // Демонстрация сортировки - специально добавляем продукты в разном порядке
        System.out.println("\n--- Демонстрация автоматической сортировки ---");
        SearchEngine sortedDemo = new SearchEngine();
        sortedDemo.addProduct(new Product("Зебра", 1000.0));
        sortedDemo.addProduct(new Product("Ананас", 500.0));
        sortedDemo.addProduct(new Product("Манго", 700.0));
        sortedDemo.addProduct(new Product("Банан", 300.0));

        Map<String, Product> allResults = sortedDemo.search("");
        System.out.println("Все товары (автоматически отсортированы по имени):");
        for (Map.Entry<String, Product> entry : allResults.entrySet()) {
            System.out.println("- " + entry.getValue());
        }
    }
}