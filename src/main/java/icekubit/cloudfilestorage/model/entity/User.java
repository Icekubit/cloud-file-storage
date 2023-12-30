package icekubit.cloudfilestorage.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
