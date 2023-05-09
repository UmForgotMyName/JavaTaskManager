package com.mycompany.testproject1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * JavaFX App
 */
public class App extends Application {

    public static ArrayList<GridPane> newPanes = new ArrayList<>();
    public static ArrayList<Task> tasks = new ArrayList<>();
    public static VBox vbox = new VBox();
    public static String sort;
    public static String ListTitle;

    public void taskDisplay() {
        vbox.getChildren().add(0, createNavigation());
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Add the ScrollPane to a Scene and show the scene
        Scene scene = new Scene(scrollPane, 800, 600);
        Stage stage = new Stage();
        stage.setTitle(ListTitle);
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest((WindowEvent event) -> {
            // Show the start menu again
            vbox.getChildren().clear();
            stage.close();
            start(stage);
        });
    }

    public static void main(String[] args) {
        launch();
    }

    public static GridPane createGridPanes(Task task) {

        Label titleLabel = new Label(task.getTitle());
        titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 20));
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
            tasks.remove(index);
        });
        gridPane.add(deleteButton, 1, 4);

        Button editButton = new Button("Edit");
        editButton.setOnAction((ActionEvent event) -> {
            int index = newPanes.indexOf(gridPane);
            newPanes.remove(gridPane);
            vbox.getChildren().remove(gridPane);
            tasks.remove(index);

            Task newTask = TaskManager.taskEditor(task);
            tasks.add(index, newTask);
            newPanes.add(index, createGridPanes(newTask));
            vbox.getChildren().add(index + 1, newPanes.get(index));

            // sort the tasks after editing
            sortTasks();
        });
        gridPane.add(editButton, 0, 4);

        // Set some styling for the gridpane
        String backgroundColor = "#ECEFF1";
        String fontFamily = "Roboto, sans-serif;";
        String css = "-fx-background-color: " + backgroundColor + ";" + "-fx-font-family: " + fontFamily + ";" + "-fx-font-size: 12pt;";
        gridPane.setStyle(css + "-fx-border-color: black; -fx-padding: 10px;");

        return gridPane;
    }

    public static void sortTasks() {
        ArrayList<GridPane> sortedPanes = new ArrayList<>();
        ArrayList<Task> sortedTasks = new ArrayList<>(tasks);
        if (sort != null) {
            switch (sort) {
                case "Importance":
                    Collections.sort(sortedTasks, new TaskImportanceComparator());
                    break;
                case "Creation date":
                    Collections.sort(sortedTasks, new TaskCreationDateComparator());
                    Collections.reverse(sortedTasks);
                    break;
                case "Due date":
                    Collections.sort(sortedTasks, new TaskDueDateComparator());
                    break;
                default:
                    break;
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

        Button Savebtn = new Button();
        Savebtn.setText("Save");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.add(Addbtn, 0, 0);
        gridPane.add(sortLabel, 3, 0);
        gridPane.add(sortOptions, 4, 0);
        gridPane.add(Savebtn, 7, 0);
        gridPane.setPadding(new Insets(0, 0, 10, 0)); // Adds 10 pixels of padding to the bottom
        gridPane.setAlignment(Pos.CENTER);

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

        Savebtn.setStyle(css + "-fx-background-color: " + buttonColor + "; -fx-text-fill: " + buttonTextColor + ";");

        Addbtn.setOnAction((ActionEvent event) -> {
            Task task = TaskManager.taskCreator();
            if (task != null) {
                tasks.add(task);
                newPanes.add(createGridPanes(task));
                vbox.getChildren().add(newPanes.get(newPanes.size() - 1));

                sortTasks();
            }
        });

        sortOptions.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            //System.out.println("Selected item changed from " + oldValue + " to " + newValue);
            sort = newValue;
            sortTasks();
        });

        Savebtn.setOnAction((ActionEvent event) -> {
            PanesToFile(new File(System.getProperty("user.home") + "/TaskManagerFiles/" + ListTitle + ".txt"));
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Task List Saved");
            alert.setHeaderText(null);
            alert.setContentText("TaskList has been successfully saved to " + ListTitle +".txt");
            alert.showAndWait();
        });

        return gridPane;
    }

    @Override
    public void start(Stage primaryStage) {
        // Create the UI elements
        Button selectButton = new Button("Select TaskList");
        Label titleLabel = new Label("Rehan's TaskManager");
        Button createButton = new Button("Create New TaskList");

        // Set the spacing and padding of the vertical box
        VBox localVBox = new VBox(titleLabel, selectButton, createButton);
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
        scene.getStylesheets().add("https://fonts.googleapis.com/css2?family=Roboto&display=swap");
        String backgroundColor = "#ECEFF1";
        String buttonColor = "#2196F3";
        String buttonTextColor = "white";
        String titleTextColor = "#37474F";
        String fontFamily = "Roboto, sans-serif;";
        String css = "-fx-background-color: " + backgroundColor + ";"
                + "-fx-font-family: " + fontFamily + ";"
                + "-fx-font-size: 14pt;";
        titleLabel.setStyle(css + "-fx-text-fill: " + titleTextColor + ";");
        selectButton.setStyle(css + "-fx-background-color: " + buttonColor + "; -fx-text-fill: " + buttonTextColor + ";");
        createButton.setStyle(css + "-fx-background-color: " + buttonColor + "; -fx-text-fill: " + buttonTextColor + ";");

        // Add an event handler to the select button
        selectButton.setOnAction((ActionEvent event) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select a TaskList file");

            // Set the initial directory to the user's home directory
            File initialDirectory = new File(System.getProperty("user.home") + "/TaskManagerFiles");
            initialDirectory.mkdirs();
            fileChooser.setInitialDirectory(initialDirectory);

            // Show the file chooser dialog
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                String filename = selectedFile.getName();
                String extension = filename.substring(filename.lastIndexOf(".") + 1);
                if (extension.equals("txt")) {
                    FileToPanes(selectedFile);
                    taskDisplay();
                    popupStage.close(); // Close the popup stage
                } else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Please choose a text file (.txt)");
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error selecting file.");
                alert.showAndWait();
            }
        });

        createButton.setOnAction((ActionEvent event) -> {
            ListTitle = createTaskListPopup();
            if (ListTitle != null) {
                taskDisplay();
                popupStage.close(); // Close the popup stage
            }
        });

        // Show the popup
        popupStage.showAndWait();
    }

    public static void FileToPanes(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line, title = null, description = null, dueDate = null, creationDate = null, importance;
            int lineCounter = 0;

            while ((line = reader.readLine()) != null) {
                if (lineCounter == 0) {
                    ListTitle = line;
                }
                switch ((lineCounter - 1) % 5) {
                    case 0:
                        title = line;
                        break;
                    case 1:
                        description = line;
                        break;
                    case 2:
                        dueDate = line;
                        break;
                    case 3:
                        creationDate = line;
                        break;
                    case 4:
                        importance = line;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                        Task task = new Task(title, description, dueDate, dateFormat.parse(creationDate), Integer.parseInt(importance));
                        tasks.add(task);
                        newPanes.add(createGridPanes(task));
                        vbox.getChildren().add(newPanes.get(newPanes.size() - 1));
                        break;
                    default:
                        break;
                }
                lineCounter++;
            }
        } catch (IOException | ParseException ex) {
            ex.printStackTrace();
        }

    }

    public static void PanesToFile(File outputFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, false))) {
            writer.println(ListTitle);
            for (Task task : tasks) {
                writer.println(task.getTitle());
                writer.println(task.getDescription());
                writer.println(task.getDueDate());
                writer.println(task.getCreationDate());
                writer.println(task.getImportance());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public static String createTaskListPopup() {
        final String[] name = new String[1];

        // Create a new stage for the popup
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Create Task List");

        // Create UI controls
        Label titleLabel = new Label("Enter a name for your task list:");
        TextField nameField = new TextField();
        Button createButton = new Button("Create");
        Button cancelButton = new Button("Cancel");

        // Set up layout
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));
        gridPane.add(titleLabel, 0, 0, 2, 1);
        gridPane.add(new Label("Name:"), 0, 1);
        gridPane.add(nameField, 1, 1);
        gridPane.add(createButton, 0, 2);
        gridPane.add(cancelButton, 1, 2);

        // Set up button actions
        createButton.setOnAction(e -> {
            boolean flag = true;

            File directory = new File(System.getProperty("user.home") + "/TaskManagerFiles");
            File[] files = directory.listFiles();
            name[0] = nameField.getText().trim();
            for (File file : files) {
                if (file.getName().equals(name[0] + ".txt")) {
                    flag = false;

                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("TaskList already exists. Please use another name.");
                    alert.showAndWait();
                }
            }

            if (!name[0].isEmpty() && flag == true) {
                popupStage.close();
            }
        });

        cancelButton.setOnAction(e -> popupStage.close());

        // Create a new scene with the grid pane
        Scene scene = new Scene(gridPane);

        // Set the scene on the popup stage
        popupStage.setScene(scene);

        // Show the popup and wait for it to close
        popupStage.showAndWait();
        return name[0];
    }
}
