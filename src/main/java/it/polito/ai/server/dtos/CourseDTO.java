package it.polito.ai.server.dtos;

import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
public class CourseDTO extends RepresentationModel<CourseDTO> {
    @NotEmpty
    private String name;
    @NotEmpty
    private String acronimo;
    @Positive
    private int min;
    @Positive
    private int max;
    private boolean enabled; //corso attivato
}
