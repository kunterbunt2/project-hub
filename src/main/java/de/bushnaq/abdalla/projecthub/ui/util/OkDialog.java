package de.bushnaq.abdalla.projecthub.ui.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;

public class OkDialog extends Dialog {
    //    private final Logger logger = LoggerFactory.getLogger(OkDialog.class);

    private static final long serialVersionUID = 4587574970790565747L;

    public Registration addListener(ComponentEventListener<ConfirmEvent> listener) {
        if (this instanceof Component) {
            return ComponentUtil.addListener(this, ConfirmEvent.class, listener);
        } else {
            throw new IllegalStateException(String.format("The class '%s' doesn't extend '%s'. " + "Make your implementation for the method '%s'.",
                    getClass().getName(), Component.class.getSimpleName(), "addClickListener"));
        }
    }

    public void open(String caption, String message) {
        setModal(true);
        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(true);
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidth("400px");
        add(verticalLayout);

        Html captionLable = new Html("<h1>" + caption + "</h1>");
        verticalLayout.add(captionLable);

        Div content = new Div();
        content.getElement().setProperty("innerHTML", message);
        verticalLayout.add(content);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        verticalLayout.add(horizontalLayout);
        horizontalLayout.setWidth("100%");
        super.open();
    }
}
