package mealplanner;

import java.sql.SQLException;
import java.util.*;

public class Main {
  public static DBManager dbManager = null;
  public static Scanner scanner = new Scanner(System.in);


  public static void main(String[] args) {
    try {
      dbManager = new DBManager();

      userLoop: while (true) {
        // Ask user for action
        System.out.println("What would you like to do (add, show, exit)?");
        String action = scanner.nextLine();

        switch (action) {
          case "show":
            showMeals();
            break;
          case "add":
            addMeal();
            break;
          case "exit":
            break userLoop;
          default:
            // Restarts due to invalid action input
        }
      }
    } catch (SQLException e) {
      System.out.println(e);
      return;
    }

    scanner.close();
    System.out.println("Bye!");
  }

  private static void showMeals throws SQLException {
    System.out.println("Which category do you want to print (breakfast, lunch, dinner)?");
    String category = scanner.nextLine();
    while (!isValidCategory(category)) {
      System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
    }

  }

  private static void addMeal() throws SQLException {
    System.out.println("Which meal do you want to add (breakfast, lunch, dinner)?");
    String category = scanner.nextLine();
    while (!isValidCategory(category)) {
      System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
      category = scanner.nextLine();
    }

    System.out.println("Input the meal's name:");
    String name = scanner.nextLine();

    while (!isValidName(name)) {
      System.out.println("Wrong format. Use letters only!");
      name = scanner.nextLine();
    }

    ArrayList<String> ingredients = getIngredients(scanner);
    while (!validateIngredients(ingredients)) {
      System.out.println("Wrong format. Use letters only!");
      ingredients = getIngredients(scanner);
    }


    int id = dbManager.getNextId();
    Meal meal = new Meal(category, name, id, ingredients);
    dbManager.insertMeal(meal);
    dbManager.insertIngredients(meal);
    System.out.println("The meal has been added!");
  }

  private static boolean isValidCategory(String category) {
    return category.equals("breakfast") || category.equals("lunch") || category.equals("dinner");
  }

  private static boolean isValidName(String name) {
    return name.matches("^(?!\\s*$)[a-zA-Z\\s]+$");
  }

  private static boolean isValidIngredient(String ingredient) {
    return ingredient.matches("^(?!\\s*$)[a-zA-Z\\s,]+$");
  }

  private static ArrayList<String> getIngredients(Scanner scanner) {
    System.out.println("Input the ingredients:");
    String ingredientsInput = scanner.nextLine();
    String[] givenIngredients = ingredientsInput.split(",");
    return new ArrayList<>(Arrays.asList(givenIngredients));
  }

  private static boolean validateIngredients(ArrayList<String> ingredients) {
    for (String ingredient : ingredients) {
      if (!isValidIngredient(ingredient)) {
        return false;
      }
    }

    return true;
  }

}