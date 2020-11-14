package it.polito.ai.server.dtos;

import it.polito.ai.server.entities.Course;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotEmpty;

@Data
public class ParametriDTO extends RepresentationModel<ParametriDTO> {

    @NotEmpty
    CourseDTO courseDTO;
    @NotEmpty
    ModelloVMDTO modelloVMDTO;
}
