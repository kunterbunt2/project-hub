package de.bushnaq.abdalla.projecthub.dao;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Proxy;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "versions")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Proxy(lazy = false)
@JsonIdentityInfo(
        scope = VersionDAO.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class VersionDAO extends AbstractTimeAwareDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude//help intellij debugger not to go into a loop
    private ProductDAO product;

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @JoinColumn(name = "version_id", referencedColumnName = "id", nullable = false, updatable = false)
//    @JsonManagedReference
    private List<ProjectDAO> projects = new ArrayList<>();

}
