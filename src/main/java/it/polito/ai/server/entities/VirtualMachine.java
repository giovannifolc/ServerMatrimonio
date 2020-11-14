package it.polito.ai.server.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VirtualMachine {

    @Id
    @GeneratedValue
    private Long id;

    private int numVcpu;
    private int diskSpaceMB; // li consideriamo come mega byte
    private int ramMB;


    @Transient
    private Byte[] screenshot;


    private boolean attiva = false;

    @ManyToOne
    @JoinColumn(name= "team_id")
    private Team team;

    @ManyToMany
    @JoinTable(name="student_VM", joinColumns = {@JoinColumn(name = "virtual_machine_id") }, inverseJoinColumns = {@JoinColumn(name = "student_id")})
    private List<Student> owners = new ArrayList<>();


    private String creator;


    /**
     *
     * @param student
     *
     * Serve per aggiungere gli studenti che hanno il ruolo di proprietario (owner) della virtual machine.
     * Inizialmente il ruolo viene dato allo studente che crea la vm, può essere poi esteso ad altri membri del gruppo
     *
     */
    public void addOwner(Student student){
        this.owners.add(student);
        student.getVirtualMachines().add(this);
    }

    public void setTeam(Team team){
        if(this.team!=null){
            /*se sto cambiando da un corso ad un altro (o a nessun altro) allora devo rimuovere this dal corso di partenza*/
            this.team.getVirtualMachines().remove(this);
        }


        if(team==null){
            this.team = null;
        }
        else{
            team.getVirtualMachines().add(this);
            this.team=team;
        }
    }

    public Byte[] getScreen() throws IOException {

        if(screenshot == null){
           /* File resource2 = new ClassPathResource(
                    "./pom.xml").getFile();*/
            File resource = new File(
                    "./src/main/resources/img/vm.png");
            screenshot = ArrayUtils.toObject(Files.readAllBytes(resource.toPath()));
        }

        return screenshot;
    }

    public Byte[] attivaVM() throws IOException {
        attiva = true;
        if(screenshot == null){
           /* File resource2 = new ClassPathResource(
                    "./pom.xml").getFile();*/
            File resource = new File(
                    "./src/main/resources/img/vm.png");
            screenshot = ArrayUtils.toObject(Files.readAllBytes(resource.toPath()));
        }
        return screenshot;
    }

    public void disattivaVM(){
        attiva = false;
    }


}
