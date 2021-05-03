package app.web.views;

import app.jpa_repo.CourseRepository;
import app.jpa_repo.PersonRepository;
import app.model.courses.Course;
import app.model.users.Person;
import app.web.components.UserForm;
import app.web.layout.Navbar;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "admin", layout = Navbar.class)
@PageTitle("Discool : Admin Panel")
@CssImport("./styles/formStyle.css")

public class PanelAdminView extends VerticalLayout {

    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private UserForm form;
    private final TextField lastNameFilter = new TextField();
    private final TextField emailFilter = new TextField();
    private final TextField firstNameFilter = new TextField();
    private final Div listUser = new Div();
    private final Tab usersTab = new Tab("Utilisateurs");
    private final Tab coursesTab = new Tab("Cours");
    private final Tabs tabs = new Tabs(usersTab, coursesTab);
    private final Grid<Person> usersGrid = new Grid<>();
    private final Grid<Course> coursesGrid = new Grid<>();

    public PanelAdminView(@Autowired PersonRepository personRepository, @Autowired CourseRepository courseRepository) {
        this.personRepository = personRepository;
        this.courseRepository = courseRepository;
        listUser.add(createButtonsDiv());
        createUserGrid();
        createCoursesGrid();
        createTabs();
        if (lastNameFilter.getValue() == null || lastNameFilter.getValue().isEmpty()) {
            configureFilter2();
            configureFilter();
            configureFilter3();
        } else if (emailFilter.getValue() == null || emailFilter.getValue().isEmpty()) {
            configureFilter3();
        } else {
            configureFilter();
            configureFilter3();
            configureFilter2();
        }
        form.addListener(UserForm.SaveEvent.class, this::savePerson);
        form.addListener(UserForm.DeleteEvent.class, this::deletePerson);
        form.addListener(UserForm.CloseEvent.class, e -> closeEditor());
        updateList();
        closeEditor();

    }

    private Div createButtonsDiv() {
        Div div = new Div();
        Button addUser = new Button("Ajouter des utilisateurs");
        div.add(lastNameFilter, emailFilter, firstNameFilter, addUser);
        div.getStyle().set("display", "inline-block");
        lastNameFilter.getStyle().set("padding", "5px");
        emailFilter.getStyle().set("padding", "5px");
        firstNameFilter.getStyle().set("padding", "5px");
        addClassName("list-view");
        addUser.addClickListener(buttonClickEvent -> addPerson());
        return div;
    }

    public void deletePerson(UserForm.DeleteEvent evt) {
        personRepository.delete(evt.getPerson());
        updateList();
        closeEditor();
    }

    public void savePerson(UserForm.SaveEvent evt) {
        personRepository.save(evt.getPerson());
        updateList();
        closeEditor();
    }

    public void addPerson(){
        usersGrid.asSingleSelect().clear();
        editPerson(new Person());
    }

    public void updateList() {

        if ((emailFilter.getValue() == null
                || emailFilter.getValue().isEmpty()) && (lastNameFilter.getValue() == null
                || lastNameFilter.getValue().isEmpty())) {
            usersGrid.setItems(findAll(firstNameFilter.getValue()));
        } else if ((emailFilter.getValue() == null
                || emailFilter.getValue().isEmpty()) && (firstNameFilter.getValue() == null
                || firstNameFilter.getValue().isEmpty())) {
            usersGrid.setItems(findAll(lastNameFilter.getValue()));
        } else {
            usersGrid.setItems(findAll(emailFilter.getValue()));
        }
    }

    public List<Person>findAll(String stringFilter) {
        if ((emailFilter.getValue() == null
                || emailFilter.getValue().isEmpty()) && (lastNameFilter.getValue() == null
                || lastNameFilter.getValue().isEmpty()) && (firstNameFilter.getValue() == null
                || firstNameFilter.getValue().isEmpty())) {
            return personRepository.findAll();
        } else if ((lastNameFilter.getValue() == null || lastNameFilter.getValue().isEmpty()) && (firstNameFilter.getValue() == null || firstNameFilter.getValue().isEmpty())) {
            return personRepository.searchByEmail(stringFilter);
        } else if ((emailFilter.getValue() == null || emailFilter.getValue().isEmpty()) && (lastNameFilter.getValue() == null || lastNameFilter.getValue().isEmpty())) {
            return personRepository.searchByUserName(stringFilter);
        } else {
            return personRepository.search(stringFilter);
        }

    }

    public void configureFilter() {
        lastNameFilter.setPlaceholder("Filtrer par nom...");
        lastNameFilter.setClearButtonVisible(true);
        lastNameFilter.setValueChangeMode(ValueChangeMode.LAZY);
        lastNameFilter.addValueChangeListener(e -> updateList());
    }
    public void configureFilter2() {
        emailFilter.setPlaceholder("Filtrer par email...");
        emailFilter.setClearButtonVisible(true);
        emailFilter.setValueChangeMode(ValueChangeMode.LAZY);
        emailFilter.addValueChangeListener(e -> updateList());
    }

    public void configureFilter3() {
        firstNameFilter.setPlaceholder("Filtrer par Prénom...");
        firstNameFilter.setClearButtonVisible(true);
        firstNameFilter.setValueChangeMode(ValueChangeMode.LAZY);
        firstNameFilter.addValueChangeListener(e -> updateList());
    }
    private void closeEditor() {
        form.setPerson(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void createUserGrid() {
        usersGrid.setItems(personRepository.findAll());
        usersGrid.addColumn(Person::getUsername).setHeader("Pseudo");
        usersGrid.addColumn(Person::getLastName).setHeader("Nom");
        usersGrid.addColumn(Person::getFirstName).setHeader("Prénom");
        usersGrid.addColumn(Person::getEmail).setHeader("Email");
        usersGrid.addColumn(Person::getDescription).setHeader("Description");
        usersGrid.addColumn(Person::getRole).setHeader("Rôle");
        usersGrid.addColumn(Person::getWebsite).setHeader("Site Web");
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
        coursesGrid.addColumn(Course::getName).setHeader("Nom");
        coursesGrid.addColumn(Course::getTeacherId).setHeader("Nom de l'enseignant.e");
        coursesGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                                     GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        coursesTab.add(coursesGrid);
    }

    private void createTabs() {
        form = new UserForm(personRepository);
        form.getStyle().set("flex", "1");
        form.getStyle().set("display", "list-item");
        Div content = new Div(usersGrid, form);
        VerticalLayout content_layout = new VerticalLayout(listUser, content);
        Div content2 = new Div(coursesGrid);
        VerticalLayout content2_layout = new VerticalLayout(content2);
        content.setSizeFull();
        content2.setSizeFull();
        content2.setVisible(true);
        content.addClassName("content");
        usersTab.getStyle().set("flex-direction", "column");
        tabs.add(usersTab, coursesTab);
        Map<Tab, VerticalLayout> tabsToPages = new HashMap<>();
        tabsToPages.put(usersTab, content_layout);
        tabsToPages.put(coursesTab, content2_layout);
        tabsToPages.values().forEach(page -> page.setVisible(false));
        Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
        selectedPage.setVisible(true);
        tabs.addSelectedChangeListener(event -> {
            tabsToPages.values().forEach(page -> page.setVisible(false));
            Component insideSelectedPage = tabsToPages.get(tabs.getSelectedTab());
            insideSelectedPage.setVisible(true);
        });
        add(tabs, content_layout, content2_layout);
    }
}
