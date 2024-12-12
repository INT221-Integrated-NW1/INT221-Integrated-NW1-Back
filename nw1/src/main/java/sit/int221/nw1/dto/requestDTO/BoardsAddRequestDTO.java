package sit.int221.nw1.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BoardsAddRequestDTO {
    private String boardId;
    private String oid;
    @NotBlank(message = "Board name is required")
    @Size(min = 1, max = 120, message = "Board name must be between 1 to 120.")
    private String board_name;

}