package it.polito.ai.server.dtos;

import com.opencsv.bean.CsvBindByName;
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
public class StudentWithTeamInfoDTO extends RepresentationModel<StudentWithTeamInfoDTO> {

    @CsvBindByName
    @NotEmpty
    private String id;
    @CsvBindByName
    @NotEmpty
    private String name; /*lastName*/
    @CsvBindByName
    @NotEmpty
    private String firstName;
    @Email
    private String email;
    @NotEmpty
    private String password;

    private String teamNameForSelectedCourse;

}
