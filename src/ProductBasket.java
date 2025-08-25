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
