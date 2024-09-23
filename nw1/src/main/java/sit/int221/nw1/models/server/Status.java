package sit.int221.nw1.models.server;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Getter
@Setter
@Entity
@Table(name = "status") // เปลี่ยนชื่อตารางให้สอดคล้องกับหลาย status
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Integer id;
    
    @Column(name = "status_name", nullable = false)
    private String name;

    @Column(name = "status_description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "board_id") // เชื่อมกับ Board
    private Boards board;
}
