package app.web.views;

import app.controller.Controller;
import app.jpa_repo.CourseRepository;
import app.jpa_repo.DirectMessageRepository;
import app.jpa_repo.PersonRepository;
import app.jpa_repo.PublicChatMessageRepository;
import app.model.chat.PublicChatMessage;
import app.model.courses.Course;
import app.model.users.Person;
import app.web.components.UserForm;
import app.web.layout.Navbar;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "admin", layout = Navbar.class)
@PageTitle("Discool : Admin Panel")
@CssImport("./styles/formStyle.css")
public class PanelAdminView extends VerticalLayout {
    private final Controller controller;
    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private final PublicChatMessageRepository publicChatMessageRepository;
    private UserForm form;
    private final TextField lastNameFilter = new TextField();
    private final TextField emailFilter = new TextField();
    private final TextField firstNameFilter = new TextField();
    private final Div listUser = new Div();
    private final Tab usersTab = new Tab("Utilisateurs");
    private final Tab coursesTab = new Tab("Cours");
    private final Tabs tabs = new Tabs(usersTab, coursesTab);
    private final Grid<Person> usersGrid = new Grid<>();
    private final Grid<CourseWithName> coursesGrid = new Grid<>(CourseWithName.class);

    public PanelAdminView(@Autowired PersonRepository personRepository,
                          @Autowired CourseRepository courseRepository,
                          @Autowired DirectMessageRepository directMessageRepository,
                          @Autowired PublicChatMessageRepository publicChatMessageRepository) {
        this.personRepository = personRepository;
        this.courseRepository = courseRepository;
        this.publicChatMessageRepository = publicChatMessageRepository;
        this.controller = new Controller(personRepository,
                                         null, publicChatMessageRepository,
                                         courseRepository, null,
                                         null, null, directMessageRepository);

        listUser.add(createButtonsDiv());
        createUserGrid();
        createCourseGrid();
        createTabs();
        updateFilter();
        form.addListener(UserForm.SaveEvent.class, this::savePerson);
        form.addListener(UserForm.DeleteEvent.class, this::deletePerson);
        form.addListener(UserForm.CloseEvent.class, e -> closeEditor());
        updateList();
        closeEditor();
    }

