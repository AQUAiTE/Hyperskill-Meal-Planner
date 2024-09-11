package mealplanner;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Meal {
    private int mealId;
    private String name;
    private String category;
    private ArrayList<String> ingredients;

    public Meal(String category, String name, int mealId) {
        this.category = category;
        this.name = name;
        this.mealId = mealId;
    }

    public Meal(String category, String name, int mealId, ArrayList<String> ingredients) {
        this.category = category;
        this.name = name;
        this.mealId = mealId;
        this.ingredients = ingredients;
    }

    public int getMealId() {
        return this.mealId;
    }

    public String getName() {
        return this.name;
    }

    public String getCategory() {
        return this.category;
    }

    public ArrayList<String> getIngredients() {
        return this.ingredients;
    }

    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }

    public void showMeal() {
        System.out.println("Name: " + this.name);
        System.out.println("Ingredients:");
        for (String ingredient : this.ingredients) {
            System.out.println(ingredient.trim());
        }
        System.out.println();
    }

}
