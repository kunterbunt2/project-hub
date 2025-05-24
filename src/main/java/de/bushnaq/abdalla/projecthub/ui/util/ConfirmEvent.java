package de.bushnaq.abdalla.projecthub.ui.util;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;

public class ConfirmEvent extends ComponentEvent<Button> {

    private static final long serialVersionUID = -1944511558166789382L;

    public ConfirmEvent(Button source, boolean fromClient) {
        super(source, fromClient);
    }

}