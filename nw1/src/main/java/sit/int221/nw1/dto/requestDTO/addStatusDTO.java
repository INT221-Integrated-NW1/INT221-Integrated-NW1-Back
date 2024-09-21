package sit.int221.nw1.dto.requestDTO;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class addStatusDTO {
    private Integer id;
    @Size(min = 1,max = 50)
    private String name;
    @Size(min = 1,max = 200)
    private String description;
}