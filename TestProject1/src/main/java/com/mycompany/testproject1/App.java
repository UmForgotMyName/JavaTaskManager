package com.mycompany.testproject1;

import java.util.ArrayList;
import java.util.Collections;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    public static ArrayList<GridPane> newPanes = new ArrayList<>();
    public static ArrayList<Task> tasks = new ArrayList<>();
    public static String sort;

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox();

        vbox.getChildren().add(0, createNavigation(vbox));
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Add the ScrollPane to a Scene and show the scene
        Scene scene = new Scene(scrollPane, 800, 600);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static GridPane createGridPanes(Task task, VBox vbox) {

        Label titleLabel = new Label(task.getTitle());
        titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
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
            newPanes.add(index, createGridPanes(newTask, vbox));
            vbox.getChildren().add(index + 1, newPanes.get(index));

            // sort the tasks after editing
            sortTasks(vbox);
        });
        gridPane.add(editButton, 0, 4);

        // Set some styling for the gridpane
        gridPane.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 10px;");

        return gridPane;
    }

    public static void sortTasks(VBox vbox) {
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
                sortedPanes.add(i, createGridPanes(tasks.get(index), vbox));
            }
            newPanes = sortedPanes;
            vbox.getChildren().clear();
            vbox.getChildren().add(0, createNavigation(vbox));
            vbox.getChildren().addAll(newPanes);
        }
    }

    public static Node createNavigation(VBox vbox) {
        Button Addbtn = new Button();
        Addbtn.setText("Add Task");

        Label sortLabel = new Label("Sort by");
        ChoiceBox<String> sortOptions = new ChoiceBox<>();
        sortOptions.getItems().addAll("Importance", "Creation date", "Due date");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.add(Addbtn, 0, 0);
        gridPane.add(sortLabel, 3, 0);
        gridPane.add(sortOptions, 4, 0);

        Addbtn.setOnAction((ActionEvent event) -> {
            Task task = TaskManager.taskCreator();
            if (task != null){
            tasks.add(task);
            newPanes.add(createGridPanes(task, vbox));
            vbox.getChildren().add(newPanes.get(newPanes.size() - 1));
            }
        });

        sortOptions.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            System.out.println("Selected item changed from " + oldValue + " to " + newValue);
            sort = newValue;
            sortTasks(vbox);
        });
        return gridPane;
    }

}
