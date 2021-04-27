
    package app.web.components;

import app.jpa_repo.PersonRepository;
import app.model.users.Person;
import app.web.views.PanelAdminView;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import org.hibernate.event.spi.DeleteEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

    public class UserForm extends FormLayout {

        private final PersonRepository personRepository;
        private Notification notification ;
        private Person person ;
        Binder<Person> binder = new BeanValidationBinder<>(Person.class) ;
        TextField username = new TextField("Pseudo");
        TextField firstName = new TextField("First name");
        TextField lastName = new TextField("Last name");
        EmailField email = new EmailField("Email");
        PasswordField password = new PasswordField("Password");
        TextField description = new TextField("Description");
        ComboBox<Person.Role> role = new ComboBox<>("Role");
        TextField website = new TextField("Website");
        Person.Role[] departmentList = Person.Role.getRole();

        Button save = new Button("Save");
        Button delete = new Button("Delete");
        Button close = new Button("Cancel");


        public UserForm(@Autowired PersonRepository personRepository) {

            description.setClearButtonVisible(true);//pour avoir le boutton croix qui permet de supprimer toute la saisie d'un coup
            email.setClearButtonVisible(true);
            this.personRepository = personRepository ;
            binder.bindInstanceFields(this);
            role.setItems(departmentList);
            addClassName("user-form");
            getStyle().set("display","block");
            add(username,
                    firstName,
                    lastName,
                    email,
                    password,
                    description,
                    role,
                    website,
                    createButtonsLayout());
        }

        public void setPerson(Person person){
            this.person = person;
            binder.readBean(person);
        }

        public boolean userExist(String username){
            if(personRepository.findByUsername(username)!=null){
                return true ;
            }
            return false;
        }

        private HorizontalLayout createButtonsLayout() {
            save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
            close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            save.addClickShortcut(Key.ENTER);
            close.addClickShortcut(Key.ESCAPE);
            save.addClickListener(click ->{

                if(this.username.isEmpty()) {
                    notification = new Notification("pseudo is empty !",3000,Notification.Position.MIDDLE);
                    notification.open();
                }
                if(this.password.isEmpty()) {
                    notification = new Notification("password is empty !",3000,Notification.Position.MIDDLE);
                    notification.open();
                }
                if(this.email.isEmpty()) {
                    notification = new Notification("email is empty !",3000,Notification.Position.MIDDLE);
                    notification.open();
                }
                if(this.role.isEmpty()){
                    notification = new Notification("you have to select a role !",3000,Notification.Position.MIDDLE);
                    notification.open();
                }
                else {
                    if (!userExist(this.username.getValue())) {
                        personRepository.addUser(this.username.getValue(), this.password.getValue(), this.role.getValue(), this.firstName.getValue(), this.lastName.getValue(), this.email.getValue(), this.description.getValue(), this.website.getValue(), 0, 0, 0);
                    } else {
                        personRepository.updateUserById(person.getId(), email.getValue(), username.getValue(), firstName.getValue(), lastName.getValue(), description.getValue(), role.getValue(), website.getValue());
                    }
                    personRepository.deleteNullUsers();
                    validateAndSave();
                }
                });
            delete.addClickListener(click ->{
                personRepository.deleteUserByIdGroup_members(person.getId());
                personRepository.deleteUserByIdIndirect_messages(person.getId());
                personRepository.updateUserByIdPosts(person.getId());
                personRepository.deleteUserById(person.getId());
                fireEvent(new DeleteEvent(this, person));
            });
            close.addClickListener(click ->fireEvent(new CloseEvent(this)));
            binder.addStatusChangeListener(evt -> save.setEnabled(binder.isValid()));
            return new HorizontalLayout(save, delete, close);
        }

        private void validateAndSave() {
            try {
                binder.writeBean(person);
                fireEvent(new SaveEvent(this, person));
            } catch (ValidationException e) {
                e.printStackTrace();
            }
        }

        public static abstract class UserFormEvent extends ComponentEvent<UserForm> {
            private Person person;

            protected UserFormEvent(UserForm source, Person person) {
                super(source, false);
                this.person = person;
            }

            public Person getPerson() {
                return person;
            }
        }

        public static class SaveEvent extends UserFormEvent {
                SaveEvent(UserForm source, Person person) {
                super(source, person);
            }
        }

        public static class DeleteEvent extends UserFormEvent {
            DeleteEvent(UserForm source, Person person) {
                super(source, person);
            }
        }

        public static class CloseEvent extends UserFormEvent {
            CloseEvent(UserForm source) {
                super(source, null);
            }
        }

        public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
            return getEventBus().addListener(eventType, listener);
        }
    }


