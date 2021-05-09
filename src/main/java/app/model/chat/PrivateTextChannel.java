package app.model.chat;

import lombok.*;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "private_channels")
@Table(name = "private_channels")
public class PrivateTextChannel implements TextChannel {
    @Id // to say this is the primary key in the database
    @GeneratedValue(strategy = GenerationType.IDENTITY) // to generate the id
    @Column(
            name = "id", // the name of the column in the database
            updatable = false // so that the value can't be updated
    )
    private long id;

    @Column(
            name = "userA",
            updatable = false
    )
    private long userA;

    @Column(
            name = "userB",
            updatable = false
    )
    private long userB;

    @Column(
            name = "user1_read"
    )
    private boolean user1Read;

    @Column(
            name = "user2_read"
    )
    private boolean user2Read;

    public String getName() {
        return ""; // no title for private text channels
    }
}
