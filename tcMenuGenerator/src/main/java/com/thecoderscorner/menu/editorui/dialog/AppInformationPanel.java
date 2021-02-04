/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.controller.MenuEditorController;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginConfig;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.OnlineLibraryVersionDetector.ReleaseType;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.AVAILABLE_PLUGIN;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.CURRENT_PLUGIN;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class AppInformationPanel {
    public static final String LIBRARY_DOCS_URL = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/";
    public static final String GITHUB_PROJECT_URL = "https://github.com/davetcc/tcMenu/";

    private static final System.Logger logger = System.getLogger(AppInformationPanel.class.getSimpleName());
    private final MenuEditorController controller;
    private final ArduinoLibraryInstaller installer;
    private final CodePluginManager pluginManager;
    private final CurrentProjectEditorUI editorUI;
    private final LibraryVersionDetector libraryVersionDetector;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ConfigurationStorage storage;
    private VBox libraryInfoVBox;

    public AppInformationPanel(ArduinoLibraryInstaller installer, MenuEditorController controller,
                               CodePluginManager pluginManager, CurrentProjectEditorUI editorUI,
                               LibraryVersionDetector libraryVersionDetector, ConfigurationStorage storage) {
        this.installer = installer;
        this.controller = controller;
        this.pluginManager = pluginManager;
        this.editorUI = editorUI;
        this.libraryVersionDetector = libraryVersionDetector;
        this.storage = storage;
    }

    public Node showEmptyInfoPanel() {
        VBox vbox = new VBox();
        vbox.setSpacing(5);

        Label docsLbl = new Label("TcMenu designer");
        docsLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 110%;-fx-padding: 4px;");
        vbox.getChildren().add(docsLbl);
        // add the documentation links
        labelWithUrl(vbox, LIBRARY_DOCS_URL, "Browse docs and watch starter videos (F1 at any time)", "libdocsurl");
        labelWithUrl(vbox, GITHUB_PROJECT_URL, "Please give us a star on github if you like this tool", "githuburl");

        // add the library installation status

        var streamCombo = new ComboBox<ReleaseType>(FXCollections.observableList(
                List.of(ReleaseType.values()))
        );
        streamCombo.getSelectionModel().select(libraryVersionDetector.getReleaseType());
        streamCombo.getSelectionModel().selectedItemProperty().addListener((observableValue, oldVal, newVal) -> {
            libraryVersionDetector.changeReleaseType(newVal);
            checkAndReportItems(libraryInfoVBox);
        });

        var streamLabel = new Label("Plugin and library stream");
        streamLabel.setAlignment(Pos.CENTER_LEFT);
        streamLabel.setPadding(new Insets(4, 0, 0, 0));
        var hbox = new HBox(5.0, streamLabel, streamCombo);
        vbox.getChildren().add(hbox);

        var usingArduinoIde = new CheckBox("I am using Arduino IDE and have global libraries installed");
        usingArduinoIde.setSelected(storage.isUsingArduinoIDE());
        usingArduinoIde.selectedProperty().addListener((observableValue, oldVal, newVal) -> {
            storage.setUsingArduinoIDE(newVal);
            controller.presentInfoPanel();
        });
        vbox.getChildren().add(usingArduinoIde);

        var useRecursiveNaming = new CheckBox("Use fully qualified variable names for menus (submenu + name)");
        useRecursiveNaming.setSelected(controller.getProject().getGeneratorOptions().isNamingRecursive());
        useRecursiveNaming.selectedProperty().addListener((observableValue, oldVal, newVal) -> {
            controller.getProject().setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                    .withExisting(controller.getProject().getGeneratorOptions())
                    .withRecursiveNaming(newVal)
                    .codeOptions());
        });
        vbox.getChildren().add(useRecursiveNaming);

        libraryInfoVBox = new VBox(3.0);
        checkAndReportItems(libraryInfoVBox);
        vbox.getChildren().add(libraryInfoVBox);
        return vbox;
    }

    private void checkAndReportItems(VBox vbox) {
        vbox.getChildren().clear();
        vbox.getChildren().add(new Label("Reading version information.."));
        var fr = executor.submit(() -> {
            libraryVersionDetector.acquireVersions();
            Platform.runLater(this::redrawTheTitlePage);
        });
    }
    private void redrawTheTitlePage() {
        var vbox = libraryInfoVBox;
        vbox.getChildren().clear();
        if(installer.getArduinoDirectory().isEmpty() && storage.isUsingArduinoIDE()) {
            Label setManually = new Label("Set Arduino directory manually (Code -> Override Arduino Directory)");
            setManually.getStyleClass().add("libsNotOK");
            vbox.getChildren().add(setManually);
        }
        else if(storage.isUsingArduinoIDE() && installer.statusOfAllLibraries().isUpToDate()) {
            Label lblTcMenuOK = new Label("Embedded Arduino libraries all up-to-date");
            lblTcMenuOK.setId("tcMenuStatusArea");
            lblTcMenuOK.getStyleClass().add("libsOK");
            vbox.getChildren().add(lblTcMenuOK);
        }
        else if(storage.isUsingArduinoIDE()) {
            Label libsNotOK = new Label("Please update libraries from Arduino IDE (see About box for details)");
            libsNotOK.getStyleClass().add("libsNotOK");
            libsNotOK.setId("tcMenuStatusArea");
            vbox.getChildren().add(libsNotOK);
            var refreshButton = new Button("Refresh library status");
            refreshButton.setOnAction(actionEvent -> controller.presentInfoPanel());
            vbox.getChildren().add(refreshButton);
        }

        Label pluginLbl = new Label("Installed code generation plugins");
        pluginLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 110%;");
        vbox.getChildren().add(pluginLbl);

        boolean pluginUpdateNeeded = (pluginManager.getLoadedPlugins().size() == 0);

        for(var plugin : pluginManager.getLoadedPlugins()) {
            var availableVersion = getVersionOfLibraryOrError(plugin, AVAILABLE_PLUGIN);
            var installedVersion = getVersionOfLibraryOrError(plugin, CURRENT_PLUGIN);
            Label pluginInfoLbl = new Label("- " + plugin.getName() + ". Installed: "
                    + plugin.getVersion() + ", Available: " + availableVersion);
            pluginInfoLbl.getStyleClass().add("pluginInfoLbl");
            vbox.getChildren().add(pluginInfoLbl);
            pluginUpdateNeeded = pluginUpdateNeeded || !installedVersion.equals(availableVersion);
        }

        if(pluginUpdateNeeded) {
            var noPlugins = pluginManager.getLoadedPlugins().isEmpty();
            Button btn = new Button(noPlugins ? "No plugins installed, click to install" : "Click to update plugins to latest version");
            btn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
            vbox.getChildren().add(btn);
            btn.setOnAction(this::onUpgradeAction);
        }

    }

    private void onUpgradeAction(ActionEvent actionEvent) {
        executor.execute(new UpgradeTask((Button)actionEvent.getSource()));
        ((Button)actionEvent.getSource()).setDisable(true);
    }

    private VersionInfo getVersionOfLibraryOrError(CodePluginConfig plugin, InstallationType type) {
        try {
            return installer.getVersionOfLibrary(plugin.getModuleName(), type);
        } catch (IOException e) {
            return VersionInfo.ERROR_VERSION;
        }
    }

    private void labelWithUrl(VBox vbox, String urlToVisit, String title, String fxId) {
        Hyperlink docs = new Hyperlink(title);
        docs.setTooltip(new Tooltip(urlToVisit));
        docs.setStyle("-fx-vgap: 5px; -fx-border-insets: 0;");
        docs.setOnAction((event)->  editorUI.browseToURL(urlToVisit));
        docs.setId(fxId);
        vbox.getChildren().add(docs);
    }

    private class UpgradeTask implements Runnable {
        private final Button button;

        public UpgradeTask(Button source) {
            this.button = source;
        }

        private void updateUI(String status, boolean success) {
            Platform.runLater(() -> {
                button.setText(status);
                if(success)
                    button.setStyle("-fx-background-color: green;-fx-text-fill: white;");
                else
                    button.setStyle("-fx-background-color: red;-fx-text-fill: white;");
            });
        }

        @Override
        public void run() {
            try {
                List<String> allPlugins;
                if(pluginManager.getLoadedPlugins().isEmpty()) {
                    allPlugins = List.of("core-display", "core-remote");
                }
                else {
                    allPlugins = pluginManager.getLoadedPlugins().stream()
                            .map(CodePluginConfig::getModuleName)
                            .collect(Collectors.toList());
                }
                for(var pluginName : allPlugins) {
                    var availableVersion = installer.getVersionOfLibrary(pluginName, AVAILABLE_PLUGIN);
                    var installedVersion = installer.getVersionOfLibrary(pluginName, CURRENT_PLUGIN);
                    if(!installedVersion.equals(availableVersion)) {
                        updateUI("Updating plugin " + pluginName, true);
                        logger.log(INFO, "Updating " + pluginName);
                        libraryVersionDetector.upgradePlugin(pluginName, availableVersion);
                    }
                }
                updateUI("Refreshing plugins", true);
                pluginManager.reload();
                updateUI("Plugins reloaded", true);

            } catch (Exception e) {
                updateUI("Failed to update: " + e.getMessage(), false);
                logger.log(ERROR, "Update failed with exception", e);
            }
        }
    }
}
