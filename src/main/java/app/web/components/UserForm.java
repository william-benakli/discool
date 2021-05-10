package app.web.components;

import app.controller.Controller;
import app.jpa_repo.PersonRepository;
import app.model.users.Person;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class UserForm extends FormLayout {

    private final Controller controller;
    private final Dialog dialog  = new Dialog();
    private final PersonRepository personRepository;
    private final Binder<Person> binder = new BeanValidationBinder<>(Person.class);
    private final TextField username = new TextField("Pseudo");
    private final TextField firstName = new TextField("Nom");
    private final TextField lastName = new TextField("Prenom");
    private final EmailField email = new EmailField("Email");
    private final PasswordField password = new PasswordField("Mot-de-passe");
    private final TextField description = new TextField("Description");
    private final ComboBox<Person.Role> role = new ComboBox<>("Role");
    private final TextField website = new TextField("Website");
    private final Button save = new Button("Enregistrer");
    private final Button delete = new Button("Supprimer");
    private final Button close = new Button("Retour");
    private final Button infoBtn = new Button("Aide");
    private Person person;
    private final String path = "./uploads/UsersCSV/" ;
    private final UploadComponent upload = new UploadComponent("50px", "96%", 1, 30000000,
            path);

    public UserForm(PersonRepository personRepository) {
        upload.setAcceptedFileTypes(".csv");
        upload.addFinishedListener(finishedEvent -> {
            try {
                uploadCSVFile(upload.getFileName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        upload.addFailedListener(failedEvent -> {
            Notification notification;
            notification = new Notification("Error with the File", 3000, Notification.Position.MIDDLE);
            notification.open();
        });
        this.controller = new Controller(personRepository,
                                         null, null,
                                         null, null,
                                         null, null, null);
        description.setClearButtonVisible(true);
        email.setClearButtonVisible(true);
        this.personRepository = personRepository;
        binder.bindInstanceFields(this);
        Person.Role[] departmentList = Person.Role.getRole();
        role.setItems(departmentList);
        addClassName("user-form");
        dialog.setWidth("50%");
        dialog.setHeight("65%");
        setCss();
        dialog.add(username,
            firstName,
            lastName,
            email,
            password,
            description,
            role,
            website,
            createButtonsLayout(),
            upload
                );
    }

    public void openDialog(){
        dialog.open();
    }
    public void closeDialog(){
        dialog.close();
    }

    private void setCss(){
        username.getStyle()
                .set("display","block")
                .set("height","50px")
                .set("margin-top","-20px");
        password.getStyle()
                .set("display","block")
                .set("height","50px");
        email.getStyle()
                .set("display","block")
                .set("height","50px");
        firstName.getStyle()
                .set("display","block")
                .set("height","50px");
        lastName.getStyle()
                .set("display","block")
                .set("height","50px");
        description.getStyle()
                .set("display","block")
                .set("height","50px");
        role.getStyle()
                .set("display","block")
                .set("margin-top","60px");
        website.getStyle()
                .set("display","block")
                .set("margin-top","-50px");
    }

    /**
     * Return a layout where the buttons are
     */

    private HorizontalLayout createButtonsLayout() {
        final HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.getStyle().set("display","block");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);
        save.addClickListener(click -> {

            if (checkIfFieldNotEmpty()) {
                if (!controller.userExist(this.username.getValue())) {
                    controller.addUser(this.username.getValue(), this.password.getValue(), this.role.getValue(), this.firstName.getValue(), this.lastName.getValue(), this.email.getValue(), this.description.getValue(), this.website.getValue(), 0, 0, 0);
                } else {
                    controller.updateUserById(person.getId(), email.getValue(), username.getValue(), firstName.getValue(), lastName.getValue(), description.getValue(), role.getValue(), website.getValue());
                }
                controller.deleteNullUsers();
                validateAndSave();
            }
        });
        delete.addClickListener(click -> {
            controller.deleteUser(person);
            fireEvent(new DeleteEvent(this, person));
        });
        close.addClickListener(click -> {fireEvent(new CloseEvent(this));
            closeDialog();
        });
        infoBtn.addClickListener(click -> infoCsv());
        binder.addStatusChangeListener(evt -> save.setEnabled(binder.isValid()));
        horizontalLayout.add(save, delete, close, infoBtn);
        return horizontalLayout;
    }

    /**
     * create a dialog in which we indicate to the user the form of how the fields in the CSV file should be
     */

    private void infoCsv(){
        final VerticalLayout masterLayout = new VerticalLayout();
        final Dialog info = new Dialog();
        final H1 p = new H1("Format du fichier .csv");
        final Paragraph paragraph = new Paragraph("La première ligne n'est pas necessaire.Voici deux examples ci-dessous.");
        final Image image = new Image("img/exampleCSV.png","");
        image.setWidth("60%");
        image.setHeight("auto");
        info.setWidth("40%");
        info.setHeight("auto");
        p.getStyle().set("margin-top","50px");
        masterLayout.add(p,paragraph,image);
        masterLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        info.add(masterLayout);
        info.open();
    }

    /**
     * @return false if one of the required fields is empty, true if everything is ok
     */
    private boolean checkIfFieldNotEmpty() {
        boolean ok = true;
        Notification notification;
        if (this.username.isEmpty()) {
            notification = new Notification("Pseudo field is empty !", 3000, Notification.Position.MIDDLE);
            notification.open();
            ok = false;
        } else if (this.password.isEmpty()) {
            notification = new Notification("Password field is empty !", 3000, Notification.Position.MIDDLE);
            notification.open();
            ok = false;
        } else if (this.email.isEmpty()) {
            notification = new Notification("email field is empty !", 3000, Notification.Position.MIDDLE);
            notification.open();
            ok = false;
        } else if (this.role.isEmpty()) {
            notification = new Notification("You need to select a role !", 3000, Notification.Position.MIDDLE);
            notification.open();
            ok = false;
        }
        return ok;
    }
    private void uploadCSVFile(String fileName) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(fileName)) ;
        String line = "";
        try {
            while((line = br.readLine())!=null){
                String[] values = line.split(",");
                Person p = new Person();
                p.setUsername(values[0]);
                p.setFirstName(values[3]);
                p.setLastName(values[4]);
                p.setEmail(values[5]);
                p.setPassword(values[1]);
                p.setDescription(values[6]);
                p.setRoleAsString(values[2]);//can't use the builder that's why i used sets
                p.setWebsite(values[7]);

                updateForm(values[0],values[3],values[4], values[5], values[1], values[6], p.getRole(),values[7]);
                controller.addUser(this.username.getValue(), this.password.getValue(), this.role.getValue(), this.firstName.getValue(), this.lastName.getValue(), this.email.getValue(), this.description.getValue(), this.website.getValue(), 0, 0, 0);
                controller.deleteNullUsers();
                try {
                    binder.writeBean(p);
                    fireEvent(new SaveEvent(this, p));
                } catch (ValidationException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        br.close();
        Files.delete(Paths.get(fileName));
    }

    private void updateForm(String username , String firstName, String lastName, String email, String password, String description, Person.Role role , String website){
        this.username.setValue(username);
        this.firstName.setValue(firstName);
        this.lastName.setValue(lastName);
        this.email.setValue(email);
        this.password.setValue(password);
        this.description.setValue(description);
        this.role.setValue(role);
        this.website.setValue(website);
    }


    private void validateAndSave() {
        try {
            binder.writeBean(person);
            fireEvent(new SaveEvent(this, person));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    public void setPerson(Person person) {
        this.person = person;
        binder.readBean(person);
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
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
}

