package sit.int221.nw1.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;
import sit.int221.nw1.entities.Status;

@Getter
@Setter
public class TaskDTO {
    private Integer id;
    private String title;
    private String assignees;
    private Status status;

}