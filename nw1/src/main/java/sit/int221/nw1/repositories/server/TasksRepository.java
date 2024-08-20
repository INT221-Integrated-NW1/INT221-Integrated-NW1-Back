package sit.int221.nw1.repositories.server;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.nw1.models.server.Status;
import sit.int221.nw1.models.server.Tasks;

import java.util.List;

public interface TasksRepository extends JpaRepository<Tasks, Integer>{
    List<Tasks> findByStatus(Status status);
    List<Tasks> findByStatusNameIn(List<String> statusNames);
    List<Tasks> findByStatusIdIn(List<Integer> statusIds);
}
