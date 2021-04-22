package app.jpa_repo;

import app.model.users.Person;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long> {

    Person findById(long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET email = :emailparam WHERE id = :idparam ")
    void updateEmailById(@Param("idparam") long id, @Param("emailparam") String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE  users SET email = :emailparam , username = :usernameparam , firstName = :firstnameparam , lastName = :lastnameparam , description = :descriptionparam , role = :roleparam , website = :websiteparam  WHERE id = :idparam ")
    void updateUserById(@Param("idparam") long id, @Param("emailparam") String email, @Param("usernameparam") String username , @Param("firstnameparam") String firstname, @Param("lastnameparam") String lastname, @Param("descriptionparam") String description, @Param("roleparam") Person.Role role, @Param("websiteparam") String website);

    Person findByUsername(String username);
    ArrayList<Person> findAll();

}
