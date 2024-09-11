package mealplanner;

import java.util.*;
import java.sql.*;

public class DBManager {
    private final Connection conn;

    public DBManager() throws SQLException {
        this.conn = connectToDB();
        this.createTables();
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

    public int getNextId() throws SQLException {
        int id;
        Statement statement = this.conn.createStatement();
        ResultSet ids = statement.executeQuery("SELECT COALESCE(MAX(meal_id), 0) + 1 FROM meals");
        if (ids.next()) {
            id = ids.getInt(1);
        } else {
            id = 1;
        }

        statement.close();
        ids.close();
        return id;
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

    public void showMeals() throws SQLException {
        Statement statement = this.conn.createStatement();
        ResultSet allMeals = statement.executeQuery("SELECT * FROM meals");
        if (!allMeals.isBeforeFirst()) {
            System.out.println("No meals saved. Add a meal first.");
            return;
        }

        ArrayList<Meal> tempMeals = new ArrayList<>();
        while (allMeals.next()) {
            String category = allMeals.getString("category");
            String name = allMeals.getString("meal");
            int id = allMeals.getInt("meal_id");

            Meal meal = new Meal(category, name, id);
            tempMeals.add(meal);

            ArrayList<String> ingredients = new ArrayList<>();
            Statement statement2 = this.conn.createStatement();
            ResultSet rs = statement2.executeQuery("SELECT * FROM ingredients WHERE meal_id = " + id);

            while (rs.next()) {
                String ingredient = rs.getString("ingredient");
                ingredients.add(ingredient);
            }
            meal.setIngredients(ingredients);
            statement2.close();
            rs.close();
        }

        allMeals.close();
        statement.close();

        for (Meal meal : tempMeals) {
            meal.showMeal();
        }
    }

}
