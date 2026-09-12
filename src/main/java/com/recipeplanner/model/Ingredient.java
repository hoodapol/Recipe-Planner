package com.recipeplanner.model;

public class Ingredient {
    private String name,unit;
    private double quantity;

    public Ingredient(String name, double quantity, String unit)
    {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return quantity + " " + unit + " " + name;
    }
}
