package app.model.users;


import org.apache.commons.lang3.StringUtils;

public class PersonFilter {
    String userName = "";
    String lastName = "";
    String firstName = "";
    String email = "";

    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) {
        this.userName = username;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean test(Person person) {
        if (userName.length() > 0 && !StringUtils
                .containsIgnoreCase(String.valueOf(person.getUsername()),
                        userName)) {
            return false;
        }
        if (lastName.length() > 0 && !StringUtils.containsIgnoreCase(
                String.valueOf(person.getLastName()), lastName)) {
            return false;
        }
        if (firstName.length() > 0 && !StringUtils
                .containsIgnoreCase(String.valueOf(person.getFirstName()), firstName)) {
            return false;
        }

        if (email.length() > 0 && !StringUtils.containsIgnoreCase(
                String.valueOf(person.getEmail()),
                email)) {
            return false;
        }
        return true;
    }
}