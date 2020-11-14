package it.polito.ai.server.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Course {
    @Id
    private String name;
    private String acronimo;
    private int min;
    private int max;
    private boolean enabled;

    @OneToMany(mappedBy = "course")
    List<Consegna> consegne = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name= "teacher_course", joinColumns = @JoinColumn(name="course_id"), inverseJoinColumns = @JoinColumn(name="teacher_id"))
    private List<Teacher> teachers = new ArrayList<>();

    @ManyToMany(mappedBy = "courses")
    private List<Student> students=new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE)
    private List<Team> teams = new ArrayList<>();

    @OneToOne(mappedBy = "course", cascade = {CascadeType.ALL})
    private ModelloVM modelloVM;


    public void addStudent(Student student){
            this.students.add(student);
            student.getCourses().add(this);
    }

    public void addTeam(Team team){
        /*
        non serve che faccia this.teams.add(course)
        perchè sfrutto il fatto che venga fatto da setCourse di Team
        anzi se lo facessi lo aggiungerei due volte
        */
        team.setCourse(this);
    }

    public void addConsegna(Consegna consegna){
         /*
        non serve che faccia this.consegne.add(course)
        perchè sfrutto il fatto che venga fatto da setCourse di Consegna
        anzi se lo facessi lo aggiungerei due volte
        */
        consegna.setCourse(this);
    }

    public void removeTeam(Team team){
        /*
        non serve che faccia this.teams.remove(course)
        perchè sfrutto il fatto che venga fatto da setCourse di Team
        anzi se lo facessi cercherei di toglierlo due volte
        */
        team.setCourse(null);
    }

    public void addTeacher(Teacher teacher){
        this.teachers.add(teacher);
        teacher.getCourses().add(this);
    }


    public void removeStudent(Student student) {
        this.students.remove(student);
        student.getCourses().remove(this);
    }

    public void setModelloVM(ModelloVM modelloVM){
        this.modelloVM = modelloVM;
        modelloVM.setCourse(this);
    }
}
