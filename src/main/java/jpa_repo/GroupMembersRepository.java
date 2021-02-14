package jpa_repo;

import model.users.GroupMembers;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMembersRepository extends JpaRepository<GroupMembers, Long> {
}
