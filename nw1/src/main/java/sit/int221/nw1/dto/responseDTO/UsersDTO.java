package sit.int221.nw1.dto.responseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsersDTO {
    private String oid;
    private String name;
    private String username;
    private String password;
    private String email;
    private String role;
}