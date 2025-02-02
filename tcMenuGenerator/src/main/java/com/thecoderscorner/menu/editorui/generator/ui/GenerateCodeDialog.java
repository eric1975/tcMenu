/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */
/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.dialog.SelectAuthenticatorTypeDialog;
import com.thecoderscorner.menu.editorui.dialog.SelectEepromTypeDialog;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinitionCollection;
import com.thecoderscorner.menu.editorui.generator.parameters.expander.CustomDeviceExpander;
import com.thecoderscorner.menu.editorui.generator.parameters.expander.InternalDeviceExpander;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.generator.validation.IoExpanderPropertyValidationRules;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport.createDialogStateAndShow;
import static com.thecoderscorner.menu.editorui.generator.core.SubSystem.*;
import static com.thecoderscorner.menu.editorui.generator.ui.UICodePluginItem.UICodeAction.CHANGE;
import static com.thecoderscorner.menu.editorui.generator.ui.UICodePluginItem.UICodeAction.SELECT;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static javafx.collections.FXCollections.observableArrayList;

public class GenerateCodeDialog {
    private final static String DEFAULT_THEME_ID = "b186c809-d9ef-4ca8-9d4b-e4780a041ccc"; // manual theme, works for most displays

    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

    private final CodePluginManager manager;
    private final CurrentProjectEditorUI editorUI;
    private final CurrentEditorProject project;
    private final CodeGeneratorRunner runner;
    private final EmbeddedPlatforms platforms;

    private List<CodePluginItem> displaysSupported;
    private List<CodePluginItem> inputsSupported;
    private List<CodePluginItem> remotesSupported;
    private List<CodePluginItem> themesSupported;
    private final List<String> initialPlugins = new ArrayList<>();

    private UICodePluginItem currentDisplay;
    private UICodePluginItem currentTheme;
    private UICodePluginItem currentInput;
    private final List<UICodePluginItem> currentRemotes = new ArrayList<>();

    private ComboBox<EmbeddedPlatform> platformCombo;
    private Stage mainStage;

    private Label themeTitle;
    private VBox centerPane;
    private Label eepromTypeLabel;
    private Label authModeLabel;

    public GenerateCodeDialog(CodePluginManager manager, CurrentProjectEditorUI editorUI,
                              CurrentEditorProject project, CodeGeneratorRunner runner,
                              EmbeddedPlatforms platforms) {
        this.manager = manager;
        this.editorUI = editorUI;
        this.project = project;
        this.runner = runner;
        this.platforms = platforms;

    }

