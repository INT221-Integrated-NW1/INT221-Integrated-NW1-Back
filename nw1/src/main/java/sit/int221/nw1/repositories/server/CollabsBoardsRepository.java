package sit.int221.nw1.repositories.server;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.nw1.models.server.Collabs;

import java.util.List;

public interface CollabsBoardsRepository extends JpaRepository<Collabs,Integer>{
    boolean existsByOidAndBoardBoardId (String oid, String boardId);

    Collabs findCollabsByOidAndBoardId (String oid, String boardId);

    List<Collabs> findCollabsByOid(String oid);
}
