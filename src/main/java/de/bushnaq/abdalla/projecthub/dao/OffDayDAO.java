package de.bushnaq.abdalla.projecthub.dao;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.bushnaq.abdalla.projecthub.dto.OffDayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Proxy;

@Entity
@Table(name = "off_days")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Proxy(lazy = false)
@JsonIdentityInfo(
        scope = OffDayDAO.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class OffDayDAO extends AbstractDateRangeDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private OffDayType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude//help intellij debugger not to go into a loop
    private UserDAO user;
}
