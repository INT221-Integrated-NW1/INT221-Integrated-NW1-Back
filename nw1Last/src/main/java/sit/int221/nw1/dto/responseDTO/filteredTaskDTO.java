package sit.int221.nw1.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;
import sit.int221.nw1.entities.Status;
@Getter
@Setter
public class filteredTaskDTO {

        private Integer id;
        private String title;
        private String assignees;
        private String status;

    }

