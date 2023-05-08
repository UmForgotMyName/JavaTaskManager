/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.testproject1;

import java.util.regex.Pattern;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 *
 * @author rehan
 */
public class TaskManager {

    public static Task taskCreator() {

        final Task[] task = new Task[1]; // Declare task as a final array to make it effectively final
        final Pattern datePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        // Create the UI components
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll("1", "2", "3", "4", "5");
        choiceBox.setValue("1");

        ButtonType buttonTypeOK = new ButtonType("Submit", ButtonData.OK_DONE);

        TextField titleTextField = new TextField();
        TextField dueDateTextField = new TextField();
        dueDateTextField.setPromptText("YYYY-MM-DD");
        TextArea descriptionTextArea = new TextArea();
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Create a new Task");
        alert.setHeaderText(null);

        alert.getButtonTypes().setAll(buttonTypeOK);
        Button okButton = (Button) alert.getDialogPane().lookupButton(buttonTypeOK);

        // Create a GridPane layout to organize the components
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.add(new javafx.scene.control.Label("Title:"), 0, 0);
        gridPane.add(titleTextField, 1, 0);
        gridPane.add(new javafx.scene.control.Label("Description:"), 0, 1);
        gridPane.add(descriptionTextArea, 1, 1);
        gridPane.add(new javafx.scene.control.Label("dueDate:"), 0, 2);
        gridPane.add(dueDateTextField, 1, 2);
        gridPane.add(new javafx.scene.control.Label("Importance:"), 0, 3);
        gridPane.add(choiceBox, 1, 3);
        gridPane.add(okButton, 0, 4);

        GridPane.setHgrow(descriptionTextArea, Priority.ALWAYS);
        GridPane.setVgrow(descriptionTextArea, Priority.ALWAYS);
        alert.getDialogPane().setContent(gridPane);

        boolean validDate = false;
        while (!validDate) {
            alert.showAndWait();

            if (alert.getResult() == null) {
                // Dialog was closed without clicking Submit button
                return null;
            }

            String title = titleTextField.getText();
            String description = descriptionTextArea.getText();
            String DueDate = dueDateTextField.getText();
            int importance = Integer.parseInt(choiceBox.getValue());

            if (!datePattern.matcher(DueDate).matches()) {
                Alert validationAlert = new Alert(Alert.AlertType.ERROR);
                validationAlert.setTitle("Invalid Due Date");
                validationAlert.setHeaderText(null);
                validationAlert.setContentText("Due date must be in the format YYYY-MM-DD. Please correct your input.");
                validationAlert.showAndWait();
            } else {
                task[0] = new Task(title, description, DueDate, importance);
                validDate = true;
            }
        }

        return task[0];
    }

    public static Task taskEditor(Task oldTask) {

        final Task[] newTask = new Task[1]; // Declare task as a final array to make it effectively final
        final Pattern datePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

        // Create the UI components
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll("1", "2", "3", "4", "5");
        choiceBox.setValue(Integer.toString(oldTask.getImportance()));

        ButtonType buttonTypeOK = new ButtonType("Submit", ButtonData.OK_DONE);

        TextField titleTextField = new TextField();
        titleTextField.setText(oldTask.getTitle());

        TextField dueDateTextField = new TextField();
        dueDateTextField.setText(oldTask.getDueDate());

        TextArea descriptionTextArea = new TextArea();
        descriptionTextArea.setText(oldTask.getDescription());

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Edit: " + oldTask.getTitle());
        alert.setHeaderText(null);

        alert.getButtonTypes().setAll(buttonTypeOK);
        Button okButton = (Button) alert.getDialogPane().lookupButton(buttonTypeOK);

        // Create a GridPane layout to organize the components
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.add(new javafx.scene.control.Label("Title:"), 0, 0);
        gridPane.add(titleTextField, 1, 0);
        gridPane.add(new javafx.scene.control.Label("Description:"), 0, 1);
        gridPane.add(descriptionTextArea, 1, 1);
        gridPane.add(new javafx.scene.control.Label("dueDate:"), 0, 2);
        gridPane.add(dueDateTextField, 1, 2);
        gridPane.add(new javafx.scene.control.Label("Importance:"), 0, 3);
        gridPane.add(choiceBox, 1, 3);
        gridPane.add(okButton, 0, 4);

        GridPane.setHgrow(descriptionTextArea, Priority.ALWAYS);
        GridPane.setVgrow(descriptionTextArea, Priority.ALWAYS);
        alert.getDialogPane().setContent(gridPane);

        boolean validDate = false;

        while (!validDate) {
            alert.showAndWait();

            if (alert.getResult() == null) {
                // Dialog was closed without clicking Submit button
                return oldTask;
            }

            String title = titleTextField.getText();
            String description = descriptionTextArea.getText();
            String DueDate = dueDateTextField.getText();
            int importance = Integer.parseInt(choiceBox.getValue());

            if (!datePattern.matcher(DueDate).matches()) {
                Alert validationAlert = new Alert(Alert.AlertType.ERROR);
                validationAlert.setTitle("Invalid Due Date");
                validationAlert.setHeaderText(null);
                validationAlert.setContentText("Due date must be in the format YYYY-MM-DD. Please correct your input.");
                validationAlert.showAndWait();
            } else {
                newTask[0] = new Task(title, description, DueDate, importance);
                validDate = true;
            }
        }

        return newTask[0];
    }

}
