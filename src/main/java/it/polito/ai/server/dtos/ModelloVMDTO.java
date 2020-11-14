package it.polito.ai.server.dtos;

import lombok.Data;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.ui.Model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Positive;

@Data
public class ModelloVMDTO extends RepresentationModel<ModelloVMDTO> {

    @Positive
    private int numVcpu;
    @Positive
    private int diskSpaceMB; // li consideriamo come mega byte
    @Positive
    private int ramMB;
    @Positive
    private int maxActiveVM;
    @Positive
    private int maxTotalVM;
}
