package it.polito.ai.server.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeacherDTO extends RepresentationModel<TeacherDTO> {
    @NotEmpty
    private String id;
    @NotEmpty
    private String name; /*lastName*/
    @NotEmpty
    private String firstName;
    @Email
    private String email;
    @NotEmpty
    private String password;
}
