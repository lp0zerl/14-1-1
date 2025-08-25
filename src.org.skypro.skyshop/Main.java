import org.skypro.skyshop.product.Product;
import product.SimpleProduct;

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


}
