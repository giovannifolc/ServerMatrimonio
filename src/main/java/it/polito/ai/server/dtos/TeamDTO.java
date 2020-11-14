package it.polito.ai.server.dtos;

import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.sql.Timestamp;

@Data
public class TeamDTO extends RepresentationModel<TeamDTO> {
    private Long id;
    private String name;
    private int status;
    private String proponenteId;
}
