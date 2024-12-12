package sit.int221.nw1.repositories.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int221.nw1.models.server.Boards;

import java.util.List;


public interface BoardsRepository extends JpaRepository<Boards, String> {
    List<Boards> findByUserOid(String oid);
    List<Boards> findByVisibility(String visibility);
}
