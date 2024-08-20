package sit.int221.nw1.dto.requestDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class deleteTaskDTO {
    private Integer id;
    private String title;
    private String assignees;
    private String status;
}

