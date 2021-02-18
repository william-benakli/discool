package app.jpa_repo;

import app.model.users.GroupMembers;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMembersRepository extends JpaRepository<GroupMembers, Long> {
}
