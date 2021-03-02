package app.jpa_repo;

import app.model.users.GroupMembers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface GroupMembersRepository extends JpaRepository<GroupMembers, Long> {

    ArrayList<GroupMembers> findByUserId(long id);
}
