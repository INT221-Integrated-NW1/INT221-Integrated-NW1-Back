package sit.int221.nw1.repositories.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.int221.nw1.models.server.BoardStatus;
import sit.int221.nw1.models.server.Boards;

import java.util.List;
import java.util.Optional;

public interface BoardStatusRepository extends JpaRepository<BoardStatus, Integer> {
    List<BoardStatus> findBoardStatusesByBoards_BoardId(String boardId);
    Optional<BoardStatus> findBoardStatusesByBoards_BoardIdAndStatus_Id(String boardId, String statusId);

    @Query("SELECT b FROM Boards b WHERE b.boardId = :boardId")
    Boards findBoardById(@Param("boardId") String boardId);
}
