package it.polito.ai.server.repositories;

import it.polito.ai.server.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    public Optional<User> findByUsername(String username);

    public boolean existsByUsername(String username);

    public Optional<User> findByEmail(String email);

}
