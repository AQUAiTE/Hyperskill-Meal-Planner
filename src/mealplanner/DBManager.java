package mealplanner;

import java.util.*;
import java.sql.*;

import static mealplanner.Main.meals;

public class DBManager {
    private final Connection conn;

    public DBManager() throws SQLException {
        this.conn = connectToDB();
        this.createTables();
        this.getExistingMeals();
    }

    private Connection connectToDB() throws SQLException {
        final String DB_URL = "jdbc:postgresql:meals_db";
        final String USER = "postgres";
        final String PASS = "1111";

        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    private void createTables() throws SQLException {
        Statement statement = this.conn.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS public.meals ("
                + "category VARCHAR(20),"
                + "meal VARCHAR(30),"
                + "meal_id INTEGER PRIMARY KEY"
                + ")");

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS public.ingredients ("
                + "ingredient VARCHAR(20),"
                + "ingredient_id INTEGER,"
                + "meal_id INTEGER REFERENCES public.meals,"
                + "PRIMARY KEY (ingredient, ingredient_id, meal_id)"
                + ")");

        statement.close();
    }

    private void getExistingMeals() throws SQLException {
        Statement statement = this.conn.createStatement();
        ResultSet dbMeals = statement.executeQuery("SELECT * FROM public.meals");

        // Get all meal category + names first
        while (dbMeals.next()) {
            String category = dbMeals.getString("category");
            String name = dbMeals.getString("meal");

            Meal meal = new Meal(category, name);
            meals.addMeal(meal);
        }
        dbMeals.close();

        // Next match the ingredients to the right meal using id
        ArrayList<String> ingredients = new ArrayList<>();
        int prevId = 0;
        ResultSet dbIngredients = statement.executeQuery("SELECT * FROM public.ingredients ORDER BY meal_id");
        while (dbIngredients.next()) {
            String ingredient = dbIngredients.getString("ingredient");
            int currentId = dbIngredients.getInt("meal_id");

            if ((currentId != prevId && prevId != 0) || dbIngredients.isLast()) {
                if (!dbIngredients.isLast()) {
                    meals.getMealFromId(prevId).setIngredients(ingredients);
                    ingredients = new ArrayList<>();
                } else {
                    ingredients.add(ingredient);
                    meals.getMealFromId(prevId).setIngredients(ingredients);
                    break;
                }
            }

            ingredients.add(ingredient);
            prevId = currentId;
        }

        dbIngredients.close();
        statement.close();

    }

    public void insertMeal(Meal meal) throws SQLException {
        String insertQuery = "INSERT INTO public.meals (category, meal, meal_id) VALUES (?, ?, ?)";

        String category = meal.getCategory();
        String name = meal.getName();
        int id = meal.getMealId();

        PreparedStatement ps = this.conn.prepareStatement(insertQuery);
        ps.setObject(1, category);
        ps.setObject(2, name);
        ps.setObject(3, id);

        ps.executeUpdate();
    }

    public void insertIngredients(Meal meal) throws SQLException {
        String ingredientInsert = "INSERT INTO public.ingredients(ingredient, ingredient_id, meal_id) VALUES (?, ?, ?)";
        PreparedStatement ps = this.conn.prepareStatement(ingredientInsert);
        ArrayList<String> ingredients = meal.getIngredients();
        int mealId = meal.getMealId();

        for (int i = 0; i < ingredients.size(); i++) {
            ps.setObject(1, ingredients.get(i));
            ps.setObject(2, i);
            ps.setObject(3, mealId);

            ps.executeUpdate();
        }
    }

}
