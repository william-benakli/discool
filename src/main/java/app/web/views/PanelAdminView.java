package app.web.views;

import app.controller.AssignmentController;
import app.controller.ChatController;
import app.controller.Controller;
import app.jpa_repo.*;
import app.model.chat.PublicChatMessage;
import app.model.chat.PublicTextChannel;
import app.model.courses.Course;
import app.model.courses.MoodlePage;
import app.model.users.Group;
import app.model.users.Person;
import app.web.components.UploadComponent;
import app.web.components.UserForm;
import app.web.layout.Navbar;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "admin", layout = Navbar.class)
@PageTitle("Discool : Admin Panel")
@CssImport("./styles/formStyle.css")
public class PanelAdminView extends VerticalLayout {
    private final Controller controller;
    private final ChatController chatController;
    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private final PublicChatMessageRepository publicChatMessageRepository;
    private final GroupMembersRepository groupMembersRepository;
    private final PrivateChatMessageRepository privateChatMessageRepository;
    private UserForm form;
    private final TextField lastNameFilter = new TextField();
    private final TextField emailFilter = new TextField();
    private final TextField firstNameFilter = new TextField();
    private final String path = "./uploads/UsersCSV/" ;
    private final UploadComponent upload = new UploadComponent("50px", "96%", 1, 30000000,
            path);
    private final Div listUser = new Div();
    private final Tab usersTab = new Tab("Utilisateurs");
    private final Tab coursesTab = new Tab("Cours");
    private final Tabs tabs = new Tabs(usersTab, coursesTab);
    private final Grid<Person> usersGrid = new Grid<>();
    private final Div coursesGrid = new Div();
    private final AssignmentController assignmentController;

