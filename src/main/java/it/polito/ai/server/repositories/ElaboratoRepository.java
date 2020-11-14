package it.polito.ai.server.repositories;


import it.polito.ai.server.entities.Consegna;
import it.polito.ai.server.entities.Course;
import it.polito.ai.server.entities.Elaborato;
import it.polito.ai.server.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElaboratoRepository extends JpaRepository<Elaborato, Long> {

    List<Elaborato> findAllByConsegnaAndStudent(Consegna consegna, Student student);
    @Query("SELECT e FROM Elaborato e INNER JOIN e.consegna c INNER JOIN c.course course WHERE course.name=:courseName AND c.id =:consegnaId")
    List<Elaborato> getElaboratiByConsegnaAndCourse(Long consegnaId, String courseName);
    @Query("SELECT e FROM Elaborato e INNER JOIN e.consegna c INNER JOIN c.course course INNER JOIN e.student student WHERE course.name=:courseName AND c.id =:consegnaId AND student.id=:studentId")
    List<Elaborato> getElaboratiByConsegnaAndCourseAndStudentId(Long consegnaId, String courseName, String studentId);
}
