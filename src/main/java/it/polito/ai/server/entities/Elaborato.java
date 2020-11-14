package it.polito.ai.server.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Elaborato {

    @Id
    @GeneratedValue
    private Long id;

    private String stato;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private Byte[] contenuto;

    @ManyToOne
    @JoinColumn(name = "consegna_id")
    private Consegna consegna;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private Timestamp dataCaricamento;

    //Flag che indica se dopo la correzione è possibile ricaricare un nuovo elaborato
    //true nel caso sia possibile, false altrimenti.
    //nel caso sia false abbiamo l'attributo voto che indicherà la votazione finale, nel caso true sarà null.
    private String possibileRiconsegna;
    private String voto;


    public void setConsegna(Consegna consegna){
        if(this.consegna!=null){
            /*se sto cambiando da una consegna ad un altra (o a nessun altra) allora devo rimuovere this dalla consegna precedente*/
            this.consegna.getElaborati().remove(this);
        }


        if(consegna==null){
            this.consegna=null;
        }
        else{
            consegna.getElaborati().add(this);
            this.consegna=consegna;
        }
    }

  public void setStudente(Student student){
        if(this.student!=null){
            //se sto cambiando la proprietà dell'elaborato da uno studente ad un altro (o a nessun altro)
            //allora devo rimuovere this dallo studente precedente.
            this.student.getElaborati().remove(this);
        }


        if(student==null){
            this.student=null;
        }
        else{
            student.getElaborati().add(this);
            this.student=student;
        }
    }

}
