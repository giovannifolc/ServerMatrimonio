package it.polito.ai.server.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Consegna {

    @Id
    @GeneratedValue
    private Long id;

    private String nomeConsegna;

    @Lob
    @Column(columnDefinition = "LONGBLOB", name = "contenuto")
    private Byte[] contenuto;

    @ManyToOne
    @JoinColumn(name = "course_name")
    private Course course;

    private Timestamp rilascio;
    private Timestamp scadenza;

    @OneToMany(mappedBy = "consegna", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<Elaborato> elaborati = new ArrayList<>();

    public void setCourse(Course course){
        if(this.course!=null){
            /*se sto cambiando da un corso ad un altro (o a nessun altro) allora devo rimuovere this dal corso di partenza*/
            this.course.getConsegne().remove(this);
        }

        if(course==null){
            this.course=null;
        }
        else{
            course.getConsegne().add(this);
            this.course=course;
        }
    }

    public void addElaborato(Elaborato elaborato){
         /*
        non serve che faccia this.elaborati.add(course)
        perchè sfrutto il fatto che venga fatto da setCourse di Elaborato
        anzi se lo facessi lo aggiungerei due volte
        */
        elaborato.setConsegna(this);
    }



}
