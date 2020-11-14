package it.polito.ai.server.repositories;

import it.polito.ai.server.entities.Course;
import it.polito.ai.server.entities.Student;
import it.polito.ai.server.entities.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {


    boolean existsByNameAndStudentsContaining(String courseName, Student student);

     List<Course> getByStudentsContaining(Student student);

    @Query("SELECT s FROM Student s INNER JOIN s.teams t INNER JOIN t.course c WHERE c.name=:courseName")
     List<Student> getStudentsInTeams(String courseName);

    @Query("SELECT st FROM Student st INNER JOIN st.courses co WHERE co.name=:courseName AND st NOT IN(SELECT s FROM Student s INNER JOIN s.teams t INNER JOIN t.course c WHERE c.name=:courseName)")
     List<Student> getStudentsNotInTeams(String courseName);

    List<Course> getByTeachersContaining(Teacher teacher);
}
