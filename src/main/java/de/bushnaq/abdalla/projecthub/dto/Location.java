package de.bushnaq.abdalla.projecthub.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * {@code LegalLocation} class
 * This class stores the work contract working location that will be used to determine the official public holidays.
 * A user might be working in Melbourne Australia and wil get paid non-working days as everybody else in Melbourne,
 * next year the same user might be working in Germany and will receive paid non-working days like everybody else in Germany.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class Location extends AbstractTimeAware {

    private String    country;
    private Long      id;
    private LocalDate start;
    private String    state;

    public Location(String country, String state, LocalDate start) {
        this.country = country;
        this.state   = state;
        this.setStart(start);
    }

    String getKey() {
        return "L-" + id;
    }


}
