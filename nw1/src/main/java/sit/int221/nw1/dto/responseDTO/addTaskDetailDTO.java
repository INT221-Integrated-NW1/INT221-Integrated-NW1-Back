package sit.int221.nw1.dto.responseDTO;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;
import sit.int221.nw1.entities.Status;

@Getter
@Setter
public class addTaskDetailDTO {
    private  Integer id;
    @Size(min = 1,max = 100)
    private String title;

    @Size(min = 1,max = 500)
    private String description;

    @Size(min = 1,max = 30)
    private String assignees;

    private Status status;
}
