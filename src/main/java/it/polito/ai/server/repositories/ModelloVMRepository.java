package it.polito.ai.server.repositories;


import it.polito.ai.server.entities.ModelloVM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelloVMRepository extends JpaRepository<ModelloVM, Long > {
}
