package de.bushnaq.abdalla.projecthub.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Product extends AbstractTimeAware {

    private Long id;

    private String name;

    private List<Version> versions = new ArrayList<>();

    public void addVersion(Version version) {
        versions.add(version);
    }
}
