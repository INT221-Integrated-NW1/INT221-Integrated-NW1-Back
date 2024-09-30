package sit.int221.nw1.models.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import sit.int221.nw1.models.client.Users;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "boards")
public class Boards {

    @Id
    @Column(name = "board_id", nullable = false, length = 10) // Keeping length 10 for ID
    private String boardId;

    @NotBlank(message = "boardName cannot be null or empty.")
    @Size(max = 120, message = "boardName length exceeds the maximum limit of 120.")
    @Column(name = "board_name", nullable = false, length = 120) // Updated length to 120
    private String boardName;

    @ManyToOne
    @JoinColumn(name = "oid", nullable = false)
    private User user;

    @OneToMany(mappedBy = "boards", fetch = FetchType.EAGER, cascade = CascadeType.ALL) // Added CascadeType for easier management
    @JsonIgnore
    private List<Tasks> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "boards", fetch = FetchType.EAGER, cascade = CascadeType.ALL) // Added CascadeType for boardStatuses too
    @JsonIgnore
    private List<BoardStatus> boardStatuses = new ArrayList<>();

    // Constructor that includes boardStatuses
    public Boards(String boardId, String boardName, User user, List<BoardStatus> boardStatuses) {
        this.boardId = boardId;
        this.boardName = boardName;
        this.user = user;
        this.boardStatuses = boardStatuses;
    }

    // Constructor without boardStatuses
    public Boards(String boardId, String boardName, User user) {
        this.boardId = boardId;
        this.boardName = boardName;
        this.user = user;
    }
}
