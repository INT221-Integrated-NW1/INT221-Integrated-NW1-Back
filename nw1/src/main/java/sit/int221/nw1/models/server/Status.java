package sit.int221.nw1.models.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "status", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Tasks> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "status")
    @JsonIgnore
    private List<BoardStatus> boardStatuses;

    public int getNoOfTasks() {
        return tasks.size();
    }
}
