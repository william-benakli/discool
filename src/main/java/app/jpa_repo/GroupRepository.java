package app.jpa_repo;

import app.model.users.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface GroupRepository extends JpaRepository<Group, Long> {
    ArrayList<Group> findAllByCourseId(long courseId);
    ArrayList<Group> findGroupsByCourseId(long courseId);

    @Modifying
    @Transactional
    @Query(value =" DELETE FROM groups WHERE courseid = :idparam ",nativeQuery = true)
    void deleteAllByCourseId(@Param("idparam") long id);


    ArrayList<Group> findAll();

    Group findGroupByCourseId(long courseId);

    Group findTopByOrderByIdDesc();
}
