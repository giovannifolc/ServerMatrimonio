package it.polito.ai.server.repositories;

import it.polito.ai.server.entities.Course;
import it.polito.ai.server.entities.Student;
import it.polito.ai.server.entities.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

     List<Team> findAllByCourseAndName(Course course, String teamName);

     List<Team> getByMembersContaining(Student student);

     List<Team> getAllByCourseEquals(Course course);


}
