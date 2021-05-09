package app.web.layout;

import app.controller.AssignmentController;
import app.controller.ChatController;
import app.controller.Controller;
import app.controller.broadcasters.MessageNotificationBroadcaster;
import app.controller.security.SecurityUtils;
import app.jpa_repo.*;
import app.model.courses.Course;
import app.model.courses.MoodlePage;
import app.model.users.GroupMembers;
import app.model.users.Person;
import app.web.components.ComponentButton;
import app.web.components.UploadComponent;
import app.web.views.*;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.shared.ui.Transport;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Push(transport = Transport.LONG_POLLING)
@CssImport("./styles/style.css")
@StyleSheet("https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap")
public class Navbar extends AppLayout {

    private final PersonRepository personRepository;
    private final MoodlePageRepository moodlePageRepository;
    private final Controller controller;
    private final ChatController chatController;
    private Person currentUser;
    private HorizontalLayout courseNavigationDock;
    private HorizontalLayout rightMenuLayout;
    private AssignmentController assignmentController;
    private Registration messageNotificationRegistration;
    private ComponentButton directMessageButton;

    public Navbar(@Autowired CourseRepository courseRepository,
                  @Autowired PublicTextChannelRepository publicTextChannelRepository,
                  @Autowired PersonRepository personRepository,
                  @Autowired MoodlePageRepository moodlePageRepository,
                  @Autowired PublicChatMessageRepository publicChatMessageRepository,
                  @Autowired GroupRepository groupRepository,
                  @Autowired GroupMembersRepository groupMembersRepository,
                  @Autowired AssignmentRepository assignmentRepository,
                  @Autowired StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository,
                  @Autowired PrivateTextChannelRepository privateTextChannelRepository,
                  @Autowired PrivateChatMessageRepository privateChatMessageRepository) {
        this.assignmentController = new AssignmentController(personRepository, assignmentRepository, studentAssignmentsUploadsRepository, courseRepository);
        this.personRepository = personRepository;
        this.moodlePageRepository = moodlePageRepository;
        this.controller = new Controller(personRepository, publicTextChannelRepository, publicChatMessageRepository, courseRepository,
                                         moodlePageRepository, groupRepository, groupMembersRepository);
        this.chatController = new ChatController(personRepository, publicTextChannelRepository, publicChatMessageRepository,
                                                 privateTextChannelRepository, privateChatMessageRepository);
        createLeftSubMenu();
        createCourseNavigationMenu();
        createRightSubMenu();
    }

    /**
     * Generates the navigation bar submenu that contains the logo
     */
    @SneakyThrows
    private void createLeftSubMenu() {
        HorizontalLayout servCardDock = createCustomHorizontalLayout();
        servCardDock.getStyle().set("margin", "0");
        ComponentButton button = createServDockImage(new Image("img/Discool.png", "créer un serveur"), Key.NAVIGATE_NEXT);
        button.getStyle()
                .set("width", "200px")
                .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
        RouterLink routerLink = new RouterLink("", HomeView.class);
        routerLink.getStyle()
                .set("margin-left", "16px");
        linkRouteurImage(servCardDock, button, routerLink);
        addToNavbar(servCardDock);
    }

    /**
     * Generates the main sub-menu of the navigation bar which contains the list of courses
     * the user belongs in.
     * Also contains a button to create a new course if the user is not a student
     */
    @SneakyThrows
    private void createCourseNavigationMenu() {
        currentUser = SecurityUtils.getCurrentUser(personRepository);
        VaadinServletRequest req = (VaadinServletRequest) VaadinService.getCurrentRequest();
        StringBuffer uriString = req.getRequestURL();
        URI uri = new URI(uriString.toString());
        String tmp = uri.toString();
        String[] splitURI = tmp.split("/");
        String cleanURI = splitURI[splitURI.length-1];

        courseNavigationDock = createCustomHorizontalLayout();
        courseNavigationDock.getStyle()
                .set("width","750px")
                .set("overflow","auto")
                .set("overflow-y","hidden")
                .set("transform","rotateX(180deg)");
        List<Course> courses = controller.findAllCourses();
        for (Course c : courses) {
            if (!SecurityUtils.isUserAdmin()) {
                for (GroupMembers groupeMembers : controller.findByUserId(currentUser.getId())) {
                    if (groupeMembers.getGroupId() == c.getId()) createCourseButton(c, splitURI, cleanURI);
                }
            }else createCourseButton(c, splitURI, cleanURI);
        }

        if (! SecurityUtils.isUserStudent()) {
            createAddACourseButton();
        }
        addToNavbar(courseNavigationDock);
    }

