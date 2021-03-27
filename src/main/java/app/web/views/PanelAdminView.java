package app.web.views;

import app.jpa_repo.PersonRepository;
import app.model.users.Person;
import app.web.layout.Navbar;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
@Route(value = "admin", layout = Navbar.class)
@PageTitle("adminView")

public class PanelAdminView extends VerticalLayout {

    private Grid<Person> grid = new Grid<>();
    private PersonRepository personRepository;

    public PanelAdminView(@Autowired PersonRepository personRepository) {

            this.personRepository = personRepository;
            grid.setItems(personRepository.findAll());
            grid.addColumn(Person::getUsername).setHeader("Nom");
            grid.addColumn(Person::getLastName).setHeader("LastName");
            grid.addColumn(Person::getEmail).setHeader("Email");
            grid.addColumn(Person::getDescription).setHeader("description");
            grid.addColumn(Person::getRole).setHeader("Role");
            grid.addColumn(Person::getWebsite).setHeader("website");
            grid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                    GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
            add(grid);

    }
}

