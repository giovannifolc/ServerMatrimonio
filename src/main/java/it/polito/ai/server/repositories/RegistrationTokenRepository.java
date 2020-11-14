package it.polito.ai.server.repositories;

import it.polito.ai.server.entities.RegistrationToken;
import it.polito.ai.server.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface RegistrationTokenRepository extends JpaRepository<RegistrationToken, String> {
    List<RegistrationToken> findAllByExpiryDateBefore(Timestamp timestamp);
}
