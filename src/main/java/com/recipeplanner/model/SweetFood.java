package com.recipeplanner.model;

public class SweetFood extends Recipe{
    private double sugarContent,carbs;
    private int calories;

    public SweetFood(String title, String description, Category category, double sugarContent, int calories, double carbs) {
        super(title, description, category);
        this.sugarContent = sugarContent;
        this.calories = calories;
        this.carbs = carbs;
    }

    public double getSugarContent() {
        return sugarContent;
    }

    public void setSugarContent(double sugarContent) {
        this.sugarContent = sugarContent;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    @Override
    public String getNutritionSummary(){
        return "Sugar: " + sugarContent+ "g, Calories: " +calories
                + ", Carbs: " +carbs + "g";
    }
}
