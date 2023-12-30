package icekubit.cloudfilestorage.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "Name", unique = true, nullable = false)
    private String name;
    @Column(name = "Email", unique = true, nullable = false)
    private String email;
    @Column(name = "Password", nullable = false)
    private String password;
}
