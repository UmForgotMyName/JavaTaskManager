package com.example.javatasklistmanager;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.mindrot.jbcrypt.BCrypt;

import java.util.*;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class App extends Application {

    private static final MongoCollection<Document> UsersCollection; // connects to main usercollection in mongodb

    static { //does all connection stuff
        String uri = "mongodb+srv://<username>:<password>@javacluster.<address>.mongodb.net/";
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .serverApi(serverApi)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("JavaTasklist");
        UsersCollection = database.getCollection("users");
    }

    static String username; //username
    static VBox taskListsVBox;//VBox used when showing the users different tasklists

    static List<Document> taskLists; //stores all tasklists in a document format

    static List<Document> tasksDocuments = new ArrayList<>(); //stores all task in a document format

    public static ArrayList<GridPane> newPanes = new ArrayList<>(); //all taskpanes shown
    public static ArrayList<Task> tasks = new ArrayList<>();// all tasks in object format

    public static VBox vbox = new VBox(); //main vbox
    public static String sort;// sorting option
    public static String ListTitle;// title of current list selected

    public void taskDisplay() {
        vbox.getChildren().add(0, createNavigation());

        DBtoTask();
        for (Task task : tasks) {
            GridPane tempPane = createGridPanes(task);
            vbox.getChildren().add(tempPane);
            newPanes.add(tempPane);
        }

        // Add the ScrollPane to a Scene and show the scene
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 800, 600);
        Stage stage = new Stage();
        stage.setTitle(ListTitle);
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            // Clear data
            vbox.getChildren().clear();
            tasksDocuments.clear();
            newPanes.clear();
            tasks.clear();
        });

        stage.show();
    }

    public static void startOfApp(String[] args) {
        launch(args);
    }

    public void DBtoTask() {
        // Retrieve the user document from the UsersCollection
        Document userDocument = UsersCollection.find(eq("username", username)).first();
        if (userDocument != null) {
            // Find the task list with the specified ListTitle
            taskLists = userDocument.getList("taskLists", Document.class);
            if (taskLists != null) {
                Optional<Document> selectedTaskList = taskLists.stream()
                        .filter(taskList -> taskList.getString("title").equals(ListTitle))
                        .findFirst();

                // Convert the tasks from the selected task list into Task objects
                selectedTaskList.ifPresent(document -> {
                    tasksDocuments = document.getList("tasks", Document.class);
                    if (tasksDocuments != null) {
                        tasksDocuments.forEach(taskDocument -> {
                            Task task = null;
                            task = new Task(
                                    taskDocument.getString("title"),
                                    taskDocument.getString("description"),
                                    taskDocument.getString("dueDate"),
                                    taskDocument.getDate("creationDate"),
                                    taskDocument.getInteger("importance"),
                                    taskDocument.getBoolean("completed")
                            );
                            tasks.add(task);
                        });
                    }
                });
            }
        }
    }

    public static GridPane createGridPanes(Task task) {

        Text titleLabel = new Text(task.getTitle());
        titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 20));

        if (task.getCompleted()) {
            titleLabel.setStyle("-fx-strikethrough: true;");
        } else {
            titleLabel.setStyle("-fx-strikethrough: false;");
        }

        Label descriptionLabel = new Label(task.getDescription());
        Label dueDateLabel = new Label("Due date: " + task.getDueDate());
        Label importanceLabel = new Label("Importance: " + task.getImportance());
        Label creationDateLabel = new Label("Created: " + task.getCreationDate());

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.add(titleLabel, 0, 0);
        gridPane.add(descriptionLabel, 0, 1);
        gridPane.add(dueDateLabel, 0, 2);
        gridPane.add(creationDateLabel, 3, 2);
        gridPane.add(importanceLabel, 0, 3);

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction((ActionEvent event) -> {
            int index = newPanes.indexOf(gridPane);

            newPanes.remove(gridPane);
            vbox.getChildren().remove(gridPane);

            String taskTitle = tasks.get(index).getTitle();
            tasks.remove(index);
            tasksDocuments.remove(index);

            // Update to remove the specified task from the "tasks" array
            Bson filter = Filters.and(
                    Filters.eq("username", username),
                    Filters.eq("taskLists.title", ListTitle)
            );
            Bson update = Updates.pull("taskLists.$.tasks", Filters.eq("title", taskTitle));
            UsersCollection.updateOne(filter, update);
        });
        gridPane.add(deleteButton, 1, 4);

        Button editButton = new Button("Edit");
        editButton.setOnAction((ActionEvent event) -> {
            int index = newPanes.indexOf(gridPane);

            // Store the current title before editing
            String currentTitle = tasks.get(index).getTitle();

            // Call the taskEditor method to get the edited task
            Task newTask = TaskManager.taskEditor(tasks.get(index));

            // Remove the current task from the list and UI
            newPanes.remove(gridPane);
            vbox.getChildren().remove(gridPane);
            tasks.remove(index);
            tasksDocuments.remove(index);

            // Update the task list with the edited task
            tasks.add(index, newTask);
            newPanes.add(index, createGridPanes(newTask));
            vbox.getChildren().add(index + 1, newPanes.get(index));

            // Update the database with the edited task
            updateTaskInDatabase(currentTitle, newTask);

            sortTasks();
        });
        gridPane.add(editButton, 0, 4);

        Button completedButton = new Button(task.getCompleted() ? "Mark as Uncompleted" : "Mark as Completed");
        completedButton.setOnAction((ActionEvent event) -> {
            //Changes styling and updates db w
            task.setCompleted(!task.getCompleted());

            completedButton.setText(task.getCompleted() ? "Mark as Uncompleted" : "Mark as Completed");

            String backgroundColor = !task.getCompleted() ? "#ECEFF1" : "#ADD8E6";
            String fontFamily = "Roboto, sans-serif;";
            String css = "-fx-background-color: " + backgroundColor + ";" + "-fx-font-family: " + fontFamily + ";" + "-fx-font-size: 12pt;";
            gridPane.setStyle(css + "-fx-border-color: black; -fx-padding: 10px;");

            if (task.getCompleted()) {
                titleLabel.setStyle("-fx-strikethrough: true;");
            } else {
                titleLabel.setStyle("-fx-strikethrough: false;");
            }

            updateTaskCompletionStatus(task.getTitle(), task.getCompleted());
        });
        gridPane.add(completedButton, 3, 4);

        // Set some styling for the gridpane
        String backgroundColor = !task.getCompleted() ? "#ECEFF1" : "#ADD8E6";
        String fontFamily = "Roboto, sans-serif;";
        String css = "-fx-background-color: " + backgroundColor + ";" + "-fx-font-family: " + fontFamily + ";" + "-fx-font-size: 12pt;";
        gridPane.setStyle(css + "-fx-border-color: black; -fx-padding: 10px;");

        return gridPane;
    }

    private static void updateTaskCompletionStatus(String taskTitle, boolean completed) {
        Bson filter = Filters.and(
                Filters.eq("username", username),
                Filters.eq("taskLists.title", ListTitle),
                Filters.eq("taskLists.tasks.title", taskTitle)
        );
        Bson update = Updates.set("taskLists.$.tasks.$[innerTask].completed", completed);
        UpdateOptions options = new UpdateOptions().arrayFilters(
                List.of(Filters.eq("innerTask.title", taskTitle))
        );

        UsersCollection.updateOne(filter, update, options);
    }

    private static void updateTaskInDatabase(String currentTitle, Task editedTask) {
        Bson filter = Filters.and(
                Filters.eq("username", username),
                Filters.eq("taskLists.title", ListTitle),
                Filters.eq("taskLists.tasks.title", currentTitle)
        );
        Bson update = Updates.combine(
                Updates.set("taskLists.$.tasks.$[innerTask].completed", editedTask.getCompleted()),
                Updates.set("taskLists.$.tasks.$[innerTask].title", editedTask.getTitle()),
                Updates.set("taskLists.$.tasks.$[innerTask].description", editedTask.getDescription()),
                Updates.set("taskLists.$.tasks.$[innerTask].dueDate", editedTask.getDueDate()),
                Updates.set("taskLists.$.tasks.$[innerTask].importance", editedTask.getImportance()),
                Updates.set("taskLists.$.tasks.$[innerTask].creationDate", editedTask.getCreationDate())
        );
        UpdateOptions options = new UpdateOptions().arrayFilters(
                List.of(Filters.eq("innerTask.title", currentTitle))
        );

        UsersCollection.updateOne(filter, update, options);
    }

    private static Document convertTaskToDocument(Task task) {
        Document temp = new  Document()
                .append("title", task.getTitle())
                .append("description", task.getDescription())
                .append("dueDate", task.getDueDate())
                .append("importance", task.getImportance())
                .append("completed", task.getCompleted())
                .append("creationDate", task.getCreationDate());
        tasksDocuments.add(temp);
        return temp;
    }

    public static void sortTasks() {
        ArrayList<GridPane> sortedPanes = new ArrayList<>();
        ArrayList<Task> sortedTasks = new ArrayList<>(tasks);
        if (sort != null) {
            switch (sort) {
                case "Importance" -> sortedTasks.sort(new TaskImportanceComparator());
                case "Creation date" -> sortedTasks.sort(new TaskCreationDateComparator());
                case "Due date" -> sortedTasks.sort(new TaskDueDateComparator());
            }

            for (int i = 0; i < sortedTasks.size(); i++) {
                int index = tasks.indexOf(sortedTasks.get(i));
                sortedPanes.add(i, createGridPanes(tasks.get(index)));
            }
            newPanes = sortedPanes;
            vbox.getChildren().clear();
            vbox.getChildren().add(0, createNavigation());
            vbox.getChildren().addAll(newPanes);
        }
    }

    public static Node createNavigation() {
        Button Addbtn = new Button();
        Addbtn.setText("Add Task");

        Label sortLabel = new Label("Sort by");
        ChoiceBox<String> sortOptions = new ChoiceBox<>();
        sortOptions.getItems().addAll("Importance", "Creation date", "Due date");
        sortOptions.setValue(sort);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.add(Addbtn, 0, 0);
        gridPane.add(sortLabel, 3, 0);
        gridPane.add(sortOptions, 4, 0);
        gridPane.setPadding(new Insets(0, 0, 10, 0)); // Adds 10 pixels of padding to the bottom
        gridPane.setAlignment(Pos.CENTER);

        // CSS Styling
        String backgroundColor = "#ECEFF1";
        String buttonColor = "#2196F3";
        String buttonTextColor = "white";
        String titleTextColor = "#37474F";
        String fontFamily = "Roboto, sans-serif;";
        String css = "-fx-background-color: " + backgroundColor + ";"
                + "-fx-font-family: " + fontFamily + ";"
                + "-fx-font-size: 12pt;";

        Addbtn.setStyle(css + "-fx-background-color: " + buttonColor + "; -fx-text-fill: " + buttonTextColor + ";");

        sortLabel.setStyle(css + "-fx-text-fill: " + titleTextColor + ";");
        sortOptions.setStyle(css + "-fx-background-color: " + buttonColor + "; -fx-text-fill: " + buttonTextColor + ";");

        // Create new task and add to VBox
        Addbtn.setOnAction((ActionEvent event) -> {
            Task task = TaskManager.taskCreator();
            if (task != null) {

                Document newTaskDocument = new Document()
                        .append("title", task.getTitle())
                        .append("description", task.getDescription())
                        .append("dueDate", task.getDueDate())
                        .append("importance", task.getImportance())
                        .append("completed", task.getCompleted())
                        .append("creationDate", task.getCreationDate());

                tasksDocuments.add(newTaskDocument);

                Document userDocument = UsersCollection.find(and(eq("username", username), eq("taskLists.title", ListTitle))).first();

                if (userDocument != null) {
                    Bson filter = and(eq("username", username), eq("taskLists.title", ListTitle));
                    Bson update = new Document("$push", new Document("taskLists.$.tasks", newTaskDocument));
                    UsersCollection.updateOne(filter, update);

                }
                tasks.add(task);
                newPanes.add(createGridPanes(task));
                vbox.getChildren().add(newPanes.get(newPanes.size() - 1));

                sortTasks();
            }
        });

        // sort VBox and tasks
        sortOptions.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            sort = newValue;
            sortTasks();
        });

        return gridPane;
    }

    @Override
    public void start(Stage primaryStage) {
        // Create the UI elements
        Label titleLabel = new Label("Rehan's TaskManager");
        Button loginButton = new Button("Log In");
        Button createButton = new Button("Create New User");

        // Set the spacing and padding of the vertical box
        VBox localVBox = new VBox(titleLabel, loginButton, createButton);
        localVBox.setSpacing(10);
        localVBox.setPadding(new Insets(10));

        // Create a new scene with the vertical box
        Scene scene = new Scene(localVBox, 300, 150);

        // Create a new stage for the popup
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(scene);
        popupStage.setTitle("Rehan's TaskManager");

        // Add a style sheet to the scene to improve the look and feel of the popup
        String backgroundColor = "#ECEFF1";
        String buttonColor = "#2196F3";
        String buttonTextColor = "white";
        String titleTextColor = "#37474F";
        String fontFamily = "Roboto, sans-serif;";
        String css = "-fx-background-color: " + backgroundColor + ";"
                + "-fx-font-family: " + fontFamily + ";"
                + "-fx-font-size: 14pt;";
        titleLabel.setStyle(css + "-fx-text-fill: " + titleTextColor + ";");
        loginButton.setStyle(css + "-fx-background-color: " + buttonColor + "; -fx-text-fill: " + buttonTextColor + ";");
        createButton.setStyle(css + "-fx-background-color: " + buttonColor + "; -fx-text-fill: " + buttonTextColor + ";");

        // Add an event handler to the login button
        loginButton.setOnAction((ActionEvent event) -> {
            Optional<Pair<String, String>> result = loginPopup();
            if (result.isPresent()) {
                username = result.get().getKey();
                String password = result.get().getValue();

                if (authenticateUser(username, password)) {
                    showTaskLists(username, primaryStage);
                    popupStage.close();
                } else {
                    // Invalid credentials, show error message
                    showAlert(AlertType.ERROR, "Error", "Invalid username or password.");
                }
            }
        });

        // Create New User
        createButton.setOnAction((ActionEvent event) -> {
            createUserPopup();
        });

        // Show the popup
        popupStage.showAndWait();
    }

    private Optional<Pair<String, String>> loginPopup() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Log In");
        dialog.setHeaderText("Enter your username and password");

        // Set the button types
        ButtonType loginButtonType = new ButtonType("Log In", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the login form grid pane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        // Do some validation
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(usernameField::requestFocus);

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(usernameField.getText(), passwordField.getText());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private boolean authenticateUser(String username, String password) {
        // use BCrypt to securely store and check password associated with username
        Document userDocument = UsersCollection.find(eq("username", username)).first();
        return userDocument != null && BCrypt.checkpw(password, userDocument.getString("password"));
    }

    private void createUserPopup() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Create New User");
        dialog.setHeaderText("Enter a new username and password");

        // Set the button types
        ButtonType createButtonType = new ButtonType("Create User", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the user creation form grid pane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(usernameField::requestFocus);

        // Convert the result to a username-password-pair when the create user button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Pair<>(usernameField.getText(), passwordField.getText());
            }
            return null;
        });

        // Disable the create button by default
        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        // Add validation for non-empty fields
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(newValue.trim().isEmpty() || passwordField.getText().trim().isEmpty());
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(newValue.trim().isEmpty() || usernameField.getText().trim().isEmpty());
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(newUser -> {
            // Check if the username is unique within the collection
            if (isUsernameUnique(newUser.getKey())) {
                // Insert a new user document with a unique username with encrypted password
                Document newUserDocument = new Document()
                        .append("username", newUser.getKey())
                        .append("password", BCrypt.hashpw(newUser.getValue(), BCrypt.gensalt()))
                        .append("taskLists", new ArrayList<Document>());
                UsersCollection.insertOne(newUserDocument);
                showAlert(AlertType.INFORMATION, "Success", "User created successfully.");
            } else {
                showAlert(AlertType.ERROR, "Error", "Username already exists. Please choose another username.");
            }
        });
    }

    private void showAlert(AlertType alertType, String title, String contentText) { // Create alerts used everywhere
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    private boolean isUsernameUnique(String username) { //Searchs collection if username is unique
        Bson projectionFields = Projections.fields(
                Projections.include("username"),
                Projections.excludeId());
        Document userDoc = UsersCollection.find(eq("username", username))
                .projection(projectionFields)
                .sort(Sorts.descending("username"))
                .first();
        return userDoc == null;
    }

    private void showTaskLists(String username, Stage primaryStage) {
        // Get the user's task lists from the database
        Document userDocument = UsersCollection.find(eq("username", username)).first();
        if (userDocument != null) {
            taskLists = userDocument.getList("taskLists", Document.class);

            // Apply CSS styling
            String backgroundColor = "#ECEFF1";
            String buttonColor = "#2196F3";
            String buttonTextColor = "white";
            String fontFamily = "Roboto, sans-serif;";
            String css = "-fx-background-color: " + backgroundColor + ";"
                    + "-fx-font-family: " + fontFamily + ";"
                    + "-fx-font-size: 12pt;";

            // Create UI elements for task lists
            taskListsVBox = new VBox();
            Button createTaskListButton = new Button("Create New TaskList");
            taskListsVBox.getChildren().add(createTaskListButton);
            createTaskListButton.setStyle(css + "-fx-background-color: " + buttonColor + "; -fx-text-fill: " + buttonTextColor + ";");

            // Create buttons for each task list
            if (taskLists != null) {
                for (Document taskList : taskLists) {
                    HBox taskListBox = createTaskList(taskList);

                    // Add the HBox to the VBox
                    taskListsVBox.getChildren().add(taskListBox);
                }
            }

            // Add event handler for the create task list button
            createTaskListButton.setOnAction(event -> {
                // Prompt the user to enter a unique task list name
                String newTaskListName = promptForTaskListName(username);
                if (newTaskListName != null) {
                    // Create a new task list with the entered name
                    Document newTaskList = new Document("title", newTaskListName)
                            .append("tasks", new ArrayList<Document>());

                    // Add the new task list to the user's task lists
                    Objects.requireNonNull(taskLists).add(newTaskList);

                    HBox taskListBox = createTaskList(newTaskList);

                    // Add the HBox to the VBox
                    taskListsVBox.getChildren().add(taskListBox);

                    // Save the updated task lists to the database
                    UsersCollection.updateOne(eq("username", username), set("taskLists", taskLists));

                    // Inform the user about the successful creation
                    showAlert(AlertType.INFORMATION, "Success", "Task list created successfully: " + newTaskListName);
                }
            });


            // Set spacing and padding
            taskListsVBox.setSpacing(10);
            taskListsVBox.setPadding(new Insets(10));
            ScrollPane scrollPane = new ScrollPane(taskListsVBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            // Create a scene for the task lists
            Scene taskListsScene = new Scene(scrollPane, 300, 400);

            // Set up the primary stage
            primaryStage.setTitle("Task Lists");
            primaryStage.setScene(taskListsScene);
            primaryStage.show();
        }
    }

    private HBox createTaskList(Document taskList) {

        // Apply CSS styling
        String backgroundColor = "#ECEFF1";
        String buttonColor = "#2196F3";
        String buttonTextColor = "white";
        String titleTextColor = "#37474F";
        String fontFamily = "Roboto, sans-serif;";
        String css = "-fx-background-color: " + backgroundColor + ";"
                + "-fx-font-family: " + fontFamily + ";"
                + "-fx-font-size: 12pt;";

        ListTitle = taskList.getString("title");

        // Create an HBox for better styling
        HBox taskListBox = new HBox();
        Button openTaskListButton = new Button("Open");
        Button deleteTaskListButton = new Button("Delete");

        // Set spacing and padding for the HBox
        taskListBox.setSpacing(10);
        taskListBox.setPadding(new Insets(10));

        // Add buttons to the HBox
        taskListBox.getChildren().addAll(new Label(ListTitle), openTaskListButton, deleteTaskListButton);

        taskListBox.getChildren().get(0).setStyle(css + "-fx-text-fill: " + titleTextColor + ";");
        openTaskListButton.setStyle(css + "-fx-background-color: " + buttonColor + "; -fx-text-fill: " + buttonTextColor + ";");
        deleteTaskListButton.setStyle(css + "-fx-background-color: " + buttonColor + "; -fx-text-fill: " + buttonTextColor + ";");

        // Add event handlers for the buttons
        openTaskListButton.setOnAction(event -> {
            // Implement the logic to open the selected task list
            ListTitle = taskList.getString("title");
            taskDisplay();
        });

        deleteTaskListButton.setOnAction(event -> {
            // Retrieve the index or identifier of the selected task list
            int selectedIndex = taskListsVBox.getChildren().indexOf(taskListBox) - 1;

            // Check if the index is valid
            if (selectedIndex >= 0 && selectedIndex < taskLists.size()) {
                // Remove the selected task list from the list
                taskLists.remove(selectedIndex);

                // Update the user's document in the database with the modified taskLists
                UsersCollection.updateOne(eq("username", username), set("taskLists", taskLists));

                // Remove the HBox (taskListBox) from the taskListsVBox
                taskListsVBox.getChildren().remove(taskListBox);

            } else {
                // Display an error message if the index is not valid
                showAlert(AlertType.ERROR, "Error", "Invalid selection. Please try again.");
            }
        });

        // Add the HBox to the VBox
        return taskListBox;
    }

    private String promptForTaskListName(String username) {
        boolean sameTasklistName = false;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Task List");
        dialog.setHeaderText("Enter a unique name for the new task list:");
        dialog.setContentText("Task List Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newTaskListName = result.get().trim();
            // Check if the entered name is unique among the user's task lists
            Document userDocument = UsersCollection.find(eq("username", username)).first();
            assert userDocument != null;
            List<Document> taskLists = userDocument.getList("taskLists", Document.class);
            if (taskLists == null) return newTaskListName;
            for (Document taskList : taskLists) {
                String taskListTitle = taskList.getString("title");
                if (taskListTitle.equals(newTaskListName)) {
                    sameTasklistName = true;
                }
            }
            if (!sameTasklistName && !newTaskListName.isEmpty()) {
                return newTaskListName;
            } else {
                showAlert(AlertType.ERROR, "Error", "Task list name must be unique. Please choose another name.");
                return promptForTaskListName(username);
            }
        }
        return null; // User canceled the input
    }
}
