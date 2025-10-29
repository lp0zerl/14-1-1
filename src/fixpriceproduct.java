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