    public void showCodeGenerator(Stage stage, boolean modal)  {
        this.mainStage = stage;
        BorderPane pane = new BorderPane();
        pane.getStyleClass().add("background");

        placeDirectoryAndEmbeddedPanels(pane);
        filterChoicesByPlatform(platformCombo.getValue());

        centerPane = new VBox(5);
        addTitleLabel(centerPane, "Select the input type:");
        CodeGeneratorOptions genOptions = project.getGeneratorOptions();
        var allItems = project.getMenuTree().getAllMenuItems();

        CodePluginItem itemInput = findItemByUuidOrDefault(inputsSupported, genOptions.getLastInputUuid(), Optional.empty());
        CodePluginItem itemDisplay = findItemByUuidOrDefault(displaysSupported, genOptions.getLastDisplayUuid(), Optional.empty());
        CodePluginItem itemTheme = findItemByUuidOrDefault(themesSupported, genOptions.getLastThemeUuid(), Optional.of(DEFAULT_THEME_ID));

        addToInitialSourceFilesForRemovalCheck(itemInput);
        addToInitialSourceFilesForRemovalCheck(itemTheme);
        addToInitialSourceFilesForRemovalCheck(itemDisplay);

        setAllPropertiesToLastValues(itemInput);
        setAllPropertiesToLastValues(itemDisplay);
        if(itemTheme != null) {
            setAllPropertiesToLastValues(itemTheme);
        }

        currentInput = new UICodePluginItem(manager, itemInput, CHANGE, this::onInputChange, editorUI, allItems, 0, "inputPlugin");
        currentInput.getStyleClass().add("uiCodeGen");
        centerPane.getChildren().add(currentInput);

        addTitleLabel(centerPane, "Select the display type:");
        currentDisplay = new UICodePluginItem(manager, itemDisplay, CHANGE, this::onDisplayChange, editorUI, allItems, 0, "displayPlugin");
        currentDisplay.getStyleClass().add("uiCodeGen");
        centerPane.getChildren().add(currentDisplay);

        if(itemTheme != null) {
            themeTitle = addTitleLabel(centerPane, "Select a theme:");
            currentTheme = new UICodePluginItem(manager, itemTheme, CHANGE, this::onThemeChange, editorUI, allItems, 0, "themePlugin");
            currentTheme.setId("currentThemeUI");
            currentTheme.getStyleClass().add("uiCodeGen");
            if (!currentDisplay.getItem().isThemeNeeded()) {
                currentTheme.setVisible(false);
                currentTheme.setManaged(false);
                themeTitle.setVisible(false);
                themeTitle.setManaged(false);
            }
            centerPane.getChildren().add(currentTheme);
        }
        else currentTheme = null;

        BorderPane remoteLabelPane = new BorderPane();
        Label titleLbl = new Label("Select IoT/remote capabilities:");
        titleLbl.setStyle("-fx-font-size: 16px; -fx-opacity: 0.6; -fx-font-weight: bold;");
        remoteLabelPane.setLeft(titleLbl);

        Button addRemoteCapabilityButton = new Button("Add another IoT/remote");
        remoteLabelPane.setRight(addRemoteCapabilityButton);
        addRemoteCapabilityButton.setOnAction(this::produceAnotherRemoteCapability);
        centerPane.getChildren().add(remoteLabelPane);

        List<String> remoteIds = genOptions.getLastRemoteCapabilitiesUuids();
        if(remoteIds != null && !remoteIds.isEmpty()) {
            int count = 0;
            for(var remoteId : remoteIds) {
                CodePluginItem itemRemote = findItemByUuidOrDefault(remotesSupported, remoteId, Optional.of(CoreCodeGenerator.NO_REMOTE_ID));
                addToInitialSourceFilesForRemovalCheck(itemRemote);
                setAllPropertiesToLastValues(itemRemote);
                String pluginId = "remotePlugin" + count;
                var currentRemote = new UICodePluginItem(manager, itemRemote, CHANGE, this::onRemoteChange, editorUI, allItems, count, pluginId);
                currentRemote.getStyleClass().add("uiCodeGen");
                centerPane.getChildren().add(currentRemote);
                count++;
                currentRemotes.add(currentRemote);
            }
        }
        else {
            // did not exist before this, must be first run.
            CodePluginItem itemRemote = findItemByUuidOrDefault(remotesSupported, "", Optional.empty());
            String pluginId = "remotePlugin0";
            var currentRemote = new UICodePluginItem(manager, itemRemote, CHANGE, this::onRemoteChange, editorUI, allItems, 0, pluginId);
            currentRemote.getStyleClass().add("uiCodeGen");
            centerPane.getChildren().add(currentRemote);
        }

        ButtonBar buttonBar = new ButtonBar();
        Button generateButton = new Button("Generate Code");
        generateButton.setDefaultButton(true);
        generateButton.setOnAction(this::onGenerateCode);
        generateButton.setId("generateButton");
        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(this::onCancel);
        buttonBar.getButtons().addAll(cancelButton, generateButton);

        ScrollPane scrollPane = new ScrollPane(centerPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        pane.setCenter(scrollPane);
        pane.setOpaqueInsets(new Insets(5));
        pane.setBottom(buttonBar);
        pane.setPrefSize(800, 750);
        BorderPane.setMargin(buttonBar, new Insets(5));
        BorderPane.setMargin(pane.getTop(), new Insets(5));


        var title = "Code Generator:" + project.getFileName();
        createDialogStateAndShow(stage, pane, title, modal);
    }

    private void produceAnotherRemoteCapability(ActionEvent actionEvent) {
        var itemRemote = findItemByUuidOrDefault(remotesSupported, CoreCodeGenerator.NO_REMOTE_ID, Optional.empty());
        setAllPropertiesToLastValues(itemRemote);
        var allItems = project.getMenuTree().getAllMenuItems();
        String pluginId = "remotePlugin" + currentRemotes.size();
        var currentRemote = new UICodePluginItem(manager, itemRemote, CHANGE, this::onRemoteChange, editorUI,
                allItems, currentRemotes.size(), pluginId);
        currentRemote.setId("currentRemoteUI-" + currentRemotes.size());
        currentRemote.getStyleClass().add("uiCodeGen");
        currentRemotes.add(currentRemote);
        centerPane.getChildren().add(currentRemote);
    }

    private void addToInitialSourceFilesForRemovalCheck(CodePluginItem pluginItem) {
        if(pluginItem == null) return;
        for(var sf : pluginItem.getRequiredSourceFiles()) {
            if(sf.isOverwritable()) {
                initialPlugins.add(sf.getFileName());
            }
        }
    }

    private Label addTitleLabel(Pane vbox, String text) {
        Label titleLbl = new Label(text);
        titleLbl.setStyle("-fx-font-size: 16px; -fx-opacity: 0.6; -fx-font-weight: bold;");
        vbox.getChildren().add(titleLbl);
        return titleLbl;
    }

    private CodePluginItem findItemByUuidOrDefault(List<CodePluginItem> items, String uuid, Optional<String> maybeDefault) {
        if(items.size() == 0) throw new IllegalStateException("No plugins have been loaded");
        return items.stream().filter(item -> item.getId().equals(uuid)).findFirst().orElseGet(() -> {
            CodePluginItem ret;
            if(maybeDefault.isPresent()) {
                ret = items.stream().filter(item -> item.getId().equals(maybeDefault.get())).findFirst().orElse(items.get(0));
            }
            else ret = items.get(0);
            return ret;
        });
    }

    private void placeDirectoryAndEmbeddedPanels(BorderPane pane) {
        GridPane embeddedPane = new GridPane();
        embeddedPane.setHgap(5);
        embeddedPane.setVgap(3);
        embeddedPane.add(new Label("Embedded Platform"), 0, 0);
        embeddedPane.add(new Label("Application Details"), 0, 2);
        embeddedPane.add(new Label("EEPROM Support"), 0, 3);
        embeddedPane.add(new Label("Pin & Authenticator"), 0, 4);

        platformCombo = new ComboBox<>(observableArrayList(platforms.getEmbeddedPlatforms()));
        embeddedPane.add(platformCombo, 1, 0, 2, 1);
        EmbeddedPlatform platform = getLastEmbeddedPlatform();
        platformCombo.getSelectionModel().select(platform);
        platformCombo.setId("platformCombo");
        platformCombo.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldVal, newVal) -> filterChoicesByPlatform(newVal)
        );

