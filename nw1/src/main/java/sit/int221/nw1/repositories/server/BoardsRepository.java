package sit.int221.nw1.repositories.server;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.nw1.models.server.Boards;

public interface BoardsRepository extends JpaRepository<Boards, String> {
}
