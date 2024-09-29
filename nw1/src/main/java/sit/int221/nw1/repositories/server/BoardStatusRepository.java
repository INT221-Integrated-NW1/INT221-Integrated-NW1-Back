package sit.int221.nw1.repositories.server;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.nw1.models.server.BoardStatus;

import java.util.List;
import java.util.Optional;

public interface BoardStatusRepository extends JpaRepository<BoardStatus, Integer> {
    List<BoardStatus> findBoardStatusesByBoards_BoardId(String boardId);
}
