package app.jpa_repo;

import app.model.users.GroupMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface GroupMembersRepository extends JpaRepository<GroupMembers, Long> {

    ArrayList<GroupMembers> findByUserId(long id);
    ArrayList<GroupMembers> findByGroupId(long id);

    @Modifying
    @Transactional
    @Query(value =" DELETE FROM group_members WHERE groupid = :idparam ",nativeQuery = true)
    void deleteAllByGroupId(@Param("idparam") long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM group_members WHERE userid = :idparam", nativeQuery = true)
    void deleteByUserId(@Param("idparam") long id);


    GroupMembers findByUserIdAndGroupId(long userId, long groupId);

    ArrayList<GroupMembers> findAllByGroupId(long groupId);

}
