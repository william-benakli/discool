package app.jpa_repo;

import app.model.users.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface PersonRepository extends JpaRepository<Person, Long> {

    Person findById(long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET email = :emailparam WHERE id = :idparam ")
    void updateEmailById(@Param("idparam") long id, @Param("emailparam") String email);

    Person findByUsername(String username);
    ArrayList<Person> findAll();
}
