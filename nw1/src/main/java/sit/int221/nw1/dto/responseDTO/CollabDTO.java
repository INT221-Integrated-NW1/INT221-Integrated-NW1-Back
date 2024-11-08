package sit.int221.nw1.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CollabDTO {
    private String name;
    private String email;
    private String oid;
    private String accessRight;
    private String addedOn;
}