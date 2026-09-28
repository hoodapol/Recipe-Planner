# Recipe Planner

A JavaFX desktop app for browsing, searching, and saving recipes, built for the Advanced Programming Laboratory course. Recipes are fetched from the TheMealDB API, stored in a local SQLite database, and displayed in a responsive multi-screen interface.

## Features

- Home tab with a responsive grid of recipe image cards
- Categories tab: pick one of six categories (Breakfast, Lunch, Dinner, Dessert, Appetizer, Snack) to see matching recipes
- Search screen with live filtering by title or category as you type, or on Enter
- Recipe detail screen with image, category, description, nutrition, ingredients, and numbered steps
- Favorites that persist across restarts
- Edit a recipe's description and delete a recipe (with a confirmation dialog)
- Deleting a recipe also removes it from Favorites automatically
- Images are cached in memory so they load once per session
- Hand-written sweet and savory sample recipes that show nutrition data

## Requirements

- JDK 21
- Maven
- Internet connection on first launch (recipes and images come from TheMealDB)
- Dependencies (managed by Maven): JavaFX 21, sqlite-jdbc 3.53.4.0, jackson-databind 2.18.2

Run with `mvn clean javafx:run`. The app creates `recipeplanner.db` on first launch. It is git-ignored, and deleting it reseeds the database.

## Project Layout

```
src/main/java/com/recipeplanner/
├── Main.java                     
├── Controller/
│   ├── WelcomeController.java     # Welcome screen, navigates to Dashboard
│   ├── DashboardController.java   # Home / Favorites / Categories tabs, recipe card grid
│   ├── SearchController.java      # Live search, background filtering
│   └── DetailController.java      # Recipe detail, favorite, edit, delete
└── model/
    ├── Recipe.java                # Base class
    ├── NutritionProvider.java     # Interface
    ├── NutritionalRecipe.java     # Abstract class
    ├── SweetFood.java             # Extends NutritionalRecipe
    ├── SavoryFood.java            # Extends NutritionalRecipe
    ├── Ingredient.java
    ├── Category.java              # Enum
    ├── Favorites.java             # Singleton, delegates to the database
    ├── RecipeRepository.java      # Singleton, seeds and reads recipes
    ├── DatabaseManager.java       # All SQL: schema, insert, select, update, delete
    ├── RecipeApiClient.java       # HTTP requests and JSON parsing
    └── ImageCache.java            # In-memory image cache
src/main/resources/com/recipeplanner/
├── welcome-view.fxml
├── dashboard-view.fxml
├── detail-view.fxml
└── search-view.fxml
```

## Concepts Used

Organized by the assignment guide's topics.

**Advanced OOP**
- Inheritance chain: `SweetFood` / `SavoryFood` extend `NutritionalRecipe`, which extends `Recipe`
- Abstract class: `NutritionalRecipe` forces subclasses to implement `getNutritionSummary()`
- Interface: `Recipe` implements `NutritionProvider`
- Polymorphism: `getNutritionSummary()` behaves differently for each subclass at runtime
- Encapsulation and composition: `Recipe` has private fields and holds a list of `Ingredient` objects
- Enums: `Category`, and `SavoryFood.SpiceLevel`
- Singleton pattern: `Favorites`, `RecipeRepository`

**JavaFX UI Design**
- FXML views with matching controllers, wired through `fx:id` and `onAction`
- Panes: `BorderPane`, `StackPane`, `VBox`, `HBox`, `FlowPane`, `ScrollPane`, `Region`
- Controls: `Label`, `Button`, `TextField`, `ListView`, `ImageView`, `TextFlow`
- Dialogs: `TextInputDialog` for editing, `Alert` for delete confirmation
- Navigation swaps the Scene's root with `setRoot(...)`, so the window keeps its size and maximized state
- Data is passed between screens through `loader.getController()`
- Dynamic content (recipe cards, category pills, result rows) is built in Java at runtime
- Styling uses inline JavaFX style properties; there is no external stylesheet

**Layout Responsiveness**
- Property binding in `DetailController`: the recipe image's `fitWidth` and its rounded clip are bound to the container's width
- `FlowPane` re-wraps recipe cards as the window resizes
- `ScrollPane` with `fitToWidth` keeps content reachable at any window size

**Concurrency**
- `ExecutorService` fixed thread pools with named daemon threads in `DashboardController`, `SearchController`, and `DetailController`
- Database reads, search filtering, and edit/delete run off the UI thread
- `Platform.runLater` returns results to the JavaFX Application Thread
- An `AtomicLong` request counter in `SearchController` discards stale search results
- Images load with JavaFX's background loading

**Database Integration**
- SQLite through JDBC, with `PRAGMA foreign_keys = ON` on every connection
- Four tables: `recipes`, `ingredients`, `steps`, `favorites`
- Primary key `id` on every table
- Foreign keys: `ingredients.recipe_id`, `steps.recipe_id`, `favorites.recipe_id` reference `recipes.id`
- `ON DELETE CASCADE` on `favorites`; ingredients and steps are deleted in code before the recipe
- `PreparedStatement` for all parameterized queries

**Data Manipulation (CRUD)**
- Create: `insertRecipe`, splitting one `Recipe` across three tables
- Read: `getAllRecipes`, `getRecipeById`, with `ResultSet` mapping back to the correct subclass
- Update: `updateRecipeDescription`, triggered by the Edit button
- Delete: `deleteRecipe`, triggered by the Delete button

**Networking and Data Parsing**
- `HttpClient` requests to TheMealDB (`random.php`, `filter.php`, `lookup.php`)
- Jackson `ObjectMapper` and `JsonNode` parse the responses into `Recipe` and `Ingredient` objects
- The API's 20 numbered ingredient fields are looped into a list, and the instructions paragraph is split into steps

## Known Gaps

- Only the four hand-written sample recipes have nutrition data. API recipes show a fallback message, since the free API provides none.
- Search only covers recipes already stored locally, not the full TheMealDB catalog.
- API ingredients keep their measure inside the name (for example "2 tbsp Butter"), with quantity 0, because the API gives no separate quantity and unit.
- TheMealDB categories are mapped to the six app categories heuristically. Some assignments are arbitrary (Pasta maps to Lunch, Vegetarian and Vegan map to Snack).
- If the first launch is offline, only the four sample recipes are stored and the app won't retry the fetch. Delete `recipeplanner.db` and relaunch online to reseed.
- Duplicate recipes are only avoided within a single seeding run.
- Only the description is editable.
