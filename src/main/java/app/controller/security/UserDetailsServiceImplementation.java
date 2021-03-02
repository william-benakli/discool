package app.controller.security;

import app.jpa_repo.PersonRepository;
import app.model.users.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

public class UserDetailsServiceImplementation implements UserDetailsService {
    private PersonRepository personRepository;

    @Autowired
    public void setPersonRepository(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Person person = personRepository.findByUsername(username);

        // get the user's role
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(person.getRoleAsString()));

        // TODO : implement passwords
        // the weird string is the encoded version of "password"
        // (aka to log in, put a valid username and "password" as the password)
        UserDetails user = User.withUsername(person.getUsername()).password("$2a$10$lSTd/IJRB7IU9a43gXKpUeQT2oiXH9H8PUWf0786VoVWv4KzJZh0m")
                .authorities(grantedAuthorities).build();
        return user;
    }
}
