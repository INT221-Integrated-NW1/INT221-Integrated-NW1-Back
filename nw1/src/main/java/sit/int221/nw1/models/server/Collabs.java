package sit.int221.nw1.models.server;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "collaborators")
public class Collabs {
    @Id
    @Column(name = "collab_id",nullable = false,length = 10)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer collabId;

    @Column(name = "board_id")
    private String boardId;

    @Column(name = "oid")
    private String oid;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_right")
    private AccessRight accessRight;
    public enum AccessRight {
        READ,
        WRITE
    }

    @JsonIgnore
    @UpdateTimestamp
    @Column(name = "added_on")
    private ZonedDateTime addedOn;

    @Column(name = "email")
    private String email;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false, insertable = false, updatable = false)
    private Boards board;

    @ManyToOne
    @JoinColumn(name = "oid", nullable = false, insertable = false, updatable = false)
    private User user;

}