package app.jpa_repo;

import app.model.users.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {

    Person findById(long id);

    Person findByUsername(String username);
}
