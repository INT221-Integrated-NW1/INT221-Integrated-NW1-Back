package sit.int221.nw1.repositories.server;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.nw1.models.server.Statuses;

import java.util.Optional;

public interface StatusesRepository extends JpaRepository<Statuses, String> {
    boolean existsByName(String name);
    Optional<Statuses> findStatusesById(String Id);
    Optional<Statuses> findStatusesByBoardStatusesAndId(String BoardStatus ,String id);
}