        var appName = project.getGeneratorOptions().getApplicationName();
        var appUuid = project.getGeneratorOptions().getApplicationUUID();
        if(appName == null || appName.isEmpty()) {
            appName = "New app";
        }
        Label appNameLabel = new Label(appName + " - " + appUuid);
        appNameLabel.setId("appNameLabel");
        embeddedPane.add(appNameLabel, 1, 2);

        eepromTypeLabel = new Label(project.getGeneratorOptions().getEepromDefinition().toString());
        eepromTypeLabel.setId("eepromTypeLabel");
        var eepromTypeButton = new Button("Change EEPROM");
        eepromTypeButton.setOnAction(this::onEepromButtonPressed);
        eepromTypeButton.setPrefWidth(120);
        eepromTypeButton.setId("eepromTypeButton");
        embeddedPane.add(eepromTypeLabel, 1, 3);
        embeddedPane.add(eepromTypeButton, 2, 3);

        authModeLabel = new Label(project.getGeneratorOptions().getAuthenticatorDefinition().toString());
        authModeLabel.setId("authModeLabel");
        var authModeButton = new Button("Change Auth");
        authModeButton.setId("authModeButton");
        authModeButton.setPrefWidth(120);
        authModeButton.setOnAction(this::onAuthenticatorButtonPressed);
        embeddedPane.add(authModeLabel, 1, 4);
        embeddedPane.add(authModeButton, 2, 4);

