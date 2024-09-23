package sit.int221.nw1.repositories.server;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.nw1.models.server.Statuses;

import java.util.Optional;

public interface StatusesRepository extends JpaRepository<Statuses, Integer> {
    Statuses findByName(String name);

    Optional<Statuses> findByIdAndBoardsBoardId(Integer Id , String boardId);


}
