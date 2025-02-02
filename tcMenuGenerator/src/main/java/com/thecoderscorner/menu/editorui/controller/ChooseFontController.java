package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.dialog.AppInformationPanel.FONTS_GUIDE_URL;
import static com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition.FontMode.*;

public class ChooseFontController {
    public RadioButton adafruitFontSel;
    public RadioButton adafruitLocalFontSel;
    public RadioButton staticFontSel;
    public RadioButton largeNumSelect;
    public Button okButton;
    public TextField fontVarField;
    public TextField fontNumField;
    public RadioButton defaultFontSelect;
    public Label errorField;
    private Optional<FontDefinition> result = Optional.empty();

    public void initialise(String currentSelection) {
        var maybeFont = FontDefinition.fromString(currentSelection);
        if(maybeFont.isPresent()) {
            var font = maybeFont.get();
            fontVarField.setText(font.getFontName());
            fontNumField.setText(Integer.toString(font.getFontNumber()));
            switch (font.getFontMode()) {
                case DEFAULT_FONT -> defaultFontSelect.setSelected(true);
                case ADAFRUIT -> adafruitFontSel.setSelected(true);
                case NUMBERED -> largeNumSelect.setSelected(true);
                case AVAILABLE -> staticFontSel.setSelected(true);
                case ADAFRUIT_LOCAL -> adafruitLocalFontSel.setSelected(true);
            }
        }
        else {
            adafruitFontSel.setSelected(true);
            fontVarField.setText("MyFont");
            fontNumField.setText("1");
        }
    }

    public void onFontDefinitionsDocs(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo(FONTS_GUIDE_URL);
    }

    public void onCreatePressed(ActionEvent actionEvent) {
        FontDefinition.FontMode mode;
        if(adafruitFontSel.isSelected()) mode = ADAFRUIT;
        else if(adafruitLocalFontSel.isSelected()) mode = ADAFRUIT_LOCAL;
        else if(largeNumSelect.isSelected()) mode = NUMBERED;
        else if(staticFontSel.isSelected()) mode = AVAILABLE;
        else mode = DEFAULT_FONT;

        int mag;
        try {
            mag = Integer.parseInt(fontNumField.getText());
        }
        catch(Exception e) {
            errorField.setText("Only use integers for font number / size");
            return;
        }

        if(fontVarField.getText().isBlank() && (mode == ADAFRUIT || mode == ADAFRUIT_LOCAL ||  mode == AVAILABLE)) {
            errorField.setText("Adafruit and static fonts require a font name");
            return;
        }

        if(!fontVarField.getText().isBlank() && (mode == DEFAULT_FONT ||  mode == NUMBERED)) {
            errorField.setText("Default or numbered fonts are not named");
            return;
        }

        result = Optional.of(new FontDefinition(mode, fontVarField.getText(), mag));

        closeIt();
    }

    public void onCancelPressed(ActionEvent actionEvent) {
        result = Optional.empty();
        closeIt();
    }

    private void closeIt() {
        Stage s = (Stage) largeNumSelect.getScene().getWindow();
        s.close();
    }

    public Optional<FontDefinition> getResult() {
        return result;
    }
}
