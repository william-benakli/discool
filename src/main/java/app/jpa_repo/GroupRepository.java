package app.jpa_repo;

import app.model.users.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface GroupRepository extends JpaRepository<Group, Long> {
    ArrayList<Group> findAllByCourseId(long courseId);
}
