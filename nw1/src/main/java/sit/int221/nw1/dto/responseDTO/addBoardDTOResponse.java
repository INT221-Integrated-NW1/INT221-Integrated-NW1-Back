package sit.int221.nw1.dto.responseDTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sit.int221.nw1.models.server.User;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class addBoardDTOResponse {
        private String id;
        private String name;
        private String visibility;
        private ZonedDateTime createdOn;
        private UserResponseDTO user;
}
