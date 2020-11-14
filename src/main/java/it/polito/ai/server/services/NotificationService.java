package it.polito.ai.server.services;

import it.polito.ai.server.dtos.TeamDTO;
import it.polito.ai.server.entities.Student;
import org.springframework.security.access.prepost.PreAuthorize;

import java.sql.Timestamp;
import java.util.List;

public interface NotificationService {
    void sendMessage(String address, String subject, String body);
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    boolean confirm(String token); // per confermare la partecipazione al gruppo
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    boolean reject(String token); //per esprimere il proprio diniego a partecipare
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    void notifyTeam(TeamDTO dto, List<Student> students, Timestamp expiryDate);
    void confirmRegistration(String token);
}
