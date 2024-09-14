package mealplanner;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.sql.*;
import java.io.File;

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
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS meals ("
                + "category VARCHAR(20),"
                + "meal VARCHAR(255),"
                + "meal_id INTEGER PRIMARY KEY"
                + ")");

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS ingredients ("
                + "ingredient VARCHAR(255),"
                + "ingredient_id INTEGER,"
                + "meal_id INTEGER REFERENCES meals,"
                + "PRIMARY KEY (ingredient, ingredient_id, meal_id)"
                + ")");

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS plan ("
                + "meal_option VARCHAR(20),"
                + "meal_category VARCHAR(20),"
                + "meal_id INTEGER REFERENCES meals,"
                + "PRIMARY KEY (meal_option, meal_category)"
                + ")");

        statement.close();
    }

    public boolean planExists() throws SQLException {
        Statement statement = this.conn.createStatement();
        ResultSet plannedMeals = statement.executeQuery("SELECT * FROM plan");
        if (plannedMeals.next()) {
            return true;
        }

        return false;
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
        String insertQuery = "INSERT INTO meals (category, meal, meal_id) VALUES (?, ?, ?)";

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
        String ingredientInsert = "INSERT INTO ingredients(ingredient, ingredient_id, meal_id) VALUES (?, ?, ?)";
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

    public void showMeals(String category) throws SQLException {
        String categorySelect = "SELECT * FROM meals WHERE category = ?";
        PreparedStatement ps = this.conn.prepareStatement(categorySelect);
        ps.setObject(1, category);
        ResultSet allMeals = ps.executeQuery();

        if (!allMeals.isBeforeFirst()) {
            System.out.println("No meals found.");
            return;
        }

        ArrayList<Meal> tempMeals = new ArrayList<>();
        while (allMeals.next()) {
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
        ps.close();

        System.out.println("Category: " + category);
        for (Meal meal : tempMeals) {
            meal.showMeal();
        }
    }

    public ArrayList<String> listMeals(String category) throws SQLException {
        String categorySelect = "SELECT * FROM meals WHERE category = ? ORDER BY meal ASC";
        PreparedStatement ps = this.conn.prepareStatement(categorySelect);
        ps.setObject(1, category);
        ResultSet allMeals = ps.executeQuery();

        ArrayList<String> meals = new ArrayList<>();

        if (!allMeals.isBeforeFirst()) {
            System.out.println("No meals found.");
            return meals;
        }

        while (allMeals.next()) {
            String name = allMeals.getString("meal");
            meals.add(name);
            System.out.println(name);
        }

        allMeals.close();
        ps.close();

        return meals;
    }

    public void setPlanDay(String day, String category, String name) throws SQLException {
        String mealFinder = "SELECT * FROM meals WHERE category = ? AND meal = ?";
        PreparedStatement findMealId = this.conn.prepareStatement(mealFinder);
        findMealId.setObject(1, category);
        findMealId.setObject(2, name);

        ResultSet allMeals = findMealId.executeQuery();
        allMeals.next();
        int id = allMeals.getInt("meal_id");

        allMeals.close();
        findMealId.close();

        String insertToPlan = "INSERT INTO plan (meal_option, meal_category, meal_id) VALUES (?, ?, ?)";
        PreparedStatement ps = this.conn.prepareStatement(insertToPlan);
        ps.setObject(1, day);
        ps.setObject(2, category);
        ps.setObject(3, id);

        ps.executeUpdate();
    }

    public void showPlan(String day) throws SQLException {
        String planFinder = "SELECT * FROM plan WHERE meal_option = ?";
        PreparedStatement ps = this.conn.prepareStatement(planFinder);
        ps.setObject(1, day);
        ResultSet dayMeals = ps.executeQuery();

        System.out.println(day);
        while (dayMeals.next()) {
            String category = dayMeals.getString("meal_category");
            int id = dayMeals.getInt("meal_id");

            String matchMeal = "SELECT meal FROM meals WHERE meal_id = ? AND category = ?";
            PreparedStatement matcher = this.conn.prepareStatement(matchMeal);
            matcher.setObject(1, id);
            matcher.setObject(2, category);

            ResultSet foundMeal = matcher.executeQuery();
            foundMeal.next();

            System.out.printf("%s: %s\n", category, foundMeal.getString("meal"));

            foundMeal.close();
            matcher.close();
        }

        ps.close();
        dayMeals.close();
        System.out.println();
    }

    public void createShoppingList(String filename) throws SQLException, RuntimeException, FileNotFoundException {
        Statement statement = this.conn.createStatement();
        ResultSet ingredientsList = statement.executeQuery("SELECT ingredients.ingredient, COUNT(ingredients.ingredient) AS numIngredients "
                                                            + "FROM plan "
                                                            + "JOIN meals ON plan.meal_id = meals.meal_id "
                                                            + "JOIN ingredients ON meals.meal_id = ingredients.meal_id "
                                                            + "GROUP BY ingredients.ingredient");

        File file = new File(filename);
        PrintWriter pw = new PrintWriter(file);

        while (ingredientsList.next()) {
            String ingredient = ingredientsList.getString("ingredient");
            int numIngredient = ingredientsList.getInt("numIngredients");
            if (numIngredient >= 2) {
                pw.printf("%s x%d\n", ingredient, numIngredient);
            } else {
                pw.println(ingredient);
            }
        }

        statement.close();
        ingredientsList.close();
        pw.close();
    }
}
