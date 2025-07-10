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

class Product {
    private final String name;
    private final int price;

    public Product(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }
}

class ProductBasket {
    private final Product[] products = new Product[5];
    private int size = 0;

    public void addProduct(Product product) {
        if (size >= products.length) {
            System.out.println("Невозможно добавить продукт");
            return;
        }
        products[size++] = product;
    }

    public int getTotalPrice() {
        int sum = 0;
        for (Product product : products) {
            if (product != null) {
                sum += product.getPrice();
            }
        }
        return sum;
    }

    public void printBasket() {
        if (size == 0) {
            System.out.println("В корзине пусто");
            return;
        }
        for (Product product : products) {
            if (product != null) {
                System.out.println(product.getName() + ": " + product.getPrice());
            }
        }
        System.out.println("Итого: " + getTotalPrice());
    }

    public boolean hasProduct(String name) {
        for (Product product : products) {
            if (product != null && product.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        for (int i = 0; i < products.length; i++) {
            products[i] = null;
        }
        size = 0;
    }
}
