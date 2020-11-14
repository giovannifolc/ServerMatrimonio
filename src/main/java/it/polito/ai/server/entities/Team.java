package it.polito.ai.server.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Team {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private int status;
    private String proponenteId;

    @ManyToOne
    @JoinColumn(name="course_id")
    private Course course;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name= "team_student", joinColumns = @JoinColumn(name="team_id"), inverseJoinColumns = @JoinColumn(name="student_id"))
    private List<Student> members= new ArrayList<>();

    @OneToMany(mappedBy = "team")
    private List<VirtualMachine> virtualMachines = new ArrayList<>();


    public void setCourse(Course course){
        if(this.course!=null){
            /*se sto cambiando da un corso ad un altro (o a nessun altro) allora devo rimuovere this dal corso di partenza*/
            this.course.getTeams().remove(this);
        }


        if(course==null){
            this.course=null;
        }
        else{
            course.getTeams().add(this);
            this.course=course;
        }
    }

    public void addStudent(Student student){
        this.members.add(student);
        student.getTeams().add(this);
    }

    public void removeStudent(Student student){
        this.members.remove(student);
        student.getTeams().remove(this);
    }

    public void addVirtualMachine(VirtualMachine vm){
         /*
        non serve che faccia this.virtualMachines.add(vm)
        perchè sfrutto il fatto che venga fatto da setTeam di VirtualMachine
        anzi se lo facessi cercherei di aggiungerlo due volte
        */
        vm.setTeam(this);

    }




}
