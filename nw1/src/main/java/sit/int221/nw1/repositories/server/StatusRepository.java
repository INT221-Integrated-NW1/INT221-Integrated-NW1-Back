package sit.int221.nw1.repositories.server;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.nw1.models.server.Status;

import java.util.Optional;

public interface StatusRepository extends JpaRepository<Status, Integer> {
    Optional<Status> findByName(String name);
    boolean existsByName(String name);
}