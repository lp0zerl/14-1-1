
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

class ProductBasket {
    private java.util.List<Product> products;

    public ProductBasket() {
        this.products = new java.util.ArrayList<>();
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public java.util.List<Product> removeProductsByName(String name) {
        java.util.List<Product> removedProducts = new java.util.ArrayList<>();
        java.util.Iterator<Product> iterator = products.iterator();

        while (iterator.hasNext()) {
            Product product = iterator.next();
            if (product.getName().equals(name)) {
                removedProducts.add(product);
                iterator.remove();
            }
        }

        return removedProducts;
    }

    public void printBasket() {
        if (products.isEmpty()) {
            System.out.println("Корзина пуста");
            return;
        }

        System.out.println("Содержимое корзины:");
        for (Product product : products) {
            System.out.println("- " + product);
        }
    }
}

// ==================== ПАКЕТ search ====================
// === ОТДЕЛЬНЫЙ ФАЙЛ: SearchEngine.java ===
// package search;

class SearchEngine {
    private java.util.List<Product> products;

    public SearchEngine() {
        this.products = new java.util.ArrayList<>();
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public java.util.List<Product> search(String query) {
        java.util.List<Product> results = new java.util.ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Product product : products) {
            if (product.getName().toLowerCase().contains(lowerQuery)) {
                results.add(product);
            }
        }

        return results;
    }
}

// ==================== ПАКЕТ main ====================
// === ОТДЕЛЬНЫЙ ФАЙЛ: Main.java ===
// package main;

public class Main {
    public static void main(String[] args) {
        demonstrateProductBasket();
        demonstrateSearchEngine();
    }

    private static void demonstrateProductBasket() {
        System.out.println("=== ДЕМОНСТРАЦИЯ РАБОТЫ КОРЗИНЫ ===");

        // Создаем корзину и добавляем продукты
        ProductBasket basket = new ProductBasket();
        basket.addProduct(new Product("Яблоки", 150.0));
        basket.addProduct(new Product("Бананы", 80.0));
        basket.addProduct(new Product("Яблоки", 120.0));
        basket.addProduct(new Product("Апельсины", 200.0));

        System.out.println("\n--- Исходная корзина ---");
        basket.printBasket();

        // Демонстрация удаления существующего продукта
        System.out.println("\n--- Удаление яблок ---");
        java.util.List<Product> removed = basket.removeProductsByName("Яблоки");
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
        java.util.List<Product> removed2 = basket.removeProductsByName("Груши");
        if (removed2.isEmpty()) {
            System.out.println("Список удаленных продуктов пуст");
        }

        System.out.println("\n--- Корзина после второй попытки удаления ---");
        basket.printBasket();
    }

    private static void demonstrateSearchEngine() {
        System.out.println("\n\n=== ДЕМОНСТРАЦИЯ РАБОТЫ ПОИСКОВОГО ДВИЖКА ===");

        // Демонстрация работы поискового движка
        SearchEngine searchEngine = new SearchEngine();
        searchEngine.addProduct(new Product("Смартфон Samsung", 15000.0));
        searchEngine.addProduct(new Product("Наушники Sony", 5000.0));
        searchEngine.addProduct(new Product("Чехол для смартфона", 800.0));
        searchEngine.addProduct(new Product("Смартфон Xiaomi", 12000.0));
        searchEngine.addProduct(new Product("Телевизор LG", 35000.0));
        searchEngine.addProduct(new Product("Смартфон Apple iPhone", 80000.0));

        System.out.println("\n--- Результаты поиска 'Смартфон' ---");
        java.util.List<Product> searchResults = searchEngine.search("Смартфон");
        System.out.println("Найдено товаров: " + searchResults.size());
        for (Product product : searchResults) {
            System.out.println("- " + product);
        }

        System.out.println("\n--- Результаты поиска 'Sony' ---");
        java.util.List<Product> sonyResults = searchEngine.search("Sony");
        System.out.println("Найдено товаров: " + sonyResults.size());
        for (Product product : sonyResults) {
            System.out.println("- " + product);
        }

        System.out.println("\n--- Результаты поиска 'Телевизор' ---");
        java.util.List<Product> tvResults = searchEngine.search("Телевизор");
        System.out.println("Найдено товаров: " + tvResults.size());
        for (Product product : tvResults) {
            System.out.println("- " + product);
        }

        System.out.println("\n--- Результаты поиска 'Несуществующий' ---");
        java.util.List<Product> emptyResults = searchEngine.search("Несуществующий");
        System.out.println("Найдено товаров: " + emptyResults.size());
        if (emptyResults.isEmpty()) {
            System.out.println("Товары не найдены");
        }
    }
}