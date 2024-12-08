package sit.int221.nw1.repositories.server;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.nw1.models.server.Statuses;
import sit.int221.nw1.models.server.Tasks;

import java.util.List;
import java.util.Optional;

public interface TasksRepository extends JpaRepository<Tasks, Integer>{
    List<Tasks> findByStatus(Statuses status);
    List<Tasks> findByBoardsBoardId(String boardId);
    Optional<Tasks> findTasksById(Integer id);
    List<Tasks> findTasksByBoards_BoardIdAndStatus_Id(String boardId, String statusId);


}
