package sit.int221.nw1.models.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Getter
@Setter
@Table(name = "status")
public class Statuses {

    @Id
    @Column(name = "status_id")
    private String id ;

    @Size(min=1, max=50)
    @Column(name = "status_name")
    private String name;
    @Size(min=1, max=200)
    @Column(name = "status_description")
    private String description;

//    @ManyToOne
//    @JoinColumn(name = "board_id", nullable = false)
//    private Boards boards;
//
    @OneToMany(mappedBy = "status", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Tasks> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "status" ,fetch = FetchType.EAGER)
    @JsonIgnore
    private List<BoardStatus> boardStatuses;

//    public int getNoOfTasks() {
//        return tasks.size();
//    }
}
