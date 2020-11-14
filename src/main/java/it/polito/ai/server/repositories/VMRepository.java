package it.polito.ai.server.repositories;

import it.polito.ai.server.entities.VirtualMachine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VMRepository extends JpaRepository<VirtualMachine, Long> {
}