        ColumnConstraints column1 = new ColumnConstraints(120);
        ColumnConstraints column2 = new ColumnConstraints(400, 500, 999, Priority.ALWAYS, HPos.LEFT, true);
        ColumnConstraints column3 = new ColumnConstraints(120);
        embeddedPane.getColumnConstraints().add(column1);
        embeddedPane.getColumnConstraints().add(column2);
        embeddedPane.getColumnConstraints().add(column3);
        pane.setTop(embeddedPane);
    }

    private void onAuthenticatorButtonPressed(ActionEvent actionEvent) {
        var dlg = new SelectAuthenticatorTypeDialog(mainStage, project.getGeneratorOptions().getAuthenticatorDefinition(),true);
        dlg.getResultOrEmpty().ifPresent(authSel -> {
            authModeLabel.setText(authSel.toString());
            project.setGeneratorOptions(new CodeGeneratorOptionsBuilder().withExisting(project.getGeneratorOptions())
                    .withAuthenticationDefinition(authSel)
                    .codeOptions());
        });
    }

    private void onEepromButtonPressed(ActionEvent actionEvent) {
        var dlg = new SelectEepromTypeDialog(mainStage, project.getGeneratorOptions().getEepromDefinition(),true);
        dlg.getResultOrEmpty().ifPresent(newRom -> {
            eepromTypeLabel.setText(newRom.toString());
            project.setGeneratorOptions(new CodeGeneratorOptionsBuilder().withExisting(project.getGeneratorOptions())
                    .withEepromDefinition(newRom)
                    .codeOptions());
        });
    }

    private EmbeddedPlatform getLastEmbeddedPlatform() {
        var platform = EmbeddedPlatform.ARDUINO_AVR;
        String lastPlatform = project.getGeneratorOptions().getEmbeddedPlatform();
        try {
            platform = platforms.getEmbeddedPlatformFromId(lastPlatform);
        }
        catch (Exception e) {
            logger.log(ERROR, "Chosen platform could not be loaded back." + lastPlatform, e);
            editorUI.alertOnError(
                    "Platform changed",
                    "The platform " + lastPlatform + "is no longer available, defaulting to AVR"
            );
        }
        return platform;
    }

    private void filterChoicesByPlatform(EmbeddedPlatform newVal) {
        displaysSupported = manager.getPluginsThatMatch(newVal, DISPLAY);
        inputsSupported = manager.getPluginsThatMatch(newVal, INPUT);
        remotesSupported = manager.getPluginsThatMatch(newVal, REMOTE);
        themesSupported = manager.getPluginsThatMatch(newVal, THEME);
    }

    private void setAllPropertiesToLastValues(CodePluginItem itemToSetFor) {
        if(itemToSetFor == null || itemToSetFor.getProperties() == null) return;

        for(var prop :  itemToSetFor.getProperties()) {
            var lastProp = project.getGeneratorOptions().getLastProperties().stream()
                    .filter(p -> prop.getName().equals(p.getName()) && prop.getSubsystem().equals(p.getSubsystem()))
                    .findFirst();
            if (lastProp.isPresent()) {
                prop.setLatestValue(lastProp.get().getLatestValue());
            } else {
                prop.resetToInitial();
            }
        }

        ensureIoFullyDeclared(itemToSetFor);
    }

    private void ensureIoFullyDeclared(CodePluginItem pluginItem) {
        logger.log(INFO, "Checking for unmapped IO devices: " + pluginItem.getDescription());
        var codeOptions = project.getGeneratorOptions();

        // find any IO device declarations that do not match to an entry in the expanders
        var anyIoWithoutEntries = pluginItem.getProperties().stream()
                .filter(prop -> prop.getValidationRules() instanceof IoExpanderPropertyValidationRules)
                .filter(prop -> codeOptions.getExpanderDefinitions().getDefinitionById(prop.getLatestValue()).isEmpty())
                .collect(Collectors.toList());

        // nothing to do if list is empty
        if(anyIoWithoutEntries.isEmpty()) {
            logger.log(INFO, "All IO devices mapped");
            return;
        }

        var allExpanders = new HashSet<>(codeOptions.getExpanderDefinitions().getAllExpanders());
        // now we iterate through the unmapped expanders, which must be from prior to the automated support.
        for (var customIo : anyIoWithoutEntries) {
            if (StringHelper.isStringEmptyOrNull(customIo.getLatestValue())) {
                // for empty strings, the previous assumption was using device IO. This is now explicitly defined
                // as deviceIO
                customIo.setLatestValue(InternalDeviceExpander.DEVICE_ID);
                logger.log(INFO, "Device being mapped as internal: " + customIo.getLatestValue());
            } else {
                // otherwise, previously the assumption was using a custom defined expander in the sketch, now we'll
                // actually add that to the sketch.
                allExpanders.add(new CustomDeviceExpander(customIo.getLatestValue()));
                logger.log(INFO, "Device being mapped as custom: " + customIo.getLatestValue());
            }
        }

        project.setGeneratorOptions(
                new CodeGeneratorOptionsBuilder().withExisting(codeOptions)
                        .withExpanderDefinitions(new IoExpanderDefinitionCollection(allExpanders))
                        .codeOptions()
        );
        logger.log(INFO, "Done mapping all IO devices");
    }

    private void onCancel(ActionEvent actionEvent) {
        var stage = (Stage)(currentInput.getScene().getWindow());
        stage.close();
    }

    private void onGenerateCode(ActionEvent actionEvent) {
        saveCodeGeneratorChanges();
        runner.startCodeGeneration(mainStage, platformCombo.getSelectionModel().getSelectedItem(),
                                   Paths.get(project.getFileName()).getParent().toString(),
                                   getAllPluginsForConversion(),
                                   initialPlugins,
                                   true);

        var stage = (Stage)(currentInput.getScene().getWindow());
        stage.close();
    }

    private void saveCodeGeneratorChanges() {
        var allProps = new ArrayList<CreatorProperty>();
        allProps.addAll(currentDisplay.getItem().getProperties());
        allProps.addAll(currentInput.getItem().getProperties());
        for(var remote : currentRemotes) {
            allProps.addAll(remote.getItem().getProperties());
        }
        if(currentTheme != null) {
            allProps.addAll(currentTheme.getItem().getProperties());
        }

        String themeId = currentTheme != null ? currentTheme.getItem().getId() : "";
        var opts = project.getGeneratorOptions();
        project.setGeneratorOptions(new CodeGeneratorOptionsBuilder().withExisting(opts)
                .withPlatform(platformCombo.getSelectionModel().getSelectedItem().getBoardId())
                .withDisplay(currentDisplay.getItem().getId())
                .withInput(currentInput.getItem().getId())
                .withRemotes(currentRemotes.stream().map(r-> r.getItem().getId()).collect(Collectors.toList()))
                .withTheme(themeId)
                .withProperties(allProps)
                .codeOptions()
        );
    }

    private List<CodePluginItem> getAllPluginsForConversion() {
        var allPlugins = new ArrayList<CodePluginItem>();
        allPlugins.add(currentDisplay.getItem());
        allPlugins.add(currentInput.getItem());
        for(var pl : currentRemotes) {
            allPlugins.add(pl.getItem());
        }
        if(currentDisplay.getItem().isThemeNeeded() && currentTheme != null) {
            allPlugins.add(currentTheme.getItem());
        }
        return allPlugins;
    }

    private void onDisplayChange(UICodePluginItem uiPlugin, CodePluginItem item) {
        logger.log(INFO, "Action fired on display");
        selectPlugin(displaysSupported, "Display", (uiPluginLocal, pluginItem)-> {
            currentDisplay.setItem(pluginItem);
            changeProperties();
            if(currentTheme == null) return;
            boolean themeNeeded = currentDisplay.getItem().isThemeNeeded();
            currentTheme.setVisible(themeNeeded);
            currentTheme.setManaged(themeNeeded);
            themeTitle.setVisible(themeNeeded);
            themeTitle.setManaged(themeNeeded);
        });
    }

    private void changeProperties() {
        List<CodePluginItem> creators = getAllPluginsForConversion();

        creators.stream()
                .filter(p -> p != null && p.getProperties().size() > 0)
                .forEach(this::setAllPropertiesToLastValues);
    }

    private void onRemoteChange(UICodePluginItem uiPlugin, CodePluginItem item) {
        if(item == null) {
            logger.log(INFO, "Remove fired on remote");
            currentRemotes.remove(uiPlugin);
            centerPane.getChildren().remove(uiPlugin);
            changeProperties();

            for(int i=0; i<currentRemotes.size(); i++) {
                currentRemotes.get(i).setItemIndex(i);
            }

            return;
        }
        logger.log(INFO, "Action fired on remote");
        selectPlugin(remotesSupported, "Remote", (uiPluginLocal, pluginItem)-> {
            if (uiPluginLocal.getItemIndex() < currentRemotes.size()) {
                currentRemotes.get(uiPlugin.getItemIndex()).setItem(pluginItem);
                changeProperties();
            }
        });
    }

    private void onThemeChange(UICodePluginItem uiPlugin, CodePluginItem item) {
        logger.log(INFO, "Action fired on theme");
        selectPlugin(themesSupported, "Theme", (uiLocal, pluginItem)-> {
            currentTheme.setItem(pluginItem);
            changeProperties();
        });
    }

    private void onInputChange(UICodePluginItem uiPlugin, CodePluginItem item) {
        logger.log(INFO, "Action fired on input");
        selectPlugin(inputsSupported, "Input", (uiLocal, pluginItem)-> {
            currentInput.setItem(pluginItem);
            changeProperties();
        });
    }

    private void selectPlugin(List<CodePluginItem> pluginItems, String changeWhat, BiConsumer<UICodePluginItem, CodePluginItem> eventHandler) {

        Popup popup = new Popup();
        List<UICodePluginItem> listOfComponents = pluginItems.stream()
                .map(display -> new UICodePluginItem(manager, display, SELECT, (ui, item) -> {
                    popup.hide();
                    eventHandler.accept(ui, item);
                }, 0, "pluginSel_" + display.getId()))
                .collect(Collectors.toList());

        VBox vbox = new VBox(5);
        addTitleLabel(vbox, "Select the " + changeWhat + " to use:");
        vbox.getChildren().addAll(listOfComponents);

        BorderPane pane = new BorderPane();
        pane.setCenter(vbox);
        vbox.getStyleClass().add("popupWindow");

        var scroll = new ScrollPane(pane);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPrefSize(700, 600);
        popup.getContent().add(scroll);
        popup.setAutoHide(true);
        popup.setOnAutoHide(event -> popup.hide());
        popup.setHideOnEscape(true);
        var stage = (Stage)(currentInput.getScene().getWindow());
        popup.show(stage);
    }
}
