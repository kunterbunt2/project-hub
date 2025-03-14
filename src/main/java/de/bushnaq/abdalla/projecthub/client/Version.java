package de.bushnaq.abdalla.projecthub.client;


import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Version extends TimeAware {

    private Long         id;
    private String       name;
    private List<Sprint> sprints = new ArrayList<>();

}
