package it.polito.ai.server.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    private String id;
    private String name; /*lastName*/
    private String firstName;
    @Lob
    @Column(columnDefinition = "LONGBLOB", name = "image")
    private Byte[] image;

    @OneToMany(mappedBy="student")
    private List<Token> tokens;
    // Aggiungere creatore?


    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name= "student_course", joinColumns = @JoinColumn(name="student_id"), inverseJoinColumns = @JoinColumn(name="course_name"))
    private List<Course> courses = new ArrayList<>();

    @ManyToMany(mappedBy = "members")
    private List<Team> teams= new ArrayList<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.EAGER)
    private  List<Elaborato> elaborati = new ArrayList<>();

    @ManyToMany(mappedBy = "owners")
    private List<VirtualMachine> virtualMachines = new ArrayList<>();

    public void addCourse(Course course){
            this.courses.add(course);
            course.getStudents().add(this);
    }



    public void addVirtualMachine(VirtualMachine vm){
        this.virtualMachines.add(vm);
        vm.getOwners().add(this);
    }

    public void removeVirtualMachine(VirtualMachine vm){
        this.virtualMachines.remove(vm);
        vm.getOwners().remove(this);
    }

    public void addElaborato(Elaborato elaborato){
        /* non serve che faccia this.elaborati.add(token)
        perchè sfrutto il fatto che venga fatto da setStudent di Elaborato
        anzi se lo facessi lo aggiungerei due volte */
        elaborato.setStudent(this);
    }

    public void addTeam(Team team){
        this.teams.add(team);
        team.getMembers().add(this);
    }

    public void removeTeam(Team team){
        this.teams.remove(team);
        team.getMembers().remove(this);
    }

    public void addToken(Token token){
         /*
        non serve che faccia this.tokens.add(token)
        perchè sfrutto il fatto che venga fatto da setStudent di Token
        anzi se lo facessi lo aggiungerei due volte */
        token.setStudent(this);
    }

    /*public Byte[] readElaborato(Consegna consegna){

        boolean flag;

        Optional<Elaborato> elaborato = elaborati.stream()
                .filter(el->el.getConsegna().equals(consegna) && el.getStato().equals("NULL"))
                .findFirst();

        if(!elaborato.isPresent()){
            return null;
        }

        elaborato.get().setStato("LETTO");
        return elaborato.get().getContenuto();
    }
*/



}