    private void createAddACourseButton() {
        ComponentButton button = createServDockImage(new Image("img/add.svg", "Create serveur"), Key.NAVIGATE_NEXT);
        Dialog dialog = new Dialog();
        TextField field = new TextField();
        Button valider = new Button("Valider", buttonClickEvent1 -> {
            controller.createServer(currentUser.getId(), field.getValue(), "img/DDiscool.svg");
            dialog.close();
            UI.getCurrent().getPage().reload();
        });
        button.getStyle()
                .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
        dialog.add(field, valider);
        button.addClickListener(buttonClickEvent -> dialog.open());
        courseNavigationDock.add(button);
    }

    /**
     * Creates a clickable button to redirect to a Course
     * @param c         The course to redirect to
     * @param splitURI  ? // TODO : edit doc #42
     * @param cleanURI  ?
     */
    private void createCourseButton(Course c, String[] splitURI, String cleanURI) {
        ComponentButton button = createServDockImage(
                new Image(
                        (c.getPathIcon().length() != 0) ? c.getPathIcon() : "img/DDiscool.svg",
                        (c.getPathIcon().length() != 0) ? c.getName() : "DDiscool"
                ), Key.NAVIGATE_NEXT);
        button.getStyle()
                .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml())
                .set("transform","rotateX(180deg)");
        long pageId = findHomePageId(c.getId());
        RouterLink routerLink = new RouterLink("", MoodleView.class, pageId);
        linkRouteurImage(courseNavigationDock, button, routerLink);
        //style server
        if (splitURI.length>=4){
            if(splitURI[3].equals("moodle") && controller.getMoodlePage(Integer.parseInt(cleanURI)).getCourseId()==c.getId()){
                styleServer(routerLink);
            }else if((splitURI[3].equals("assignment") || splitURI[3].equals("teacher_assignment")) && assignmentController.getAssignment(Integer.parseInt(cleanURI)).getCourseId()==c.getId()){
                styleServer(routerLink);
            }else if(splitURI[3].equals("channels") && controller.getTextChannel(Integer.parseInt(cleanURI)).getCourseId()==c.getId()){
                styleServer(routerLink);
            }
        }
    }

    private void styleServer(RouterLink routerLink){
        routerLink.getStyle()
                .set("border-radius","0 0 10px 10px")
                .set("padding","0 10px")
                .set("background-color", ViewWithSidebars.ColorHTML.GREY.getColorHtml());
    }

    private long findHomePageId(long courseId) {
        ArrayList<MoodlePage> pages = controller.getAllMoodlePagesForCourse(courseId);
        for (MoodlePage moodlePage : pages) {
            if (moodlePage.isHomePage()) {
                return moodlePage.getId();
            }
        }
        return 0; // no homepage for this course, should throw an error
    }

    /**
     * Generates the submenu of the navigation bar which contains user, server and private message settings
     */
    @SneakyThrows
    private void createRightSubMenu() {
        rightMenuLayout = createCustomHorizontalLayout();
        rightMenuLayout.getStyle()
                .set("margin", "0")
                .set("padding-left","16px");

        createDirectMessageButton();
        createUserParamButton();

        currentUser = SecurityUtils.getCurrentUser(personRepository);
        if (SecurityUtils.isUserAdmin()) {
            createAdminSettingsButton();
        }

        addToNavbar(rightMenuLayout);
    }

    private void createDirectMessageButton() {
        String imagePath;
        if (chatController.hasUnreadMessages(currentUser.getId())) {
            imagePath = "img/message_unread.svg";
        } else {
            imagePath = "img/message_read.svg";
        }
        directMessageButton = createAndStyleButton(imagePath, "Messages privés");
        RouterLink routerLink = new RouterLink("", PrivateTextChannelView.class, (long) -1);
        linkRouteurImage(rightMenuLayout, directMessageButton, routerLink);
    }

    private void createUserParamButton() {
        ComponentButton button = createAndStyleButton("img/manageAccounts.svg", "Paramètres des utilisateurs");
        button.addClickListener(event -> {
            UserParametersDialog userParametersDialog = new UserParametersDialog();
            userParametersDialog.open();
        });
        rightMenuLayout.add(button);
    }

    private void createAdminSettingsButton() {
        ComponentButton button = createAndStyleButton("img/settings.svg", "Paramètres de serveurs");
        RouterLink adminLink = new RouterLink("", PanelAdminView.class);
        adminLink.getElement().appendChild(button.getElement());
        rightMenuLayout.add(adminLink);
    }

    private ComponentButton createAndStyleButton(String imagePath, String altText) {
        ComponentButton button = createServDockImage(new Image(imagePath, altText), Key.NAVIGATE_NEXT);
        button.getStyle()
                .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
        return button;
    }

    /**
     * Create an empty dock and style it.
     * We will add buttons later to allow the user to navigate in the application
     *
     * @return an empty dock with a style
     */
    private HorizontalLayout createCustomHorizontalLayout() {
        HorizontalLayout servCardDock = new HorizontalLayout();
        servCardDock.getStyle()
                .set("vertical-align", "middle")
                .set("margin", "auto");
        return servCardDock;
    }

    /**
     * Create a button in one of the docks of the navigation bar
     *
     * @param img      The picture to put in the navbar to represent a course
     * @param shortCut Shortcuts to access the course
     * @return the button with an Anchor link to the course
     */
    public static ComponentButton createServDockImage(Image img, Key shortCut) {
        img.setHeightFull();
        img.setWidthFull();
        ComponentButton imgButton = new ComponentButton(img);
        imgButton.getStyle()
                .set("padding", "0")
                .set("margin", "12px 6px 6px 6px")
                .set("height", "50px")
                .set("width", "50px")
                .set("border-radius", "10px")
                .set("cursor", "pointer");
        imgButton.addFocusShortcut(shortCut, KeyModifier.ALT);
        return imgButton;
    }

    /**
     * Links an image a button and a link router to have a clickable button redirecting to a link
     *
     * @param servCardDock The layout to put the button in
     * @param button       The clickable button
     * @param routerLink   the link to redirect the user
     */
    private void linkRouteurImage(HorizontalLayout servCardDock, ComponentButton button, RouterLink routerLink) {
        button.getStyle().set("overflow", "hidden");
        routerLink.getElement().appendChild(button.getElement());
        servCardDock.add(routerLink);
    }

    /**
     * The pop-up that allows the user to access their parameters and change their info
     */
    private class UserParametersDialog extends Dialog {
        private final FlexLayout layout = new FlexLayout();
        private final FlexLayout layoutL = new FlexLayout();
        private final FlexLayout layoutR = new FlexLayout();
        private final Div divAdmin = new Div();
        private EmailField emailField;
        private PasswordField passwordField;
        private PasswordField passwordFieldConfirmation;
        private Button valider;
        private FlexLayout audioControlsLayout;
        private Anchor logoutAnchor;
        private Paragraph paramUser;

        public UserParametersDialog() {
            createUserParametersForm();
            createUserTabs();
            createAudioControls();
            createLogoutButton();
            createAdminButtonForUserParamDialog();
            styleUserParamLayout();

            if (SecurityUtils.isUserAdmin()) layoutL.add(paramUser, logoutAnchor, divAdmin);
            else layoutL.add(paramUser, logoutAnchor);

            /*Set Dialog*/
            add(layout);
            setWidth("50%");
            setHeight("65%");
        }

        private void createAdminButtonForUserParamDialog() {
            Button panelAdmin = new Button("Panel Admin", event -> {
                UI.getCurrent().navigate("admin");
                this.close();
            });
            panelAdmin.getStyle()
                    .set("background-color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml())
                    .set("color", ViewWithSidebars.ColorHTML.WHITE.getColorHtml());
            divAdmin.add(panelAdmin);
            divAdmin.getStyle()
                    .set("position","absolute")
                    .set("bottom","64px")
                    .set("width","25%")
                    .set("text-align","center");
        }

        private void createUserParametersForm() {
            emailField = new EmailField("Changer d'e-mail");
            emailField.setClearButtonVisible(true);
            emailField.setErrorMessage("Veuillez entrer une adresse email valide");
            emailField.setPlaceholder(currentUser.getEmail());

            passwordField = new PasswordField();
            passwordField.setLabel("Changer de mot de passe");

            passwordFieldConfirmation = new PasswordField();
            passwordFieldConfirmation.setLabel("Mot de passe actuel");

            valider = new Button("Valider");
            valider.getStyle()
                    .set("background-color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml())
                    .set("color", ViewWithSidebars.ColorHTML.WHITE.getColorHtml())
                    .set("top","20px")
                    .set("text-align","center");

            valider.addClickListener(buttonClickEvent -> {
                personRepository.updateEmailById(currentUser.getId(), emailField.getValue());
                this.close();
            });
        }

        /**
         * Creates the tabs for the user parameters dialog
         */
        private void createUserTabs() {
            /*Tab 1*/
            Tab tab1 = new Tab("Mon compte");
            Div page1 = new Div();
            FlexLayout userInfosLayout = new FlexLayout();
            styleTab(tab1, userInfosLayout);
            userInfosLayout.add(createUserCard(), emailField, passwordField, passwordFieldConfirmation, valider);
            page1.add(userInfosLayout);

            /*Tab 2*/
            Tab tab2 = new Tab("Voix et Vidéo");
            Div page2 = new Div();
            page2.setVisible(false);
            audioControlsLayout = new FlexLayout();
            styleTab(tab2, audioControlsLayout);
            page2.add(audioControlsLayout);

            /*navigation between tabs*/
            Map<Tab, Component> tabsToPages = new HashMap<>();
            tabsToPages.put(tab1, page1);
            tabsToPages.put(tab2, page2);
            Tabs tabs = new Tabs(tab1, tab2);
            Div pages = new Div(page1, page2);

            tabs.addSelectedChangeListener(event -> {
                tabsToPages.values().forEach(page -> page.setVisible(false));
                Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
                selectedPage.setVisible(true);
            });

            layoutR.add(tabs, pages);
        }

        /**
         * Creates the audio input/output options for the user parameters dialog
         */
        private void createAudioControls() {
            // TODO: set up audio output and input #25
            Select<String> input = new Select<>();
            input.setItems("Option one", "Option two");
            input.setLabel("Périphérique d'entrée");
            input.getStyle().set("margin-top","25px");
            Select<String> output = new Select<>();
            output.setItems("Option one", "Option two");
            output.setLabel("Périphérique de sortie");
            audioControlsLayout.add(createUserCard(), input, output);
        }

        /**
         * Creates the logout button to go in the user parameters dialog
         */
        private void createLogoutButton() {
            Button logoutButton = new Button("Déconnexion");
            logoutButton.getStyle()
                    .set("background-color", ViewWithSidebars.ColorHTML.DANGER.getColorHtml())
                    .set("color", ViewWithSidebars.ColorHTML.WHITE.getColorHtml());
            logoutAnchor = new Anchor("logout", "");
            logoutAnchor.getStyle()
                    .set("position","absolute")
                    .set("bottom","24px")
                    .set("width","25%")
                    .set("text-align","center");
            logoutAnchor.add(logoutButton);
        }

        /**
         * Adds style to the user parameters dialog and add a title
         */
        private void styleUserParamLayout() {
            paramUser = new Paragraph("Paramètres utilisateur");
            paramUser.getStyle()
                    .set("width","25%")
                    .set("text-align","center")
                    .set("position","absolute")
                    .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml())
                    .set("font-weight","700");

            layout.add(layoutL,layoutR);
            layout.getStyle()
                    .set("height","100%")
                    .set("width","100%")
                    .set("flex-direction","row");

            layoutR.getStyle()
                    .set("height","100%")
                    .set("width","75%")
                    .set("padding-left","24px")
                    .set("flex-direction","column");

            layoutL.getStyle()
                    .set("height","100%")
                    .set("width","25%")
                    .set("flex-direction","column")
                    .set("border-right","solid .5px #EAEAEA")
                    .set("padding-right","36px");
        }

        /**
         * Change the style of tabs
         *
         * @param tab1 Tab to apply the CSS properties to
         * @param div  Div to apply the CSS properties to
         */
        private void styleTab(Tab tab1, FlexLayout div) {
            tab1.getStyle().set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
            div.getStyle()
                    .set("margin","auto")
                    .set("min-height","100px")
                    .set("width","75%")
                    .set("flex-direction","column");
        }

        /**
         * Create a FlexLayout containing the icon and the user's nickname
         *
         * @return a user card
         */
        private FlexLayout createUserCard() {
            Person currentUser = SecurityUtils.getCurrentUser(personRepository);
            FlexLayout card = new FlexLayout();
            card.getStyle()
                    .set("padding-top","50px");
            FlexLayout profilePictureLayout = createProfilePicture(new Paragraph(currentUser.getUsername()));
            card.add(profilePictureLayout);
            return card;
        }

        /**
         * Create a layout with the profile picture and a button to change the picture
         * @return the layout
         */
        private FlexLayout createProfilePicture(Paragraph userName) {
            FlexLayout ppLayout = new FlexLayout();
            Image profilPicture = currentUser.getProfilePicture();
            Button button = new Button("Change profile picture");
            Div nameButton = new Div();

            nameButton.getStyle()
                    .set("display","flex")
                    .set("flex-direction","column");
            userName.getStyle()
                    .set("margin","auto auto auto 24px")
                    .set("font-size","18px")
                    .set("font-weight","700")
                    .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
            profilPicture.getStyle()
                    .set("width","100px")
                    .set("height","100px")
                    .set("border-radius","50px");
            button.getStyle()
                    .set("background-color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml())
                    .set("color", ViewWithSidebars.ColorHTML.WHITE.getColorHtml())
                    .set("margin","24px 0 12px 24px");

            button.addClickListener(event -> {
                ChangeProfilePictureDialog changeProfilePicture = new ChangeProfilePictureDialog();
            });
            nameButton.add(userName, button);
            ppLayout.add(profilPicture, nameButton);
            return ppLayout;
        }

    }

    private class ChangeProfilePictureDialog extends Dialog {

        public ChangeProfilePictureDialog() {
            createDialog();
            open();
        }

        private void createDialog() {
            H2 title = new H2("Upload a new profile picture");
            title.getStyle()
                    .set("text-align","center")
                    .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
            Paragraph instructions = new Paragraph("Choose a new picture from your browser (or drag-and-drop).\n " +
                                                        "Only .jpg and .jpeg files are accepted.");

            UploadComponent uploadComponent = new UploadComponent("50px", "96%", 1, 30000000,
                                                                  "src/main/webapp/profile_pictures",
                                                                  "image/jpeg");
            uploadComponent.addSucceededListener(event -> {
                String oldName = uploadComponent.getFileName();
                try {
                    renameProfilePicture(oldName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Notification.show("Your profile picture was updated successfully");
                Notification.show("Reload the page to see the changes!");
                this.close();
            });

            this.add(title, instructions, uploadComponent);
        }

        private void renameProfilePicture(String oldName) throws Exception {
            String extension;
            if (oldName.endsWith("jpeg")) {
                extension = ".jpeg";
            } else if (oldName.endsWith("jpg")){
                extension = ".jpg";
            } else if (oldName.endsWith("JPG")) {
                extension = ".JPG";
            } else if (oldName.endsWith("JPEG")) {
                extension = ".JPEG";
            } else {
                System.out.println("Problem while tying to figure out the file's name while renaming the profile picture");
                extension = "";
            }
            File old = new File(oldName);
            File newFile = new File("src/main/webapp/profile_pictures/" + currentUser.getId() + extension.toLowerCase());
            if (!old.renameTo(newFile)) {
                throw new Exception("File can't be renamed");
            }
        }
    }

    //--------- methods to update the direct messages button if there is an unread message
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        messageNotificationRegistration = MessageNotificationBroadcaster.register((notify) -> {
            if (ui.isEnabled() && ui.getUI().isPresent()) {
                ui.access(() -> receiveBroadcast(notify));
            }
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        messageNotificationRegistration.remove();
        messageNotificationRegistration = null;
    }

    private void receiveBroadcast(Boolean notify) {
        String imagePath;
        if (chatController.hasUnreadMessages(currentUser.getId())) {
            imagePath = "img/message_unread.svg";
        } else {
            imagePath = "img/message_read.svg";
        }
        directMessageButton = createAndStyleButton(imagePath, "Messages prives");
    }

}
