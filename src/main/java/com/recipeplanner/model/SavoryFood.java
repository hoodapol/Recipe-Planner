package com.recipeplanner.model;

public class SavoryFood extends NutritionalRecipe {
    public enum SpiceLevel {
        MILD, MEDIUM, HOT, EXTRAHOT
    }
    private SpiceLevel spiceLevel;
    private double protein,sodium;

    public SavoryFood(String title, String description, Category category, SpiceLevel spicelevel, double protein, double sodium) {
        super(title, description, category);
        this.spiceLevel = spicelevel;
        this.protein = protein;
        this.sodium = sodium;
    }

    public SpiceLevel getSpicelevel() {
        return spiceLevel;
    }

    public void setSpicelevel(SpiceLevel spicelevel) {
        this.spiceLevel = spicelevel;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getSodium() {
        return sodium;
    }

    public void setSodium(double sodium) {
        this.sodium = sodium;
    }

    @Override
    public String getNutritionSummary(){
        return "Spice Level: " + spiceLevel + ", Protein: " + protein
                + "g, Sodium: " + sodium + "mg";
    }
}