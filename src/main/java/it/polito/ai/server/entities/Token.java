package it.polito.ai.server.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    @Id
    private String id;
    private Long teamId;
    private Timestamp expiryDate;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;


    public void setStudent(Student student){
        if(this.student!=null){
            /*se sto cambiando da un corso ad un altro (o a nessun altro) allora devo rimuovere this dal corso di partenza*/
            this.student.getTokens().remove(this);
        }


        if(student==null){
            this.student=null;
        }
        else{
            student.getTokens().add(this);
            this.student=student;
        }
    }
}
