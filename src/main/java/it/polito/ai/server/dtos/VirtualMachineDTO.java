package it.polito.ai.server.dtos;

import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class VirtualMachineDTO extends RepresentationModel<VirtualMachineDTO> {

    private Long id;
    @Positive
    private int numVcpu;
    @Positive
    private int diskSpaceMB;
    @Positive
    private int ramMB;
    @NotEmpty
    private boolean attiva;

    private String creator;
}
