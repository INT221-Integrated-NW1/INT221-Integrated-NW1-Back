package sit.int221.nw1.dto.responseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardsResponseDTO {
    private String boardId;
    private String board_name;
    private OwnerDTO owner;  // Include the owner object
}

