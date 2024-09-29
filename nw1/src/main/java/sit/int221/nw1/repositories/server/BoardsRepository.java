package sit.int221.nw1.repositories.server;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.nw1.models.server.Boards;

import java.util.List;

public interface BoardsRepository extends JpaRepository<Boards, String> {
    List<Boards> findByBoardName(String boardName);
    List<Boards> findByUserOid(String oid);

}
