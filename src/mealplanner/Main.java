package mealplanner;

import java.sql.SQLException;
import java.util.*;

public class Main {
  public static DBManager dbManager = null;
  public static Scanner scanner = new Scanner(System.in);
  public static ArrayList<String> days =  new ArrayList<>(Arrays.asList("Monday", "Tuesday",
                                                          "Wednesday", "Thursday", "Friday",
                                                          "Saturday", "Sunday"));

  public static void main(String[] args) {
    try {
      dbManager = new DBManager();

      userLoop: while (true) {
        // Ask user for action
        System.out.println("What would you like to do (add, show, plan, list plan, exit)?");
        String action = scanner.nextLine();

        switch (action) {
          case "show":
            showMeals();
            break;
          case "add":
            addMeal();
            break;
          case "plan":
            createPlan();
            break;
          case "list plan":
            listPlan();
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

  private static void showMeals() throws SQLException {
    System.out.println("Which category do you want to print (breakfast, lunch, dinner)?");
    String category = scanner.nextLine();
    while (!isValidCategory(category)) {
      System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
      category = scanner.nextLine();
    }

    dbManager.showMeals(category);
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

  private static void createPlan() throws SQLException {
    for (String day : days) {
      System.out.println(day);

      ArrayList<String> tempMeals = dbManager.listMeals("breakfast");
      System.out.printf("Choose the breakfast for %s from the list above:\n", day);
      String breakfast = getPlanMeal(tempMeals, "breakfast", day);

      tempMeals = dbManager.listMeals("lunch");
      System.out.printf("Choose the lunch for %s from the list above:\n", day);
      String lunch = getPlanMeal(tempMeals, "lunch", day);

      tempMeals = dbManager.listMeals("dinner");
      System.out.printf("Choose the dinner for %s from the list above:\n", day);
      String dinner = getPlanMeal(tempMeals, "dinner", day);


      System.out.printf("Yeah! We planned the meals for %s.\n", day);
      System.out.println();

      dbManager.setPlanDay(day, "breakfast", breakfast);
      dbManager.setPlanDay(day, "lunch", lunch);
      dbManager.setPlanDay(day, "dinner", dinner);
    }

    listPlan();
  }

  private static String getPlanMeal(ArrayList<String> tempMeals, String category, String day) {
    String selectedMeal = scanner.nextLine();
    while (!tempMeals.contains(selectedMeal)) {
      System.out.println("This meal doesn’t exist. Choose a meal from the list above.");
      selectedMeal = scanner.nextLine();
    }

    return selectedMeal;
  }

  private static void listPlan() throws SQLException {
    for (String day : days) {
      dbManager.showPlan(day);
    }
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