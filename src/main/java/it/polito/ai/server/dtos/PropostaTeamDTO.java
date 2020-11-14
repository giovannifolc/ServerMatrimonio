package it.polito.ai.server.dtos;

import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;
import java.util.Map;


@Data
public class PropostaTeamDTO extends RepresentationModel<PropostaTeamDTO> {

    TeamDTO teamDTO;
    List<StudentWithStatusDTO> studentWithStatusDTOS;
    String token;

}
