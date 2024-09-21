package sit.int221.nw1.dto.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class filteredTaskDTO {

        private Integer id;
        private String title;
        private String assignees;
        private String status;

    }

