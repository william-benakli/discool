package app.web.views;

import app.jpa_repo.CourseRepository;
import app.jpa_repo.PersonRepository;
import app.model.courses.Course;
import app.model.users.Person;
import app.web.layout.Navbar;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
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
    private Grid<Course> gridCourse = new Grid<>();
    private PersonRepository personRepository;
    private CourseRepository courseRepository;
    public PanelAdminView(@Autowired PersonRepository personRepository, CourseRepository courseRepository) {
        this.personRepository = personRepository;
        this.courseRepository = courseRepository;
        grid.setItems(personRepository.findAll());
        grid.addColumn(Person::getUsername).setHeader("Nom");
        grid.addColumn(Person::getLastName).setHeader("LastName");
        grid.addColumn(Person::getEmail).setHeader("Email");
        grid.addColumn(Person::getDescription).setHeader("description");
        grid.addColumn(Person::getRole).setHeader("Role");
        grid.addColumn(Person::getWebsite).setHeader("website");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        add(new H2("Les Users"));
        add(grid);
        add(new H2("Les Cours"));
        gridCourse.setItems(courseRepository.findAll());
        gridCourse.addColumn(Course::getName).setHeader("Cours");
        gridCourse.addColumn(Course::getTeacherId).setHeader("TeacherID");
        gridCourse.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        add(gridCourse);
    }
}