    private void updateFilter() {
        if (lastNameFilter.getValue() == null || lastNameFilter.getValue().isEmpty()) {
            configureEmailFilter();
            configureLastNameFilter();
            configureFirstNameFilter();
        } else if (emailFilter.getValue() == null || emailFilter.getValue().isEmpty()) {
            configureFirstNameFilter();
        } else {
            configureLastNameFilter();
            configureFirstNameFilter();
            configureEmailFilter();
        }
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

    private void createCourseGrid() {
        List<CourseWithName> personList = new ArrayList<>();
        selectTeacher(controller.getAllUsers()).forEach((key, value) -> personList.add(new CourseWithName(key.getName(), value)));
        coursesGrid.setItems(personList);
        coursesGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                                     GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        coursesTab.add(coursesGrid);
    }

    private void savePerson(UserForm.SaveEvent evt) {
        controller.saveUser(evt.getPerson());
        updateList();
        closeEditor();
    }

    private void addPerson() {
        usersGrid.asSingleSelect().clear();
        editPerson(new Person());
    }

    private void updateList() {
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

    private void configureEmailFilter() {
        emailFilter.setPlaceholder("Filtrer par email...");
        emailFilter.setClearButtonVisible(true);
        emailFilter.setValueChangeMode(ValueChangeMode.LAZY);
        emailFilter.addValueChangeListener(e -> updateList());
    }

    private void configureLastNameFilter() {
        lastNameFilter.setPlaceholder("Filtrer par nom...");
        lastNameFilter.setClearButtonVisible(true);
        lastNameFilter.setValueChangeMode(ValueChangeMode.LAZY);
        lastNameFilter.addValueChangeListener(e -> updateList());
    }

    private void configureFirstNameFilter() {
        firstNameFilter.setPlaceholder("Filtrer par Prénom...");
        firstNameFilter.setClearButtonVisible(true);
        firstNameFilter.setValueChangeMode(ValueChangeMode.LAZY);
        firstNameFilter.addValueChangeListener(e -> updateList());
    }

    /**
     * Fuzzy search all users
     *
     * @param stringFilter the string to search for
     * @return a list of all the users that correspond
     */
    private List<Person> findAll(String stringFilter) {
        if ((emailFilter.getValue() == null
                || emailFilter.getValue().isEmpty()) && (lastNameFilter.getValue() == null
                || lastNameFilter.getValue().isEmpty()) && (firstNameFilter.getValue() == null
                || firstNameFilter.getValue().isEmpty())) {
            return controller.findAllUsers();
        } else if ((lastNameFilter.getValue() == null || lastNameFilter.getValue().isEmpty()) && (firstNameFilter.getValue() == null || firstNameFilter.getValue().isEmpty())) {
            return controller.searchByEmail(stringFilter);
        } else if ((emailFilter.getValue() == null || emailFilter.getValue().isEmpty()) && (lastNameFilter.getValue() == null || lastNameFilter.getValue().isEmpty())) {
            return controller.searchByUserName(stringFilter);
        } else {
            return controller.searchUser(stringFilter);
        }
    }

    private void closeEditor() {
        form.setPerson(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void createUserGrid() {
        usersGrid.setItems(controller.findAllUsers());
        usersGrid.addColumn(Person::getUsername).setHeader("Pseudo");
        usersGrid.addColumn(Person::getLastName).setHeader("Nom");
        usersGrid.addColumn(Person::getFirstName).setHeader("Prénom");
        usersGrid.addColumn(Person::getEmail).setHeader("Email");
        usersGrid.addColumn(Person::getDescription).setHeader("Description");
        usersGrid.addColumn(Person::getRole).setHeader("Rôle");
        usersGrid.addColumn(Person::getWebsite).setHeader("Site Web");
        usersGrid.addComponentColumn(event -> createInfoButton(event.getId()))
                .setHeader("Historique");
        usersGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        usersGrid.getStyle().set("flex", "2");
        usersGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        usersGrid.asSingleSelect().addValueChangeListener(event -> editPerson(event.getValue()));
        usersTab.add(usersGrid);
    }

    private void editPerson(Person person) {
        if (person == null) {
            closeEditor();
        } else {
            form.openDialog();
            form.setPerson(person);
            addClassName("editing");
        }
    }

    private Button createInfoButton(long id) {
        Button button = new Button("Info");
        Dialog dialog = new Dialog();
        dialog.add(new Text("l'ensemble des messages de cet utilisateur :"));
        dialog.setWidth("50%");
        dialog.setHeight("65%");

        Grid<PublicChatMessage> messagesGrid = new Grid<>(PublicChatMessage.class);
        messagesGrid.setItems(publicChatMessageRepository.findAllBySenderAndDeletedFalse(id));
        messagesGrid.getColumns().get(1).setVisible(false);
        messagesGrid.getColumns().get(2).setVisible(false);
        messagesGrid.getColumns().get(4).setVisible(false);
        messagesGrid.getColumns().get(5).setVisible(false);
        messagesGrid.getColumns().get(6).setVisible(false); // to hide the date of creation of the message
        messagesGrid.addComponentColumn(item -> createRemoveButton(messagesGrid, item))
                .setHeader("Actions");
        //messagesGrid.addColumn(publicChatMessage -> publicChatMessageRepository.findPublicChatMessageByUserid(id)).setHeader("Id");

        dialog.add(messagesGrid);
        button.addClickListener(event -> dialog.open());
        return button;
    }

    private void deletePerson(UserForm.DeleteEvent evt) {
        controller.deleteUser(evt.getPerson());
        updateList();
        closeEditor();
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

    private Button createRemoveButton(Grid<PublicChatMessage> grid, PublicChatMessage item) {
        Button button = new Button("Supprimer", clickEvent -> {
            ListDataProvider<PublicChatMessage> dataProvider =
                    (ListDataProvider<PublicChatMessage>) grid.getDataProvider();
            controller.deleteMessage(item);
            dataProvider.getItems().remove(item);
            dataProvider.refreshAll();
        });
        button.getStyle().set("color", "red");
        return button;
    }

    private HashMap<Course, String> selectTeacher(ArrayList<Person> allPerson) {
        HashMap<Long, String> selectTeacher = new HashMap<>();
        HashMap<Course, String> teacherCourse = new HashMap<>();
        for (Person person : allPerson) {
            if (!person.getRole().equals(Person.Role.STUDENT)) selectTeacher.put(person.getId(), person.getUsername());
        }
        for (Course course : controller.findAllCourses()) {
            teacherCourse.put(course, selectTeacher.get(course.getTeacherId()));
        }
        return teacherCourse;
    }

    public static class CourseWithName {
        private final String course;
        private final String teacher;

        public CourseWithName(String course, String name) {
            this.course = course;
            this.teacher = name;
        }

        public String getName() {
            return teacher;
        }

        public String getCourse() {
            return course;
        }
    }

}
