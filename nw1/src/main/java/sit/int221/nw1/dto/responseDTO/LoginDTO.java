package sit.int221.nw1.dto.responseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {
    private String username;
    private String encodedPassword;
}
