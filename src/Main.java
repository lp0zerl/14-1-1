public class Main {
    public static void main(String[] args) {
        Product apple = new SimpleProduct("Apple", 100);
        Product banana = new DiscountedProduct("Banana", 200, 20);
        Product chocolate = new FixPriceProduct("Chocolate");
        Product milk = new DiscountedProduct("Milk", 150, 10);
        Product tea = new FixPriceProduct("Tea");

        ProductBasket basket = new ProductBasket();

        basket.addProduct(apple);
        basket.addProduct(banana);
        basket.addProduct(chocolate);
        basket.addProduct(milk);
        basket.addProduct(tea);

        Product extra = new SimpleProduct("Extra", 99);
        basket.addProduct(extra);

        basket.printBasket();

        System.out.println("Общая стоимость: " + basket.getTotalPrice());
        System.out.println("Есть ли Milk? " + basket.hasProduct("Milk"));
        System.out.println("Есть ли Coffee? " + basket.hasProduct("Coffee"));

        basket.clear();
        basket.printBasket();
    }
}

abstract class Product {
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
    public abstract String toString();
}

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public String toString() {
        return getName() + ": " + getPrice();
    }
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

        int specialCount = 0;
        for (Product product : products) {
            if (product != null) {
                System.out.println(product.toString());
                if (product.isSpecial()) {
                    specialCount++;
                }
            }
        }

        System.out.println("Итого: " + getTotalPrice());
        System.out.println("Специальных товаров: " + specialCount);
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
