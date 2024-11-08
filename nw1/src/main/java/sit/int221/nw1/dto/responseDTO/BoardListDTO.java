package sit.int221.nw1.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardListDTO {
    private List<BoardsResponseDTO> PERSONAL_BOARD;
    private List<CollabsBoardsDTO> COLLABORATE_BOARD;
}
