package sit.int221.nw1.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CollabsBoardsDTO {
    private String id;
    private String name;
    private String visibility;
    private String accessRight;
    private OwnerDTO owner;
}