    public PanelAdminView(@Autowired PersonRepository personRepository,
                          @Autowired CourseRepository courseRepository,
                          @Autowired PublicChatMessageRepository publicChatMessageRepository,
                          @Autowired GroupMembersRepository groupMembersRepository,
                          @Autowired PrivateTextChannelRepository privateTextChannelRepository,
                          @Autowired PrivateChatMessageRepository privateChatMessageRepository,
                          @Autowired GroupRepository groupRepository,
                          @Autowired AssignmentRepository assignmentRepository,
                          @Autowired StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository,
                          @Autowired PublicTextChannelRepository publicTextChannelRepository,
                          @Autowired MoodlePageRepository moodlePageRepository) {
        this.assignmentController = new AssignmentController(personRepository, assignmentRepository, studentAssignmentsUploadsRepository, courseRepository);
        upload.setAcceptedFileTypes(".csv");
        upload.addFinishedListener(finishedEvent -> {
            try {
                form.uploadCSVFile(upload.getFileName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        upload.addFailedListener(failedEvent -> {
            Notification notification;
            notification = new Notification("Erreur avec le fichier.", 3000, Notification.Position.MIDDLE);
            notification.open();
        });
        this.groupMembersRepository = groupMembersRepository;
        this.personRepository = personRepository;
        this.courseRepository = courseRepository;
        this.publicChatMessageRepository = publicChatMessageRepository;
        this.privateChatMessageRepository = privateChatMessageRepository;
        this.controller = new Controller(personRepository,
                publicTextChannelRepository, publicChatMessageRepository,
                                         courseRepository, moodlePageRepository,
                groupRepository, groupMembersRepository, privateChatMessageRepository);
        this.chatController = new ChatController(personRepository, null,
                                                 publicChatMessageRepository, null,
                                                 privateChatMessageRepository);
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
                .setHeader("Historique des messages");
        usersGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                                   GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        usersGrid.getStyle().set("flex", "2");
        usersGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        usersGrid.asSingleSelect().addValueChangeListener(event -> editPerson(event.getValue()));
        usersTab.add(usersGrid);
    }

    void styleButtonok(Button button){
        button.getStyle()
                .set("background-color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml())
                .set("color", ViewWithSidebars.ColorHTML.WHITE.getColorHtml());
    }

    private Div createButtonsDiv() {
        Div div = new Div();
        Button addUser = new Button("Ajouter des utilisateurs");
        styleButtonok(addUser);
        Button addCsvFile = new Button("Importer un fichier .csv");
        addCsvFile.getStyle().set("margin-left","4px");
        styleButtonok(addCsvFile);
        div.add(lastNameFilter, emailFilter, firstNameFilter, addUser, addCsvFile);
        div.getStyle().set("display", "inline-block");
        lastNameFilter.getStyle().set("padding", "5px");
        emailFilter.getStyle().set("padding", "5px");
        firstNameFilter.getStyle().set("padding", "5px");
        addClassName("list-view");
        addUser.addClickListener(buttonClickEvent -> addPerson());
        addCsvFile.addClickListener(buttonClickEvent -> infoCsv());
        return div;
    }

    private void createCourseGrid() {
        boolean color=false;
        List<CourseWithName> personList = new ArrayList<>();
        selectTeacher(controller.getAllUsers()).forEach((key, value) -> personList.add(new CourseWithName(key, value, controller, assignmentController)));
        coursesGrid.add(createDivCourseHeader());
        for (PanelAdminView.CourseWithName courseWithName :personList) {
            coursesGrid.add(createDivCourse(courseWithName, color));
            color=!color;
        }
        coursesGrid.getStyle()
                .set("max-width","1000px")
                .set("margin","auto")
                .set("max-height","500px")
                .set("overflow","auto");
        coursesTab.add(coursesGrid);
    }

    Div createDivCourseHeader(){
        Div div = new Div();
        Paragraph creator = new Paragraph("Créateur");
        Paragraph course = new Paragraph("Cours");
        Paragraph nada = new Paragraph("");
        div.getStyle().set("background-color", ViewWithSidebars.ColorHTML.GREYTAB.getColorHtml());
        styleDiv(div);
        div.add( styleDivParagraph(creator, null), styleDivParagraph(course, null), styleDivParagraph(nada, null));
        return div;
    }

    Div createDivCourse(CourseWithName courseWithName, Boolean color){
        Div div = new Div();
        Paragraph paragraph = new Paragraph(courseWithName.getCourse());
        Paragraph paragraphName = new Paragraph(courseWithName.getName());
        div.getStyle().set("background-color", (!color) ? ViewWithSidebars.ColorHTML.WHITE.getColorHtml() : ViewWithSidebars.ColorHTML.GREYTAB.getColorHtml());
        styleDiv(div);
        courseWithName.getButton().getStyle()
                .set("min-width", "150px")
                .set("text-align", "center");
        div.add(styleDivParagraph(paragraphName, null), styleDivParagraph(paragraph, courseWithName), courseWithName.getButton());
        return div;
    }

    void styleDiv(Div div) {
        div.getStyle()
                .set("display", "flex")
                .set("flex-direction", "row")
                .set("justify-content", "space-around");
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

    private void infoCsv() {
        VerticalLayout masterLayout = new VerticalLayout();
        Dialog info = new Dialog();
        H1 p = new H1("Format du fichier .csv");
        Paragraph paragraph = new Paragraph("Voici un exemple de l'ordre des valeurs. " +
                                                    "Ne pas mettre la première ligne avec le nom des colonnes.");
        Image image = new Image("img/exampleCSV.png", "");
        image.setWidth("60%");
        image.setHeight("auto");
        info.setWidth("40%");
        info.setHeight("auto");
        p.getStyle().set("margin-top", "50px");
        masterLayout.add(p, paragraph, image, upload);
        masterLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        info.add(masterLayout);
        info.open();
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
        form.closeDialog();
        removeClassName("editing");
    }

    Paragraph styleDivParagraph(Paragraph paragraph, CourseWithName courseWithName) {
        paragraph.getStyle()
                .set("min-width", "150px")
                .set("text-align", "center");
        if (courseWithName != null) {
            paragraph.addClickListener(paragraphClickEvent -> UI.getCurrent().getPage().executeJs("window.location.href='" + courseWithName.getUrl() + "moodle/" + courseWithName.getCourseObject().getId() + "'"));
            paragraph.getStyle()
                    .set("cursor", "pointer")
                    .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml())
                    .set("text-decoration", "underline");
        }
        return paragraph;
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
        Button button = new Button("Messages");
        styleButtonok(button);
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

    private void configureFirstNameFilter() {
        firstNameFilter.setPlaceholder("Filtrer par prénom...");
        firstNameFilter.setClearButtonVisible(true);
        firstNameFilter.setValueChangeMode(ValueChangeMode.LAZY);
        firstNameFilter.addValueChangeListener(e -> updateList());
    }

    private void deletePerson(UserForm.DeleteEvent evt) {
        controller.deleteUser(evt.getPerson());
        updateList();
        closeEditor();
    }

    private void createTabs() {
        form = new UserForm(personRepository, courseRepository, publicChatMessageRepository, groupMembersRepository);
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
            chatController.deleteMessage(item);
            dataProvider.getItems().remove(item);
            dataProvider.refreshAll();
        });
        button.getStyle()
                .set("background-color", ViewWithSidebars.ColorHTML.DANGER.getColorHtml())
                .set("color", ViewWithSidebars.ColorHTML.WHITE.getColorHtml());
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
        private final Button remove;
        private final MoodlePage courseObject;

        public CourseWithName(Course course, String name, Controller controller, AssignmentController assignmentController) {
            this.course = course.getName();
            this.teacher = name;
            this.courseObject = controller.getHomePageCourse(course.getId(), true);
            this.remove = new Button("Supprimer", buttonClickEvent -> {
                deleteCourse(course.getId(), controller, assignmentController);
                UI.getCurrent().getPage().executeJs("window.location.href='" + getUrl() + "admin'");
            });
            remove.getStyle()
                    .set("background-color", ViewWithSidebars.ColorHTML.DANGER.getColorHtml())
                    .set("color", ViewWithSidebars.ColorHTML.WHITE.getColorHtml());
        }

        @SneakyThrows
        private String getUrl(){
            VaadinServletRequest req = (VaadinServletRequest) VaadinService.getCurrentRequest();
            StringBuffer uriString = req.getRequestURL();
            URI uri = new URI(uriString.toString());
            return uri.toString();
        }

        void deleteCourse(long course, Controller controller, AssignmentController assignmentController) {
            for (Group group : controller.selectGroupeCourse(course)) controller.removeGroupMembers(group.getId());
            controller.removeGroups(course);
            assignmentController.removeUploadsStudent(course);
            assignmentController.removeAssignment(course);
            for (PublicTextChannel publicTextChannel : controller.getAllChannelsForCourse(course)) {
                for (PublicChatMessage publicChatMessage : controller.listPosts(publicTextChannel.getId())) {
                    controller.removePosts(publicChatMessage.getId());
                }
            }
            controller.removeChannels(course);
            controller.removeMoodlePage(course);
            controller.removeCourse(course);
        }

        public String getName() {
            return teacher;
        }

        public String getCourse() {
            return course;
        }

        public Button getButton() {
            return remove;
        }

        public MoodlePage getCourseObject() { return courseObject; }

    }

}
