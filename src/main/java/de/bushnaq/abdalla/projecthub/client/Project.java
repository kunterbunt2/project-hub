package de.bushnaq.abdalla.projecthub.client;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Project extends TimeAware {
    private Long          id;
    private String        name;
    private String        requester;
    private List<Version> versions;
}