package sit.int221.nw1.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class TasksDTO {
    private Integer id;
    private String title;
    private String description;
    private String assignees;
    private String status;
    private String boardName;
    private ZonedDateTime createdOn;
    private ZonedDateTime  updatedOn;

}