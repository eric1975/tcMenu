package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.cli.CreateProjectCommand;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.cli.CreateProjectCommand.SupportedPlatform.*;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class NewProjectController {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    public RadioButton newOnlyRadio;
    public RadioButton createNewRadio;
    public TextField projectNameField;
    public ComboBox<EmbeddedPlatform> platformCombo;
    public Button dirChooseButton;
    public Button createButton;
    public TextField locationTextField;
    public CheckBox cppMainCheckbox;
    private Optional<String> maybeDirectory;
    private CurrentEditorProject project;
    private ConfigurationStorage storage;

    public void initialise(ConfigurationStorage storage, EmbeddedPlatforms platforms, CurrentEditorProject project) {
        this.storage = storage;
        maybeDirectory = storage.getArduinoOverrideDirectory();
        this.project = project;
        maybeDirectory.ifPresentOrElse(
                dir -> locationTextField.setText(dir),
                () -> locationTextField.setText("No directory")
        );

        platformCombo.setItems(FXCollections.observableList(platforms.getEmbeddedPlatforms()));
        platformCombo.getSelectionModel().select(0);
        reEvaluteAll();
    }

    public void onModeChange(ActionEvent actionEvent) {
        reEvaluteAll();
    }

    private void reEvaluteAll() {
        boolean newOnlyMode = newOnlyRadio.isSelected();
        projectNameField.setDisable(newOnlyMode);
        dirChooseButton.setDisable(newOnlyMode);
        platformCombo.setDisable(newOnlyMode);
        cppMainCheckbox.setDisable(newOnlyMode);

        if(newOnlyMode) {
            createButton.setDisable(false);
        }
        else {
            var ok = (!StringHelper.isStringEmptyOrNull(projectNameField.getText()) && maybeDirectory.isPresent());
            createButton.setDisable(!ok);
        }
    }

    public void onDirectorySelection(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose base location");
        maybeDirectory.ifPresent(dir ->  directoryChooser.setInitialDirectory(new File(dir)));
        File possibleFile = directoryChooser.showDialog(createButton.getScene().getWindow());

        if(possibleFile != null && Files.exists(possibleFile.toPath())) {
            maybeDirectory = Optional.ofNullable(possibleFile.toString());
            locationTextField.setText(possibleFile.toString());
        }
    }

    public void onCancel(ActionEvent actionEvent) {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }

    public void onCreate(ActionEvent actionEvent) {
        boolean newOnlyMode = newOnlyRadio.isSelected();
        if(newOnlyMode) {
            if(!passDirtyCheck()) return;
            project.newProject();

        }
        else if(maybeDirectory.isPresent() && !StringHelper.isStringEmptyOrNull(projectNameField.getText())) {
            if(!passDirtyCheck()) return;
            try {
                String projName = projectNameField.getText();
                CreateProjectCommand.createNewProject(
                        Paths.get(maybeDirectory.get()), projName,
                        cppMainCheckbox.isSelected(),
                        asSupportedPlatform(platformCombo.getSelectionModel().getSelectedItem()),
                        s -> logger.log(INFO, s)
                );
                Path emfFileName = Paths.get(maybeDirectory.get(), projName, projName + ".emf");
                project.openProject(emfFileName.toString());
            } catch (Exception e) {
                logger.log(ERROR, "Failure processing create new project", e);
                var alert = new Alert(Alert.AlertType.ERROR, "Error during create project", ButtonType.CLOSE);
                BaseDialogSupport.getJMetro().setScene(alert.getDialogPane().getScene());
                alert.showAndWait();
            }
        }
        else {
            var alert = new Alert(Alert.AlertType.WARNING, "Please ensure all fields are populated", ButtonType.CLOSE);
            BaseDialogSupport.getJMetro().setScene(alert.getDialogPane().getScene());
            alert.showAndWait();
            return; // avoid closing.
        }

        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }

    private boolean passDirtyCheck() {
        if(project.isDirty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "The project has not been saved, data may be lost", ButtonType.YES, ButtonType.NO);
            BaseDialogSupport.getJMetro().setScene(alert.getDialogPane().getScene());
            alert.setHeaderText("Project is unsaved, proceed anyway?");
            var result = alert.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.YES) {
                project.setDirty(false);
                return true;
            }
            else return false;
        }
        return true;
    }

    private CreateProjectCommand.SupportedPlatform asSupportedPlatform(EmbeddedPlatform selectedItem) {
        return switch(selectedItem.getBoardId()) {
            case "ARDUINO32" -> ARDUINO32;
            case "ARDUINO_ESP8266" -> ARDUINO_ESP8266;
            case "ARDUINO_ESP32" -> ARDUINO_ESP32;
            case "MBED_RTOS" -> MBED_RTOS;
            default -> ARDUINO_AVR;
        };
    }

    public void onTextChanged(KeyEvent keyEvent) {
        reEvaluteAll();
    }

    public void onPlatformChanged(ActionEvent actionEvent) {
        reEvaluteAll();
    }
}
