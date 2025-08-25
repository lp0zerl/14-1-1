public class Main {
    public static void main(String[] args) {
        Product apple = new Product("Apple", 100);
        Product banana = new Product("Banana", 80);
        Product orange = new Product("Orange", 120);
        Product milk = new Product("Milk", 150);
        Product bread = new Product("Bread", 90);
        Product cheese = new Product("Cheese", 200);

        ProductBasket basket = new ProductBasket();

        basket.addProduct(apple);
        basket.addProduct(banana);
        basket.addProduct(orange);
        basket.addProduct(milk);
        basket.addProduct(bread);

        basket.addProduct(cheese);

        basket.printBasket();

        System.out.println("Общая стоимость: " + basket.getTotalPrice());

        System.out.println("Есть ли Milk в корзине? " + basket.hasProduct("Milk"));

        System.out.println("Есть ли Juice в корзине? " + basket.hasProduct("Juice"));

        basket.clear();

        basket.printBasket();

        System.out.println("Общая стоимость: " + basket.getTotalPrice());

        System.out.println("Есть ли Apple в корзине? " + basket.hasProduct("Apple"));
    }
}
