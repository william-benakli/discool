package app.web.components;

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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;

import java.io.File;
import java.util.*;

public class ServerFormComponent extends Dialog {

    private Controller controller;


    public ServerFormComponent(Controller controller, Person currentUser) {
        this.controller = controller;


        Div tabGrid = new Div();
        String newDirName = "course_pic/";
        String name = String.valueOf(getRandom());
        Image image = new Image(newDirName + "default.png", "image_serveur");
        TextField field = new TextField();
        ComponentButton server_image = Navbar.createServDockImage(image, Key.NAVIGATE_NEXT);

        ChangeServerPictureDialog pictureDialog = new ChangeServerPictureDialog(server_image, name);

        Button changePicture = new Button("Changer de profil de serveur", buttonClickEvent1 -> {
            pictureDialog.open();
        });


        Grid<Person> teacherUser = createUserTeacherGrid();
        Grid<Person> studentsUser = createUserStudentGrid();
        Grid<Group> groupUser = createGroupGrid();

        Tabs tabs = createTabs(new Tab("Liste des étudiants"), new Tab("Liste des professeurs"), new Tab("Liste des étudiants"), studentsUser, teacherUser, groupUser);


        Button valider = new Button("Valider", buttonClickEvent1 -> {

            System.out.println(newDirName + name + ".jpg" + " exit ?");
            File f = new File("src/main/webapp/course_pic/" + name + ".jpg");
            if (f.exists()) {
                controller.createServer(currentUser.getId(), field.getValue(), newDirName + name + ".jpg");
            } else {
                controller.createServer(currentUser.getId(), field.getValue(), newDirName + "default.png");
            }

            Course course = controller.getLastCourse();

            controller.createGroup(course, field.getValue());
            Group groupCreate = controller.getLastGroup();
            controller.addPersonToCourse(controller.getPersonById(currentUser.getId()), groupCreate);

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
            close();
            UI.getCurrent().getPage().reload();
        });


        tabGrid.add(tabs, teacherUser, studentsUser, groupUser);
        add(field, server_image, changePicture, tabGrid, valider);
    }


    private Grid createUserTeacherGrid() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(controller.findAllUserByRole(Person.Role.TEACHER));
        grid.addColumn(Person::getUsername).setHeader("Nom");
        grid.addColumn(Person::getRole).setHeader("Role");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        return grid;
    }

    private Grid createUserStudentGrid() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(controller.findAllUserByRole(Person.Role.STUDENT));
        grid.addColumn(Person::getUsername).setHeader("Nom");
        grid.addColumn(Person::getRole).setHeader("Role");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        return grid;
    }

    private Grid createGroupGrid() {
        Grid<Group> grid = new Grid<>();
        grid.setItems(controller.findGroupAll());
        grid.addColumn(Group::getName).setHeader("Groupe");
        grid.addColumn(Group::getDescription).setHeader("Description");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        return grid;
    }


    public Tabs createTabs(Tab user, Tab teacher, Tab group, Grid userGrid, Grid teacherGrid, Grid groupGrid) {
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

    public void initialiseTab(Map<Tab, Grid> tabsToPages, Tab user, Tab teacher, Tab group, Grid userGrid, Grid teacherGrid, Grid groupGrid) {
        tabsToPages.put(teacher, teacherGrid);
        tabsToPages.put(user, userGrid);
        tabsToPages.put(group, groupGrid);
    }

    public long getRandom() {
        Random r = new Random();
        return r.nextLong();
    }


    private class ChangeServerPictureDialog extends Dialog {

        ComponentButton button;
        String name;

        public ChangeServerPictureDialog(ComponentButton button, String name) {
            this.button = button;
            this.name = name;
            createDialog();
            open();
        }

        private void createDialog() {
            H2 title = new H2("Upload a new server picture");
            title.getStyle()
                    .set("text-align", "center")
                    .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
            Paragraph instructions = new Paragraph("Choose a new picture from your browser (or drag-and-drop).\n " +
                    "Only .jpg and .jpeg files are accepted.");


            UploadComponent uploadComponent = new UploadComponent("50px", "96%", 1, 30000000,
                    "src/main/webapp/course_pic",
                    "image/jpeg");

            uploadComponent.addSucceededListener(event -> {
                String oldName = uploadComponent.getFileName();
                String newName = "";

                try {
                    newName = renameFile(oldName, "src/main/webapp/course_pic", name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Image img = new Image(newName.replace("src/main/webapp/", ""), name);
                img.getStyle()
                        .set("padding", "0")
                        .set("margin", "12px 6px 6px 6px")
                        .set("height", "50px")
                        .set("width", "50px")
                        .set("border-radius", "10px")
                        .set("cursor", "pointer");

                button.setIcon(img);
                Notification.show("Votre image de serveur a été changé avec succes");
                this.close();
            });

            this.add(title, instructions, uploadComponent);
        }

        public ComponentButton getButtonImage() {
            return button;
        }

        public String renameFile(String oldName, String path, String linkName) throws Exception {
            String extension = getExtensionImage(oldName);
            String newPath = path + "/" + linkName + "." + extension;
            File old = new File(oldName);
            File newFile = new File(newPath);

            if (newFile.exists()) newFile.delete();
            if (!old.renameTo(newFile)) {
                throw new Exception("File can't be renamed");
            }
            return newPath;
        }

        public String getExtensionImage(String name) {
            String[] tab_name = name.toLowerCase().split("\\.");
            if (tab_name.length > 2) return "";
            if (tab_name[1].contains("jpg") || tab_name[1].contains("jpeg") || tab_name[1].contains("png")) {
                return tab_name[1];
            }
            return "";
        }
    }

}
