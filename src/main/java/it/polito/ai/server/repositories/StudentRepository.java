package it.polito.ai.server.repositories;

import it.polito.ai.server.entities.Course;
import it.polito.ai.server.entities.Student;
import it.polito.ai.server.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    /*prendo uno studente se nei corsi ha course*/
    public List<Student> getByCoursesContaining(Course course);

    public List<Student> getByCoursesNotContaining(Course course);


}
