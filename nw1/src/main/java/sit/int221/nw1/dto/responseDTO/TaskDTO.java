package sit.int221.nw1.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;
import sit.int221.nw1.models.server.Status;
import sit.int221.nw1.models.server.Statuses;

@Getter
@Setter
public class TaskDTO {
   private Integer id;
    private String title;
    private String assignees;
    private String status;
    private String boardName;

}