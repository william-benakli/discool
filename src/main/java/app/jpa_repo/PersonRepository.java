package app.jpa_repo;

import app.model.users.Person;
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
    @Query(value = "UPDATE users SET email = :emailparam , username = :usernameparam , firstName = :firstnameparam , lastName = :lastnameparam , description = :descriptionparam , role = :roleparam , website = :websiteparam  WHERE id = :idparam")
    void updateUserById(@Param("idparam") long id, @Param("emailparam") String email, @Param("usernameparam") String username , @Param("firstnameparam") String firstname, @Param("lastnameparam") String lastname, @Param("descriptionparam") String description, @Param("roleparam") Person.Role role, @Param("websiteparam") String website);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users WHERE id = :idparam ", nativeQuery = true)
    void deleteUserById(@Param("idparam") long id);

    @Modifying
    @Transactional
    @Query(value =" DELETE FROM users WHERE username =''",nativeQuery = true)
    void deleteNullUsers();

    @Modifying
    @Transactional
    @Query(value = " INSERT INTO users (username ,password ,role ,firstName ,lastName ,email ,description ,website ,firstlogin ,lastlogin ,timecreated ) VALUES ( username ,password ,role ,firstname ,lastname ,email ,description ,website ,firstlogin ,lastlogin ,timecreated )",nativeQuery = true)
    void addUser(@Param("usernameparam") String username , @Param("passwordParam") String password, @Param("roleparam") Person.Role role,  @Param("firstnameparam") String firstname, @Param("lastnameparam") String lastname, @Param("emailparam") String email,@Param("descriptionparam") String description, @Param("websiteparam") String website,@Param("firstloginparam") long firstlogin,@Param("lastloginparam") long lastlogin,@Param("timecreatedparam") long timecreated);

    @Query("select c from users c " +
            "where lower(c.firstName) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(c.lastName) like lower(concat('%', :searchTerm, '%'))")
            List<Person> search(@Param("searchTerm") String searchTerm);

    @Query("select c from users c " +
            "where lower(c.username) like lower(concat('%', :searchTerm, '%')) ")
    List<Person> searchByUserName(@Param("searchTerm") String searchTerm);

    @Query("select c from users c " +
            "where lower(c.email) like lower(concat('%', :searchTerm, '%')) ")
    List<Person> searchByEmail(@Param("searchTerm") String searchTerm);

    Person findByUsername(String username);

    ArrayList<Person> findAll();


}
