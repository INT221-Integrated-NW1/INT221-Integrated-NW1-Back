package sit.int221.nw1.repositories.client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sit.int221.nw1.models.client.Users;


public interface UsersRepository extends JpaRepository<Users, String> {
    @Query("select u from Users u where u.username = :name")
    Users findByName(String name);

    Users findByOid(String oid);

}

