package sit.int221.nw1.models.server;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tasks")
public class Tasks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id", nullable = false)
    private Integer id;

    @Column(name = "task_title", nullable = false)
    @NotEmpty(message = "Title is required")
    private String title;

    @Column(name = "task_description")
    private String description;

    @Column(name = "task_assignees")
    private String assignees;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private Statuses status;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Boards boards;

    @CreationTimestamp
    @Column(name = "created_on", nullable = false , insertable = false, updatable = false)
    private ZonedDateTime createdOn;

    @UpdateTimestamp
    @Column(name = "updated_on", nullable = false, insertable = false)
    private ZonedDateTime updatedOn;
}
