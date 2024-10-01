package sit.int221.nw1.models.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

// User.java
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Getter
@Setter
@Table(name = "Users")
public class User {

    @Id
    @Column(name = "oid", nullable = false)
    private String oid;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Boards> boards = new ArrayList<>();

    public User(String oid) {
        this.oid = oid;
    }

    // Method to check if the user is the owner of a board
    public boolean isOwner(Boards board) {
        return this.oid.equals(board.getUser().getOid());
    }
}


