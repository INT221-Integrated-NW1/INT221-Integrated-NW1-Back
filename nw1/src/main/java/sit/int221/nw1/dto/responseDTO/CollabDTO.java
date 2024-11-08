package sit.int221.nw1.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CollabDTO {
    private String name;
    private String email;
    private String oid;
    private String accessRight;
    private ZonedDateTime addedOn;
}
