package app.jpa_repo;

import app.model.chat.PrivateTextChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.Optional;

public interface PrivateTextChannelRepository extends JpaRepository<PrivateTextChannel, Long> {

    ArrayList<PrivateTextChannel> findAllByUserA(long userA);

    ArrayList<PrivateTextChannel> findAllByUserB(long userB);

    Optional<PrivateTextChannel> findByUserAAndUserB(long userA, long userB);
}
