package de.bushnaq.abdalla.projecthub.ui.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.theme.lumo.Lumo;

public class ThemeToggle extends Button {
    private boolean darkTheme = true; // Default to dark theme

    public ThemeToggle() {
        setIcon(new Icon(VaadinIcon.ADJUST));
        addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_TERTIARY_INLINE);
        addClickListener(e -> toggleTheme());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Load saved preference
        UI.getCurrent().getPage().executeJs(
                        "return localStorage.getItem('theme')")
                .then(String.class, theme -> {
                    ThemeList themeList = UI.getCurrent().getElement().getThemeList();
                    if (theme == null || "dark".equals(theme)) {
                        // Default to dark theme if no preference is set
                        themeList.add(Lumo.DARK);
                        darkTheme = true;
                    } else {
                        themeList.remove(Lumo.DARK);
                        darkTheme = false;
                    }
                    updateTooltip();
                });
    }

    private void toggleTheme() {
        ThemeList themeList = UI.getCurrent().getElement().getThemeList();
        if (themeList.contains(Lumo.DARK)) {
            themeList.remove(Lumo.DARK);
            darkTheme = false;
        } else {
            themeList.add(Lumo.DARK);
            darkTheme = true;
        }
        updateTooltip();

        // Store preference in localStorage
        UI.getCurrent().getPage().executeJs(
                "localStorage.setItem('theme', $0)",
                darkTheme ? "dark" : "light");
    }

    private void updateTooltip() {
        setTooltipText(darkTheme ? "Switch to light theme" : "Switch to dark theme");
    }
}