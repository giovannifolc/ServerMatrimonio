package it.polito.ai.server.dtos;

import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
@Data
public class ElaboratoDTO extends RepresentationModel<ElaboratoDTO> {

    @NotNull
    private Long id;
    @NotNull
    private String stato;

    private Timestamp dataCaricamento;
    @NotNull
    private String studentId;

    private String possibileRiconsegna;

    private String voto;

}
