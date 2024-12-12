package sit.int221.nw1.models.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Getter
@Setter
@Table(name="boardStatus")
public class BoardStatus {


    @Id
    @Column(name = "bsId", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bsId;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    @JsonIgnore
    private Boards boards;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    @JsonIgnore
    private Statuses status;

    public BoardStatus(Boards board, Statuses status) {
        this.boards = board;
        this.status = status;
    }
}