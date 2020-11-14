package it.polito.ai.server.repositories;

import it.polito.ai.server.entities.Consegna;
import it.polito.ai.server.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ConsegnaRepository extends JpaRepository<Consegna, Long> {


    List<Consegna> findAllByScadenzaBefore(Timestamp t); //per selezionare quelli scaduti

    @Query("SELECT c FROM Consegna c INNER JOIN c.course course INNER JOIN course.students student WHERE course.name=:courseName AND student.id =:studentId")
    List<Consegna> getConsegneByStudentAndCourse(String studentId, String courseName);

    List<Consegna> getConsegnasByCourse(Course course);

}
