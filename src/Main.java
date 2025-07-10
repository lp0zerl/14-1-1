
public class Main {
    public static void main(String[] args) {
        Product apple = new SimpleProduct("Apple", 100);
        Product banana = new DiscountedProduct("Banana", 200, 20);
        Product chocolate = new FixPriceProduct("Chocolate");
        Product milk = new DiscountedProduct("Milk", 150, 10);
        Product tea = new FixPriceProduct("Tea");

        Article article1 = new Article("Как выбрать яблоки", "Яблоки бывают разные. Важно обращать внимание на цвет и запах.");
        Article article2 = new Article("Почему бананы полезны", "Бананы богаты калием и быстро утоляют голод.");
        Article article3 = new Article("Шоколад и настроение", "Шоколад помогает выработке эндорфинов.");
        Article article4 = new Article("Молоко: польза или вред?", "Молоко содержит кальций, но не все его переносят.");

        SearchEngine engine = new SearchEngine(20);

        engine.add(apple);
        engine.add(banana);
        engine.add(chocolate);
        engine.add(milk);
        engine.add(tea);

        engine.add(article1);
        engine.add(article2);
        engine.add(article3);
        engine.add(article4);

        System.out.println("Поиск по слову 'шоко':");
        printResults(engine.search("шоко"));

        System.out.println("\nПоиск по слову 'яблок':");
        printResults(engine.search("яблок"));

        System.out.println("\nПоиск по слову 'Tea':");
        printResults(engine.search("Tea"));

        System.out.println("\nПоиск по слову 'молоко':");
        printResults(engine.search("молоко"));
    }

    private static void printResults(Searchable[] results) {
        for (Searchable item : results) {
            if (item != null) {
                System.out.println(item.getStringRepresentation());
            }
        }
    }
}

interface Searchable {
    String getSearchTerm();
    String getType();
    String getName();

    default String getStringRepresentation() {
        return getName() + " — " + getType();
    }
}

abstract class Product implements Searchable {
    private final String name;

    public Product(String name) {
        this.name = name;
    }

    public abstract int getPrice();
    public abstract boolean isSpecial();

    public String getName() {
        return name;
    }

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

class SimpleProduct extends Product {
    private final int price;

    public SimpleProduct(String name, int price) {
        super(name);
        this.price = price;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public boolean isSpecial() {
        return false;
    }
}

class DiscountedProduct extends Product {
    private final int basePrice;
    private final int discountPercent;

    public DiscountedProduct(String name, int basePrice, int discountPercent) {
        super(name);
        this.basePrice = basePrice;
        this.discountPercent = discountPercent;
    }

    @Override
    public int getPrice() {
        return basePrice * (100 - discountPercent) / 100;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public String toString() {
        return getName() + ": " + getPrice() + " (" + discountPercent + "%)";
    }
}

class FixPriceProduct extends Product {
    private static final int FIXED_PRICE = 300;

    public FixPriceProduct(String name) {
        super(name);
    }

    @Override
    public int getPrice() {
        return FIXED_PRICE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public String toString() {
        return getName() + ": Фиксированная цена " + FIXED_PRICE;
    }
}

class Article implements Searchable {
    private final String title;
    private final String text;

    public Article(String title, String text) {
        this.title = title;
        this.text = text;
    }

    @Override
    public String getSearchTerm() {
        return title + " " + text;
    }

    @Override
    public String getType() {
        return "ARTICLE";
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public String toString() {
        return title + "\n" + text;
    }
}

class SearchEngine {
    private final Searchable[] elements;
    private int size = 0;

    public SearchEngine(int capacity) {
        this.elements = new Searchable[capacity];
    }

    public void add(Searchable searchable) {
        if (size < elements.length) {
            elements[size++] = searchable;
        }
    }

    public Searchable[] search(String keyword) {
        Searchable[] results = new Searchable[5];
        int found = 0;
        for (int i = 0; i < size; i++) {
            if (elements[i].getSearchTerm().toLowerCase().contains(keyword.toLowerCase())) {
                results[found++] = elements[i];
                if (found == 5) break;
            }
        }
        return results;
    }
}
