package sit.int221.nw1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.nw1.entities.Status;
import sit.int221.nw1.entities.Tasks;

import java.util.List;

public interface TasksRepository extends JpaRepository<Tasks, Integer>{
    List<Tasks> findByStatus(Status status);
    List<Tasks> findByStatusNameIn(List<String> statusNames);
}
