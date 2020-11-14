package it.polito.ai.server.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentWithStatusDTO extends RepresentationModel<StudentWithStatusDTO> {

    private String id;
    private String name;
    private String firstName;
    private String status;
}
