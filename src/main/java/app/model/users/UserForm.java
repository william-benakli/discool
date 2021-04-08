
    package app.model.users;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.logging.log4j.util.Strings;

import java.util.List;


    public class UserForm extends FormLayout {
         TextField firstName = new TextField("First name");
        TextField lastName = new TextField("Last name");
        EmailField email = new EmailField("Email");
        ComboBox<Person.Role> role = new ComboBox<>("Role");
        Person.Role[] departmentList = Person.Role.getRole();

        Button save = new Button("Save");
        Button delete = new Button("Delete");
        Button close = new Button("Cancel");

        public UserForm() {

            role.setItems(departmentList);
            addClassName("user-form");
            getStyle().set("display","block");
            add(firstName,
                    lastName,
                    email,
                    role,
                    createButtonsLayout());
        }

        private HorizontalLayout createButtonsLayout() {
            save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
            close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            save.addClickShortcut(Key.ENTER);
            close.addClickShortcut(Key.ESCAPE);
            return new HorizontalLayout(save, delete, close);
        }
    }


