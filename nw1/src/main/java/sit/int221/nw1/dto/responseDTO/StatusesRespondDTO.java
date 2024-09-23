package sit.int221.nw1.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusesRespondDTO {
    private Integer statusId;
    private String name;
    private String description;
    private String boards;
}

