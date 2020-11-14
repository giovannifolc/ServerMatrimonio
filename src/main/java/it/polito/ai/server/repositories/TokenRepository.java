package it.polito.ai.server.repositories;

import it.polito.ai.server.entities.Student;
import it.polito.ai.server.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

;
import java.sql.Timestamp;
import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository <Token, String> {
    List<Token> findAllByExpiryDateBefore(Timestamp t); //per selezionare quelli scaduti
    List<Token> findAllByTeamId(Long teamId); //per selezionare quelli legati ad un team
    List<Token> findAllByStudent(Student student);

    boolean existsByStudentEqualsAndTeamIdEquals(Student student, Long teamId);
}
