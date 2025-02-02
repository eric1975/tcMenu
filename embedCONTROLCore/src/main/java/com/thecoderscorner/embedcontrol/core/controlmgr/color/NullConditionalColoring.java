package com.thecoderscorner.embedcontrol.core.controlmgr.color;

import com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent;
import com.thecoderscorner.menu.domain.state.PortableColor;

import javax.sound.sampled.Control;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.*;

public class NullConditionalColoring implements ConditionalColoring {
    public PortableColor backgroundFor(RenderingStatus status, ColorComponentType ty) {
        return ControlColor.WHITE;
    }

    public PortableColor foregroundFor(RenderingStatus status, ColorComponentType ty) {
        return ControlColor.BLACK;
    }
}