package app.web.components;

import app.controller.AssignmentController;
import app.controller.Controller;
import app.model.courses.Course;
import app.model.users.Group;
import app.model.users.GroupMembers;
import app.model.users.Person;
import app.web.layout.Navbar;
import app.web.views.ViewWithSidebars;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.io.File;
import java.util.*;

public class ServerFormComponent extends Dialog {

    private final Controller controller;
    private final Person currentPerson;
    private final String newDirName = "course_pic/";

    public ServerFormComponent(Controller controller, Person currentUser) {
        this.controller = controller;
        this.currentPerson = currentUser;
        new CreateServerFormComponent();
    }

    public ServerFormComponent(Controller controller, AssignmentController assignmentController, Person currentUser, Course course) {
        this.controller = controller;
        this.currentPerson = currentUser;
        new EditServerFormComponent(course, assignmentController);

    }

    private Grid<Person> createUserTeacherGrid() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(controller.findAllUserByRole(Person.Role.TEACHER));
        grid.addColumn(Person::getUsername).setHeader("Nom");
        grid.addColumn(Person::getRole).setHeader("Role");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        return grid;
    }

    private Grid<Person> createUserStudentGrid() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(controller.findAllUserByRole(Person.Role.STUDENT));
        grid.addColumn(Person::getUsername).setHeader("Nom");
        grid.addColumn(Person::getRole).setHeader("Role");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        return grid;
    }

    private Grid<Group> createGroupGrid() {
        Grid<Group> grid = new Grid<>();
        grid.setItems(controller.findGroupAll());
        grid.addColumn(Group::getName).setHeader("Groupe");
        grid.addColumn(Group::getDescription).setHeader("Description");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        return grid;
    }


    private Tabs createTabs(Tab user, Tab teacher, Tab group, Grid<Person> userGrid, Grid<Person> teacherGrid, Grid<Group> groupGrid) {
        Tabs tabsGrid = new Tabs(teacher, user, group);
        Map<Tab, Grid> tabsToPages = new HashMap<>();
        initialiseTab(tabsToPages, user, teacher, group, userGrid, teacherGrid, groupGrid);
        tabsToPages.values().forEach(e -> e.setVisible(false));
        teacherGrid.setVisible(true);
        tabsGrid.addSelectedChangeListener(event -> {
            tabsToPages.values().forEach(e -> e.setVisible(false));
            tabsToPages.get(tabsGrid.getSelectedTab()).setVisible(true);
        });
        tabsGrid.getStyle().set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
        return tabsGrid;
    }

    private void initialiseTab(Map<Tab, Grid> tabsToPages, Tab user, Tab teacher, Tab group, Grid<Person> userGrid, Grid<Person> teacherGrid, Grid<Group> groupGrid) {
        tabsToPages.put(teacher, teacherGrid);
        tabsToPages.put(user, userGrid);
        tabsToPages.put(group, groupGrid);
    }

    private long getRandom() {
        Random r = new Random();
        return r.nextLong();
    }

    private class CreateServerFormComponent {

        private final Paragraph errorInput = new Paragraph("");

        CreateServerFormComponent() {
            createDialog();
        }

        private void createDialog() {
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            HorizontalLayout horizontalLayout = new HorizontalLayout();

            Div tabGrid = new Div();
            H1 title = new H1("Creation d'un cours");
            title.getStyle().set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
            Label labelGrid = new Label("Selectionner des utilisateurs pour votre cours:");

            TextField fieldUserInput = new TextField();
            fieldUserInput.setLabel("Nom du cours: ");
            String name = String.valueOf(getRandom());


            Image image = new Image(newDirName + "default.png", "image_serveur");
            Grid<Person> teacherUser = createUserTeacherGrid();
            Grid<Person> studentsUser = createUserStudentGrid();
            Grid<Group> groupUser = createGroupGrid();
            ComponentButton server_image = Navbar.createServDockImage(image, Key.NAVIGATE_NEXT);
            ChangeServerPictureDialog pictureDialog = new ChangeServerPictureDialog(server_image, name);

            Button changePicture = new Button("Changer l'image du cours", buttonClickEvent1 -> pictureDialog.open());

            Tabs tabs = createTabs(new Tab("Liste des étudiants"), new Tab("Liste des professeurs"), new Tab("Liste des étudiants"), studentsUser, teacherUser, groupUser);

            Button valider = new Button("Valider", buttonClickEvent1 -> {
                if (!fieldUserInput.isEmpty()) {
                    Course course = controller.createServer(currentPerson.getId(), fieldUserInput.getValue(), newDirName + "default.png");
                    saveCoursePath(course, name);
                    controller.createGroup(course, fieldUserInput.getValue());
                    Group groupCreate = controller.getLastGroup();
                    addGridListToGroupList(teacherUser, studentsUser, groupUser, groupCreate);
                    close();
                    UI.getCurrent().getPage().reload();
                } else {
                    errorInput();
                }
            });

            horizontalLayout.add(server_image, changePicture);
            tabGrid.add(tabs, teacherUser, studentsUser, groupUser);
            verticalLayout.add(title, fieldUserInput, horizontalLayout, labelGrid, tabGrid, errorInput, valider);
            add(verticalLayout);
        }

        private void saveCoursePath(Course course, String name) {
            File f = new File("src/main/webapp/course_pic/" + name + ".jpg");
            if (f.exists()) course.setPathIcon("course_pic/" + name + ".jpg");
            else course.setPathIcon("course_pic/" + "default.png");
            controller.saveCourse(course);
        }

        private void addGridListToGroupList(Grid<Person> teacherUser, Grid<Person> studentsUser, Grid<Group> groupUser, Group groupCreate) {
            controller.addPersonToCourse(controller.getPersonById(currentPerson.getId()), groupCreate);
            Set<Person> teacher = teacherUser.getSelectedItems();
            for (Person p : teacher) controller.addPersonToCourse(p, groupCreate);

            Set<Person> Students = studentsUser.getSelectedItems();
            for (Person p : Students) controller.addPersonToCourse(p, groupCreate);

            Set<Group> group = groupUser.getSelectedItems();
            for (Group g : group) {
                ArrayList<GroupMembers> personGroup = controller.getPersonByGroupId(g.getId());
                for (GroupMembers group_list : personGroup) {
                    controller.addPersonToCourse(controller.getPersonById(group_list.getUserId()), groupCreate);
                }
            }
        }

        private void errorInput() {
            errorInput.setText("Erreur ! Champs invalides");
            errorInput.getStyle().set("color", "red");
        }

    }

    private class EditServerFormComponent {

        private final AssignmentController assignmentController;
        private final Course course;
        private final String name = String.valueOf(getRandom());


        EditServerFormComponent(Course course, AssignmentController assignmentController) {
            this.course = course;
            this.assignmentController = assignmentController;
            createDialog();
        }

        private void createDialog() {
            Image image = new Image(course.getPathIcon(), "image");
            TextField fieldUserInput = new TextField();
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            HorizontalLayout horizontalImageLayout = new HorizontalLayout();

            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            VerticalLayout layoutVertical = new VerticalLayout();
            layoutVertical.setAlignItems(FlexComponent.Alignment.CENTER);
            fieldUserInput.setValue(course.getName());
            fieldUserInput.setLabel("Nom du cours: ");

            Grid<Person> listPresentCourse = createGridKickUser();
            ComponentButton server_image = Navbar.createServDockImage(image, Key.NAVIGATE_NEXT);
            ChangeServerPictureDialog pictureDialog = new ChangeServerPictureDialog(server_image, name);
            Button changePicture = new Button("Changer l'image du cours", buttonClickEvent1 -> pictureDialog.open());
            Button valider = new Button("Valider", event -> {
                if (!fieldUserInput.isEmpty()) {
                    course.setName(fieldUserInput.getValue());
                }
                saveCoursePath(name);
                close();
                UI.getCurrent().getPage().reload();
            });

            Button supprimer = createSuppCourse();
            Button fermer = createCloseButton();
            Button ajouterMembre = createAddMember();
            horizontalLayout.add(supprimer, fermer, ajouterMembre, valider);
            horizontalImageLayout.add(server_image, changePicture);
            layoutVertical.add(fieldUserInput, horizontalImageLayout, listPresentCourse, horizontalLayout);
            add(layoutVertical);
        }

        private Grid<Person> createGridKickUser() {
            Grid<Person> listPresentCourse = new Grid<>();
            listPresentCourse.setItems(controller.getAllUsersForCourse(course.getId()));
            listPresentCourse.addColumn(Person::getUsername).setHeader("Nom");
            listPresentCourse.addColumn(Person::getRole).setHeader("Role");
            listPresentCourse.addComponentColumn(item -> createRemoveButton(listPresentCourse, item, controller.getGroupByCourseId(course.getId())));
            return listPresentCourse;
        }

        private void saveCoursePath(String name) {
            File f = new File("src/main/webapp/course_pic/" + name + ".jpg");
            if (f.exists()) course.setPathIcon("course_pic/" + name + ".jpg");
            else course.setPathIcon("course_pic/" + "default.png");
            controller.saveCourse(course);
        }

        private Button createSuppCourse() {
            Button b = new Button("Supprimer le cours");
            b.getStyle().set("color", "red");
            b.addClickListener(event -> {
                controller.deleteCourse(course.getId(), assignmentController);
                close();
                UI.getCurrent().navigate("home");
                UI.getCurrent().getPage().reload();
            });
            return b;
        }

        private Button createRemoveButton(Grid<Person> grid, Person person, Group grp) {
            Button exclure = new Button("Exclure");
            exclure.getStyle().set("color", "red");
            exclure.addClickListener(event -> {
                if (currentPerson.getId() != person.getId()) {
                    if (controller.existPersonIntoCourse(person, grp)) {
                        ListDataProvider<Person> dataProvider = (ListDataProvider<Person>) grid.getDataProvider();
                        controller.deleteUserInGroup(person);
                        dataProvider.getItems().remove(person);
                        dataProvider.refreshAll();
                    }
                } else {
                    Notification.show("Vous ne pouvez pas vous exclure du serveur !");
                }
            });
            return exclure;
        }

        private Button createAddMember() {
            Button addMemberButton = new Button("Ajouter des membres");
            addMemberButton.addClickListener(event -> {
                createDialogUser();
            });
            return addMemberButton;
        }

        private Button createCloseButton() {
            Button closeButton = new Button("Fermer");
            closeButton.addClickListener(event -> {
                close();
                UI.getCurrent().getPage().reload();
            });
            return closeButton;
        }

        private void createDialogUser() {
            Dialog addUserDilaog = new Dialog();
            VerticalLayout layoutVertical = new VerticalLayout();
            HorizontalLayout layoutHorizontal = new HorizontalLayout();
            H1 title = new H1("Ajouter des membres dans le cours");
            title.getStyle().set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
            layoutVertical.setAlignItems(FlexComponent.Alignment.CENTER);
            Div tabGrid = new Div();
            Label labelGrid = new Label("Selectionner des utilisateurs pour votre cours:");
            Grid<Person> teacherUser = createUserTeacherGrid();
            Grid<Person> studentsUser = createUserStudentGrid();
            Grid<Group> groupUser = createGroupGrid();

            Tabs tabs = createTabs(new Tab("Liste des étudiants"), new Tab("Liste des professeurs"), new Tab("Liste des étudiants"), studentsUser, teacherUser, groupUser);
            tabGrid.add(tabs, teacherUser, studentsUser, groupUser);

            Button fermer = new Button("Fermer", buttonClickEv -> addUserDilaog.close());
            Button valider = new Button("Valider", buttonClickEvent1 -> {
                close();
                saveCoursePath(name);
                Group groupSelect = controller.getGroupByCourseId(course.getId());
                controller.addPersonToCourse(controller.getPersonById(currentPerson.getId()), groupSelect);
                Set<Person> teacher = teacherUser.getSelectedItems();
                for (Person p : teacher) controller.addPersonToCourse(p, groupSelect);

                Set<Person> Students = studentsUser.getSelectedItems();
                for (Person p : Students) controller.addPersonToCourse(p, groupSelect);

                Set<Group> group = groupUser.getSelectedItems();
                for (Group g : group) {
                    ArrayList<GroupMembers> personGroup = controller.getPersonByGroupId(g.getId());
                    for (GroupMembers group_list : personGroup) {
                        controller.addPersonToCourse(controller.getPersonById(group_list.getUserId()), groupSelect);
                    }
                }
                UI.getCurrent().getPage().reload();
            });

            fermer.getStyle()
                    .set("color", "white")
                    .set("background-color", "red");
            valider.getStyle()
                    .set("color", ViewWithSidebars.ColorHTML.WHITE.getColorHtml())
                    .set("background-color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());

            layoutHorizontal.add(fermer, valider);
            layoutVertical.add(title, labelGrid, tabGrid, layoutHorizontal);
            addUserDilaog.add(layoutVertical);
            addUserDilaog.open();
        }

    }

    private class ChangeServerPictureDialog extends Dialog {

        private final ComponentButton button;
        private final String namePath;

        public ChangeServerPictureDialog(ComponentButton button, String namePath) {
            this.button = button;
            this.namePath = namePath;
            createDialog();
        }

        private void createDialog() {
            H2 title = new H2("Charger une image de server");
            title.getStyle()
                    .set("text-align", "center")
                    .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
            Paragraph instructions = new Paragraph("Choissisez un fichier depuis votre navigateur.\n " +
                    "Seulement les fichiers .jpg sont acceptés.");

            UploadComponent uploadComponent = new UploadComponent("50px", "96%", 1, 30000000,
                    "src/main/webapp/course_pic",
                    ".jpg");

            uploadComponent.addSucceededListener(event -> {
                String oldName = uploadComponent.getFileName();
                String newName = "";

                try {
                    newName = renameFile(oldName, namePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Image img = new Image(newName.replace("src/main/webapp/", ""), namePath);
                img.getStyle()
                        .set("padding", "0")
                        .set("margin", "12px 6px 6px 6px")
                        .set("height", "50px")
                        .set("width", "50px")
                        .set("border-radius", "10px")
                        .set("cursor", "pointer");

                button.setIcon(img);
                button.click();
                Notification.show("Votre image de serveur a été changé avec succes");
                this.close();
            });
            uploadComponent.addFailedListener(event -> errorDialog(this, "Une erreur de connexion s'est produite"));
            uploadComponent.addFileRejectedListener(event -> errorDialog(this, "Votre fichier ne respecte pas les conditions d'envoie"));
            this.add(title, instructions, uploadComponent);
        }

        private String renameFile(String oldName, String linkName) throws Exception {
            String extension = getExtensionImage(oldName);
            String newPath = "src/main/webapp/course_pic" + "/" + linkName + "." + extension;
            File old = new File(oldName);
            File newFile = new File(newPath);

            if (newFile.exists()) newFile.delete();
            if (!old.renameTo(newFile)) {
                throw new Exception("File can't be renamed");
            }
            return newPath;
        }

        private String getExtensionImage(String name) {
            String[] tab_name = name.toLowerCase().split("\\.");
            if (tab_name.length > 2) return "";
            if (tab_name[1].contains("jpg") || tab_name[1].contains("jpeg") || tab_name[1].contains("png")) {
                return tab_name[1];
            }
            return "";
        }

        private void errorDialog(Dialog parent, String subtitle) {
            Dialog erreurDialog = new Dialog();
            VerticalLayout layout = new VerticalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            H1 title = new H1("Une erreur est survenue, ressayez !");
            Paragraph p = new Paragraph(subtitle);
            layout.add(title, p);
            erreurDialog.add(layout);
            parent.close();
            erreurDialog.open();
            erreurDialog.addDialogCloseActionListener(event -> {
                erreurDialog.close();
                parent.open();
            });
        }
    }

}
