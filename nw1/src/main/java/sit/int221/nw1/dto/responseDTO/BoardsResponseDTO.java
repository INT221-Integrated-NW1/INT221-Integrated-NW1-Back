package sit.int221.nw1.dto.responseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sit.int221.nw1.models.server.User;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardsResponseDTO {
    private String boardId;
    private String boardName;
    private String visibility;
    private UserResponseDTO user;

    }


