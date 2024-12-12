package sit.int221.nw1.dto.requestDTO;

// UpdateVisibilityRequest.java
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateVisibilityRequest {

    @NotBlank(message = "Visibility is required")
    @Pattern(regexp = "PRIVATE|PUBLIC", message = "Visibility must be either 'PRIVATE' or 'PUBLIC'")
    private String visibility;

}
