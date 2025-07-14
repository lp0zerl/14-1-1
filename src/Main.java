import java.util.*;

public class Main {
    public static void main(String[] args) {
        // === Проверка: создание неправильных продуктов ===
        try {
            Product p1 = new SimpleProduct("   ", 100);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка создания продукта: " + e.getMessage());
        }

        try {
            Product p2 = new SimpleProduct("Bread", 0);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка создания продукта: " + e.getMessage());
        }

        try {
            Product p3 = new DiscountedProduct("Juice", 100, 120);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка создания продукта: " + e.getMessage());
        }

        // === Создание корректных объектов ===
        Product apple = new SimpleProduct("Apple", 100);
        Product banana = new DiscountedProduct("Banana", 200, 20);
        Product chocolate = new FixPriceProduct("Chocolate");

        Article article1 = new Article("Про яблоки", "Яблоки полезны, яблоки сладкие");
        Article article2 = new Article("Шоколад", "Шоколад поднимает настроение. Шоколад вкусный!");

        SearchEngine engine = new SearchEngine(10);
        engine.add(apple);
        engine.add(banana);
        engine.add(chocolate);
        engine.add(article1);
        engine.add(article2);

        // === Поиск самых подходящих результатов ===
        try {
            Searchable best = engine.findBest("яблоки");
            System.out.println("Лучший результат для 'яблоки':");
            System.out.println(best.getStringRepresentation());
        } catch (BestResultNotFound e) {
            System.out.println("Ошибка поиска: " + e.getMessage());
        }

        try {
            Searchable best = engine.findBest("ананас");
            System.out.println("Лучший результат для 'ананас':");
            System.out.println(best.getStringRepresentation());
        } catch (BestResultNotFound e) {
            System.out.println("Ошибка поиска: " + e.getMessage());
        }
    }
}

// === Интерфейс ===
interface Searchable {
    String getSearchTerm();
    String getType();
    String getName();

    default String getStringRepresentation() {
        return getName() + " — " + getType();
    }
}

// === Абстрактный класс Product ===
abstract class Product implements Searchable {
    private final String name;

    public Product(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название продукта не может быть пустым или пробельным.");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract int getPrice();

    public abstract boolean isSpecial();

    @Override
    public String getSearchTerm() {
        return name;
    }

    @Override
    public String getType() {
        return "PRODUCT";
    }

    @Override
    public String toString() {
        return name + ": " + getPrice();
    }
}

// === Простые продукты ===
class SimpleProduct extends Product {
    private final int price;

    public SimpleProduct(String name, int price) {
        super(name);
        if (price <= 0) {
            throw new IllegalArgumentException("Цена продукта должна быть строго больше 0.");
        }
        this.price = price;
    }

    public int getPrice() {
        return price;
    }

    public boolean isSpecial() {
        return false;
    }
}

// === Товары со скидкой ===
class DiscountedProduct extends Product {
    private final int basePrice;
    private final int discountPercent;

    public DiscountedProduct(String name, int basePrice, int discountPercent) {
        super(name);
        if (basePrice <= 0) {
            throw new IllegalArgumentException("Базовая цена должна быть строго больше 0.");
        }
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Процент скидки должен быть от 0 до 100 включительно.");
        }
        this.basePrice = basePrice;
        this.discountPercent = discountPercent;
    }

    public int getPrice() {
        return basePrice * (100 - discountPercent) / 100;
    }

    public boolean isSpecial() {
        return true;
    }

    @Override
    public String toString() {
        return getName() + ": " + getPrice() + " (" + discountPercent + "% скидка)";
    }
}

// === Фиксированная цена ===
class FixPriceProduct extends Product {
    private static final int FIXED_PRICE = 300;

    public FixPriceProduct(String name) {
        super(name);
    }

    public int getPrice() {
        return FIXED_PRICE;
    }

    public boolean isSpecial() {
        return true;
    }

    @Override
    public String toString() {
        return getName() + ": Фиксированная цена " + FIXED_PRICE;
    }
}

// === Статьи ===
class Article implements Searchable {
    private final String title;
    private final String text;

    public Article(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getSearchTerm() {
        return title + " " + text;
    }

    public String getType() {
        return "ARTICLE";
    }

    public String getName() {
        return title;
    }

    public String toString() {
        return title + "\n" + text;
    }
}

// === Исключение BestResultNotFound ===
class BestResultNotFound extends Exception {
    public BestResultNotFound(String searchTerm) {
        super("Не найден подходящий результат для запроса: \"" + searchTerm + "\"");
    }
}

// === Поисковая система ===
class SearchEngine {
    private final Searchable[] elements;
    private int size = 0;

    public SearchEngine(int capacity) {
        elements = new Searchable[capacity];
    }

    public void add(Searchable s) {
        if (size < elements.length) {
            elements[size++] = s;
        }
    }

    public Searchable[] search(String keyword) {
        Searchable[] results = new Searchable[5];
        int count = 0;
        for (int i = 0; i < size; i++) {
            if (elements[i].getSearchTerm().toLowerCase().contains(keyword.toLowerCase())) {
                results[count++] = elements[i];
                if (count == 5) break;
            }
        }
        return results;
    }

    public Searchable findBest(String keyword) throws BestResultNotFound {
        int maxCount = 0;
        Searchable best = null;
        for (int i = 0; i < size; i++) {
            int count = countOccurrences(elements[i].getSearchTerm().toLowerCase(), keyword.toLowerCase());
            if (count > maxCount) {
                maxCount = count;
                best = elements[i];
            }
        }
        if (best == null) {
            throw new BestResultNotFound(keyword);
        }
        return best;
    }

    private int countOccurrences(String text, String sub) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(sub, index)) != -1) {
            count++;
            index += sub.length();
        }
        return count;
    }
}
