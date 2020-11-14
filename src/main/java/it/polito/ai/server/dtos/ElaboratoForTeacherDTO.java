package it.polito.ai.server.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ElaboratoForTeacherDTO extends RepresentationModel<ElaboratoForTeacherDTO> {

    @NotNull
    private Long id;
    @NotNull
    private String studentId;
    @NotNull
    private String name;
    @NotNull
    private String firstName;
    @NotNull
    private String stato;
    @NotNull
    private Timestamp dataCaricamento;

    private String possibileRiconsegna;

    private String voto;

}
