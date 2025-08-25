package org.skypro.skyshop.product;

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
