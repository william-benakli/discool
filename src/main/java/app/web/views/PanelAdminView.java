package app.web.views;

import app.jpa_repo.CourseRepository;
import app.jpa_repo.PersonRepository;
import app.model.courses.Course;
import app.model.users.Person;
import app.model.users.UserForm;
import app.web.layout.Navbar;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Route(value = "admin", layout = Navbar.class)
@PageTitle("Discool : Admin Panel")
@CssImport("./styles/formStyle.css")

public class PanelAdminView extends VerticalLayout {

    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private UserForm form;
    private final Tab usersTab = new Tab("Users");
    private final Tab coursesTab = new Tab("Courses");
    private final Tabs tabs = new Tabs(usersTab, coursesTab);
    private final Grid<Person> usersGrid = new Grid<>();
    private final Grid<Course> coursesGrid = new Grid<>();

    public PanelAdminView(@Autowired PersonRepository personRepository, @Autowired CourseRepository courseRepository) {
        this.personRepository = personRepository;
        this.courseRepository = courseRepository;
        addClassName("list-view");
        createUserGrid();
        createCoursesGrid();
        createTabs();
        closeEditor();

    }

    private void closeEditor() {
        form.setPerson(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void createUserGrid() {

        usersGrid.setItems(personRepository.findAll());
        usersGrid.addColumn(Person::getUsername).setHeader("Pseudo");
        usersGrid.addColumn(Person::getLastName).setHeader("Last Name");
        usersGrid.addColumn(Person::getFirstName).setHeader("First Name");
        usersGrid.addColumn(Person::getEmail).setHeader("Email");
        usersGrid.addColumn(Person::getDescription).setHeader("Description");
        usersGrid.addColumn(Person::getRole).setHeader("Role");
        usersGrid.addColumn(Person::getWebsite).setHeader("Website");
        usersGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                              GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        usersGrid.getStyle().set("flex","2");
        usersGrid.getColumns().forEach(col ->col.setAutoWidth(true));
        usersGrid.asSingleSelect().addValueChangeListener(event -> editPerson(event.getValue()));
        usersTab.add(usersGrid);

    }

    private void editPerson(Person person) {
        if(person==null){
            closeEditor();
        }
        else{
            form.setPerson(person);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void createCoursesGrid() {
        coursesGrid.setItems(courseRepository.findAll());
        coursesGrid.addColumn(Course::getName).setHeader("Name");
        coursesGrid.addColumn(Course::getTeacherId).setHeader("TeacherID");
        coursesGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                                     GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        coursesTab.add(coursesGrid);
    }

    private void createTabs() {

        form = new UserForm();
        form.getStyle().set("flex","1");
        form.getStyle().set("display","list-item");
        Div content = new Div(usersGrid, form);
        Div content2 = new Div(coursesGrid);
        content.setSizeFull();
        content2.setSizeFull();
        content2.setVisible(true);
        content.addClassName("content");
        usersTab.getStyle().set("flex-direction","column");
        tabs.add(usersTab,coursesTab);
        Map<Tab, Div > tabsToPages = new HashMap<>();
        tabsToPages.put(usersTab, content);
        tabsToPages.put(coursesTab, content2);
        tabsToPages.values().forEach(page -> page.setVisible(false));
        Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
        selectedPage.setVisible(true);
        tabs.addSelectedChangeListener(event -> {
            tabsToPages.values().forEach(page -> page.setVisible(false));
            Component insideSelectedPage = tabsToPages.get(tabs.getSelectedTab());
            insideSelectedPage.setVisible(true);
        });

        add(tabs, content, content2);
    }
}

