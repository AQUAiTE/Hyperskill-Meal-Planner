package mealplanner;

import java.util.ArrayList;
import java.util.List;

public class Meals {
    private List<Meal> meals;

    public Meals() {
        this.meals = new ArrayList<>();
    }

    public int size() {
        return this.meals.size();
    }

    public void addMeal(Meal meal) {
        this.meals.add(meal);
    }

    public void showMeals() {
        for (Meal meal : this.meals) {
            meal.showMeal();
        }
    }

    public Meal getMealFromId(int id) {
        for (Meal meal : this.meals) {
            if (meal.getMealId() == id) {
                return meal;
            }
        }

        return null;
    }
}
