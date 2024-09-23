package sit.int221.nw1.models.server;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "status")
public class Statuses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Integer id  ;  // Ensure this field exists

    @Size(min=1, max=50)
    @Column(name = "status_name")
    private String name;
    @Size(min=1, max=200)
    @Column(name = "status_description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Boards boards;

}
