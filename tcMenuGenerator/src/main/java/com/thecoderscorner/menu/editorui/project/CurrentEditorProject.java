/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinitionCollection;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.NoAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.NoEepromDefinition;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.nio.file.Paths;
import java.util.*;

import static com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform.ARDUINO_AVR;

/**
 * {@link CurrentEditorProject} represents the current project that is being edited by the UI. It supports the controller
 * with all the required functionality to both alter and persist project files. The controller should never perform any
 * write operations directly on the menu tree. Also controls the undo and redo buffers.
 */
public class CurrentEditorProject {

    public static final String NO_CREATOR_SELECTED = "";

    public enum EditorSaveMode { SAVE_AS, SAVE}

    private static final String TITLE = "TcMenu Designer";
    private static final int UNDO_BUFFER_SIZE = 200;
    private final CurrentProjectEditorUI editorUI;
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final ProjectPersistor projectPersistor;
    private final ConfigurationStorage configStore;
    private final Set<Integer> uncommittedItems = new HashSet<>();

    private MenuTree menuTree;
    private Optional<String> fileName = Optional.empty();
    private String description;
    private boolean dirty = true; // always assume dirty at first..
    private CodeGeneratorOptions generatorOptions;

    private final Deque<MenuItemChange> changeHistory = new LinkedList<>();
    private final Deque<MenuItemChange> redoHistory = new LinkedList<>();

    public CurrentEditorProject(CurrentProjectEditorUI editorUI, ProjectPersistor persistor, ConfigurationStorage storage) {
        this.editorUI = editorUI;
        projectPersistor = persistor;
        configStore = storage;
        cleanDown();
    }

    private void cleanDown() {
        menuTree = new MenuTree();
        fileName = Optional.empty();
        description = "";
        uncommittedItems.clear();
        generatorOptions = makeBlankGeneratorOptions();
        setDirty(false);
        updateTitle();
    }

    public ProjectPersistor getProjectPersistor() { return projectPersistor; }

    public void newProject() {
        if(checkIfWeShouldOverwrite()) {
            cleanDown();
            updateTitle();
            changeHistory.clear();
        }
    }

    private boolean checkIfWeShouldOverwrite() {
        if(isDirty()) {
            return editorUI.questionYesNo("Changes will be lost", "Do you want to discard the current menu?");
        }

        return true;
    }

    public CodeGeneratorOptions getGeneratorOptions() {
        return generatorOptions;
    }

    public void setGeneratorOptions(CodeGeneratorOptions generatorOptions) {
        setDirty(true);
        this.generatorOptions = generatorOptions;
    }

    public boolean openProject(String file) {
        try {
            if(checkIfWeShouldOverwrite()) {
                fileName = Optional.ofNullable(file);
                MenuTreeWithCodeOptions openedProject = projectPersistor.open(file);
                menuTree = openedProject.getMenuTree();
                description = openedProject.getDescription();
                generatorOptions = openedProject.getOptions();
                if (generatorOptions == null) generatorOptions = makeBlankGeneratorOptions();
                setDirty(false);
                updateTitle();
                changeHistory.clear();
                uncommittedItems.clear();
                return true;
            }
        } catch (IOException e) {
            fileName = Optional.empty();
            logger.log(Level.ERROR, "open operation failed on " + file, e);
            editorUI.alertOnError("Unable to open file", "The selected file could not be opened");
        }
        return false;
    }

    public boolean openProject() {
        if (checkIfWeShouldOverwrite()) {
            fileName = editorUI.findFileNameFromUser(true);
            fileName.ifPresent(this::openProject);
            return true;
        }
        return false;
    }

    public void saveProject(EditorSaveMode saveMode) {
        if(fileName.isEmpty() || saveMode == EditorSaveMode.SAVE_AS) {
            fileName = editorUI.findFileNameFromUser(false);
        }

        fileName.ifPresent((file)-> {
            try {
                uncommittedItems.clear();
                projectPersistor.save(file, description, menuTree, generatorOptions);
                setDirty(false);
            } catch (IOException e) {
                logger.log(Level.ERROR, "save operation failed on " + file, e);
                editorUI.alertOnError("Unable to save file", "Could not save file to chosen location");
            }
        });
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        setDirty(true);
        this.description = description;
    }

    public Set<Integer> getUncommittedItems() {
        return uncommittedItems;
    }

    public boolean isFileNameSet() {
        return fileName.isPresent();
    }

    public String getFileName() {
        return fileName.orElse("New");
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        if(this.dirty != dirty) {
            this.dirty = dirty;
            updateTitle();
        }
    }

    private void updateTitle() {
        var titlePrettyFile = "New";
        if(fileName.isPresent()) {
            titlePrettyFile = Paths.get(fileName.get()).getFileName().toString();
        }
        editorUI.setTitle(titlePrettyFile + (isDirty()?"* - ":" - ") + TITLE);
    }

    public MenuTree getMenuTree() {
        return menuTree;
    }

    public void applyCommand(EditedItemChange.Command command, MenuItem newItem) {
        applyCommand(command, newItem, menuTree.findParent(newItem));
    }

    public void applyCommand(EditedItemChange.Command command, MenuItem newItem, SubMenuItem parent) {
        if(command == EditedItemChange.Command.NEW) {
            uncommittedItems.add(newItem.getId());
        }
        else if(command == EditedItemChange.Command.REMOVE) {
            uncommittedItems.remove(newItem.getId());
        }

        MenuItem oldItem = changeHistory.stream()
                .filter(item-> item.getItem() != null && item.getItem().getId() == newItem.getId())
                .reduce((first, second) -> second)
                .map(MenuItemChange::getItem)
                .orElse(newItem);
        MenuItemChange change = new EditedItemChange(newItem, oldItem, parent, command);
        applyCommand(change);
    }

    public void applyCommand(MenuItemChange change) {
        changeHistory.add(change);

        if(changeHistory.size() > UNDO_BUFFER_SIZE) {
            changeHistory.removeFirst();
        }
        redoHistory.clear();

        change.applyTo(menuTree);
        setDirty(true);
    }

    public void undoChange() {
        if(changeHistory.isEmpty()) return;

        MenuItemChange change = changeHistory.removeLast();
        redoHistory.add(change);
        change.unApply(menuTree);
        setDirty(true);
    }

    public void redoChange() {
        if(redoHistory.isEmpty()) return;

        MenuItemChange change = redoHistory.removeLast();
        changeHistory.add(change);
        change.applyTo(menuTree);
        setDirty(true);
    }

    public boolean canRedo() {
        return !redoHistory.isEmpty();
    }

    public boolean canUndo() {
        return !changeHistory.isEmpty();
    }

    public CodeGeneratorOptions makeBlankGeneratorOptions() {
        return new CodeGeneratorOptionsBuilder()
                .withRecursiveNaming(configStore.isDefaultRecursiveNamingOn())
                .withSaveToSrc(configStore.isDefaultSaveToSrcOn())
                .codeOptions();
    }
}
