package it.polito.ai.server.dtos;

import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
@Data
public class ConsegnaDTO extends RepresentationModel<ConsegnaDTO> {

    @NotNull
    private Long id;
    @NotNull
    private String nomeConsegna;
    @NotNull
    private Timestamp rilascio;
    @NotNull
    private Timestamp scadenza;

}
