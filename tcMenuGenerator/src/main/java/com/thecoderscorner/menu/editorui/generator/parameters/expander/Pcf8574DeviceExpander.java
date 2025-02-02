package com.thecoderscorner.menu.editorui.generator.parameters.expander;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.*;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.*;

public class Pcf8574DeviceExpander extends IoExpanderDefinition {
    private final int i2cAddress;
    private final int intPin;
    private final String name;

    public Pcf8574DeviceExpander(String name, int i2cAddress, int intPin) {
        this.i2cAddress = i2cAddress;
        this.intPin = intPin;
        this.name = name;
    }

    @Override
    public String getNicePrintableName() {
        return String.format("PCF8574(0x%02x, %d)",  i2cAddress, intPin);
    }

    @Override
    public String getVariableName() {
        return "ioexp_" + name;
    }

    @Override
    public String getId() {
        return name;
    }

    public int getI2cAddress() {
        return i2cAddress;
    }

    public int getIntPin() {
        return intPin;
    }

    @Override
    public String toString() {
        return "pcf8574:" + name + ":" + i2cAddress + ":" + intPin;
    }

    @Override
    public Optional<String> generateCode() {
        return Optional.empty();
    }

    @Override
    public Optional<String> generateGlobal() {
        return Optional.of(String.format("IoAbstractionRef ioexp_%s = ioFrom8574(0x%02x, %d);", name, i2cAddress, intPin));
    }

    @Override
    public Optional<String> generateExport() {
        return Optional.of(String.format("extern IoAbstractionRef ioexp_%s;", name));
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.of(new HeaderDefinition("IoAbstractionWire.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
    }
}
