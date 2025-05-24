package de.bushnaq.abdalla.projecthub.ui.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;

public class ConfirmDialog extends Dialog {
    private static final long          serialVersionUID = 4165888461696341970L;
    //    private final Logger logger = LoggerFactory.getLogger(ConfirmDialog.class);
    private              ButtonVariant confirmButtonTheme;

    public Registration addListener(ComponentEventListener<ConfirmEvent> listener) {
        if (this instanceof Component) {
            return ComponentUtil.addListener((Component) this, ConfirmEvent.class, listener);
        } else {
            throw new IllegalStateException(String.format("The class '%s' doesn't extend '%s'. " + "Make your implementation for the method '%s'.",
                    getClass().getName(), Component.class.getSimpleName(), "addClickListener"));
        }
    }

    public void open(String caption, String message, String confirmButtonText, String rejectButtonText) {
        setModal(true);
        this.setCloseOnEsc(false);
        this.setCloseOnOutsideClick(false);
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidth("400px");
        add(verticalLayout);

        Html captionLable = new Html("<h3>" + caption + "</h3>");
        verticalLayout.add(captionLable);

        Div content = new Div();
        content.getElement().setProperty("innerHTML", message);
        verticalLayout.add(content);

        HorizontalLayout footer = new HorizontalLayout();
        verticalLayout.add(footer);
        Button confirmButton = new Button(confirmButtonText);
        confirmButton.setId("tt-confirm-button");
        confirmButton.setWidth("25%");
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        if (confirmButtonTheme != null) {
            confirmButton.addThemeVariants(confirmButtonTheme);
        }
        confirmButton.addClickListener(event -> {
            //            logger.trace("confirmButton was clicked!");
            fireEvent(new ConfirmEvent(confirmButton, false));//inform the server
            this.close();
        });
        Div spacer = new Div();
        spacer.setWidth("50%");
        Button cancelButton = new Button(rejectButtonText, event -> {
            this.close();
        });
        cancelButton.setId("tt-cancel-button");
        cancelButton.setWidth("25%");
        //        cancelButton.addClickShortcut(Key.ENTER);
        footer.add(cancelButton, spacer, confirmButton);
        footer.setWidth("100%");
        super.open();
    }

    public void setConfirmButtonTheme(ButtonVariant buttonVariant) {
        confirmButtonTheme = buttonVariant;
    }
}
