package sit.int221.nw1.dto.responseDTO;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import sit.int221.nw1.models.server.Status;

@Getter
@Setter
public class addDTORespond {
        private  Integer id;
        @Size(min = 1,max = 100)
        private String title;

        @Size(min = 1,max = 500)
        private String description;

        @Size(min = 1,max = 30)
        private String assignees;

        private Status status;
    }

