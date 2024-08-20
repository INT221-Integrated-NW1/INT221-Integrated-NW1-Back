package sit.int221.nw1.repositories.client;
import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.nw1.models.client.Users;

public interface UsersRepository extends JpaRepository<Users, String> {
    Users findByUsername(String username);
}

