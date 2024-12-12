package sit.int221.nw1.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnCollabDTO {
    private String oid;
    private String boardID;
    private String collaboratorName;
    private String collaboratorEmail;
    private String accessRight;
}
