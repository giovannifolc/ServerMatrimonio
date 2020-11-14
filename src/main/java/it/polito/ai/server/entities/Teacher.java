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
public class Teacher {
    @Id
    private String id;
    private String name; /*lastName*/
    private String firstName;
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private Byte[] image;



    @ManyToMany(mappedBy = "teachers")
    private List<Course> courses = new ArrayList<>();

    public void addCourse(Course course){
        this.courses.add(course);
        course.getTeachers().add(this);
    }

}
