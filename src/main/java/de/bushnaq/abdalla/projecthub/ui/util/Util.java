package de.bushnaq.abdalla.projecthub.ui.util;

import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;

import java.util.Arrays;

public class Util {
    private static final int DEFAULT_NOTIFICATION_DURATION = 60000;//1m

    public static DatePickerI18n createEnglishDatePickerIl8n() {
        DatePickerI18n datePickerI18n = new DatePickerI18n();
        datePickerI18n.setFirstDayOfWeek(1);//monday
//        datePickerI18n.setWeek("Week");
//        datePickerI18n.setCalendar("Calendar");
//        datePickerI18n.setClear("Clear");
        datePickerI18n.setToday("Today");
        datePickerI18n.setCancel("Cancel");
        datePickerI18n.setWeekdays(Arrays.asList("Sunday", "Monday", "Tuesday", "Wedneday", "thursday", "Friday", "Saturday"));
        datePickerI18n.setWeekdaysShort(Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"));
        datePickerI18n.setMonthNames(
                Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"));
        return datePickerI18n;
    }

    public static void notifyError(String message, Throwable e) {
        //        Div content = new Div();
        //
        //        Style style = content.getStyle();
        //        style.set("color", "red");
        //        content.setText(message + " " + e.getMessage());
        //        Notification notification = new Notification(content);
        //        notification.setDuration(DEFAULT_NOTIFICATION_DURATION);
        //        notification.setPosition(Position.BOTTOM_START);
        //        notification.open();
        Notification.show(message + " " + e.getMessage(), DEFAULT_NOTIFICATION_DURATION, Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    public static void notifyinfo(String message) {
        Notification.show(message, DEFAULT_NOTIFICATION_DURATION, Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

}
