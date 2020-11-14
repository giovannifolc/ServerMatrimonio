package it.polito.ai.server.entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class ModelloVM {

    @Id
    @GeneratedValue
    private Long id;

    /*
      Tutti i parametri valgono singolarmente per ogni gruppo
     */
    private int numVcpu;
    private int diskSpaceMB; // li consideriamo come mega byte
    private int ramMB;
    private int maxActiveVM;
    private int maxTotalVM;

    @OneToOne
    @JoinColumn(name = "course_name")
    private Course course;



}
