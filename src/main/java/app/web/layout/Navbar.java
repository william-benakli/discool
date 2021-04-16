package app.web.layout;

import app.controller.security.SecurityUtils;
import app.jpa_repo.AssignmentRepository;
import app.jpa_repo.CourseRepository;
import app.jpa_repo.CourseSectionRepository;
import app.jpa_repo.PersonRepository;
import app.model.courses.Course;
import app.model.users.Person;
import app.web.components.ComponentButton;
import app.web.views.HomeView;
import app.web.views.MoodleView;
import app.web.views.ViewWithSidebars;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.shared.ui.Transport;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Push(transport = Transport.LONG_POLLING)
@CssImport("./styles/style.css")
@StyleSheet("https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap")
public class Navbar extends AppLayout {

    //TODO: adapt the display of the buttons according to the user #53
    //TODO: the background of the selected button changes color when clicked #42

    private final CourseRepository courseRepository;
    private final PersonRepository personRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final AssignmentRepository assignmentRepository;
    private String[][] settingMenu = {
            {"img/chatBubble.svg", "Messages privés"},
            {"img/manageAccounts.svg", "Paramètres des utilisateurs"},
            {"img/settings.svg", "Paramètres de serveurs"}
    };

    public Navbar(@Autowired CourseRepository courseRepository, @Autowired PersonRepository personRepository,
                  @Autowired CourseSectionRepository courseSectionRepository, @Autowired AssignmentRepository assignmentRepository) {
        this.assignmentRepository=assignmentRepository;
        this.courseSectionRepository = courseSectionRepository;
        this.personRepository=personRepository;
        this.courseRepository = courseRepository;
        subMenuLeft();
        printCourseBar();
        subMenuRight(settingMenu);
    }

    /**
     * Generates the navigation bar submenu that contains the logo
     */
    @SneakyThrows
    private void subMenuLeft() {
        HorizontalLayout servCardDock = HorizontalLayoutCustom();
        servCardDock.getStyle().set("margin", "0");
        ComponentButton button = createServDockImage(new Image("img/Discool.png", "créer un serveur"), Key.NAVIGATE_NEXT);
        button.getStyle()
                .set("width", "200px")
                .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
        RouterLink routerLink = new RouterLink("", HomeView.class);
        routerLink.getStyle()
                .set("margin-left", "16px");
        linkRouteurImge(servCardDock, button, routerLink);
        addToNavbar(servCardDock);
    }

    /**
     * Generates the main sub-menu of the navigation bar which contains the list of servers joined by the user as well as a button to create servers
     */
    @SneakyThrows
    private void printCourseBar() {
        VaadinServletRequest req = (VaadinServletRequest) VaadinService.getCurrentRequest();
        StringBuffer uriString = req.getRequestURL();
        URI uri = new URI(uriString.toString());
        String s=uri.toString();
        String t=s.substring(s.length()-1);//TODO: edit with the correct redirect values #42
        String[] s2=s.split("/");

        long tmp = 0;//TODO: edit with the correct redirect values #42
        HorizontalLayout servCardDock = HorizontalLayoutCustom();
        ArrayList<Course> courses = (ArrayList<Course>) courseRepository.findAll();
        for (Course c : courses) {
            ComponentButton button = createServDockImage(
                    new Image(
                            (c.getPathIcon().length() != 0) ? c.getPathIcon() : "img/DDiscool.svg",
                            (c.getPathIcon().length() != 0) ? c.getName() : "DDiscool"
                    ), Key.NAVIGATE_NEXT);
            button.getStyle()
                    .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
            RouterLink routerLink = new RouterLink("", MoodleView.class, c.getId());
            linkRouteurImge(servCardDock, button, routerLink);
            if (s2.length>=4 && s2[3].equals("moodle") && t.equals(c.getId()+"")){//TODO: edit with the correct redirect values #42
                routerLink.getStyle()
                        .set("border-radius","10px 10px 0 0")
                        .set("padding","0 10px")
                        .set("background-color", ViewWithSidebars.ColorHTML.GREY.getColorHtml());
            }

        }
        RouterLink routerLink = new RouterLink("", MoodleView.class, tmp);
        if (t.equals(0+"")){//TODO: edit with the correct redirect values #42
            routerLink.getStyle()
                    .set("border-radius","10px 10px 0 0")
                    .set("padding","0 10px")
                    .set("background-color", ViewWithSidebars.ColorHTML.GREY.getColorHtml());
        }
        routerLink.addClassName("colored");

        if (! SecurityUtils.isUserStudent()) {
            ComponentButton button = createServDockImage(new Image("img/add.svg", "Create serveur"), Key.NAVIGATE_NEXT);
            Dialog dialog = new Dialog();
            TextField field = new TextField();
            Button valider = new Button("Valider", buttonClickEvent1 -> {
                courseRepository.createServer(sender.getId(), field.getValue(), "img/DDiscool.svg");
                dialog.close();
                UI.getCurrent().getPage().reload();
            });
            button.getStyle()
                    .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
            dialog.add(field, valider);
            button.addClickListener(buttonClickEvent -> dialog.open());
            servCardDock.add(button);
        }
        addToNavbar(servCardDock);
    }

    /**
     * Generates the submenu of the navigation bar which contains user, server and private message settings
     */
    @SneakyThrows
    private void subMenuRight(String[][] pathImage) {
        long tmp = 0;//TODO: edit with the correct redirect values #42
        HorizontalLayout servCardDock = HorizontalLayoutCustom();
        servCardDock.getStyle().set("margin", "0");

        for (String[] imageInfo : pathImage) {
            if (!imageInfo[0].equals("img/settings.svg") || ! SecurityUtils.isUserStudent()) {
                ComponentButton button = createServDockImage(new Image(imageInfo[0], imageInfo[1]), Key.NAVIGATE_NEXT);
                button.getStyle()
                        .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
                if (imageInfo[0].equals("img/manageAccounts.svg")) {
                    Dialog dialog = new Dialog();
                    popupuser(dialog);
                    button.addClickListener(event -> dialog.open());
                    servCardDock.add(button);
                }else if(imageInfo[0].equals("img/settings.svg")){
                    VaadinServletRequest req = (VaadinServletRequest) VaadinService.getCurrentRequest();
                    StringBuffer uriString = req.getRequestURL();
                    URI uri = new URI(uriString.toString());
                    String s=uri.toString();
                    String t=s.substring(s.length()-1);//TODO: edit with the correct redirect values #42
                    String[] s2=s.split("/");
                    System.out.println(Arrays.toString(s2));

                    Dialog dialog = new Dialog();
                    TextField labelField = new TextField();
                    labelField.getStyle()
                            .set("margin","auto");
                    labelField.setLabel("Créer un chanel textuel");
                    Button newChan= new Button("Créer", event -> {
                        courseSectionRepository.createChat( 1L, labelField.getValue());
                        dialog.close();
                    });
                    Div sectionAssignement = new Div();
                    Text assignement = new Text("Ajouter un nouveau travail à rendre");
                    sectionAssignement.getStyle()
                            .set("margin-top","50px");
                    TextField nameAssignement = new TextField("Nom du devoirs");
                    TextArea descriptionAssignement = new TextArea("Description du devoirs");


                    IntegerField noteMax = new IntegerField("Note Maximale");
                    Button validerAssignement = new Button("Ajouter un devoirs", buttonClickEvent -> {
                        assignmentRepository.createAssignement(
                                1L,
                                nameAssignement.getValue(),
                                descriptionAssignement.getValue(),
                                0,
                                0,
                                0,
                                noteMax.getValue(),
                                0);
                        dialog.close();
                        UI.getCurrent().getPage().reload();
                    });


                    sectionAssignement.add(assignement, nameAssignement, descriptionAssignement, noteMax, validerAssignement);

                    dialog.add(labelField, newChan, sectionAssignement);
                    dialog.setWidth("350px");
                    dialog.setHeight("450px");
                    button.addClickListener(event -> dialog.open());
                    servCardDock.add(button);
                }else {
                    RouterLink routerLink = new RouterLink("", MoodleView.class, tmp);
                    linkRouteurImge(servCardDock, button, routerLink);
                }
            }
        }
        addToNavbar(servCardDock);
    }


    /**
     * Set the dialog window for user parameters
     *
     * @param dialog navigation page for user settings
     */
    private void popupuser(Dialog dialog) {
        /*Creation layout*/
        FlexLayout layout = new FlexLayout();
        FlexLayout layoutL = new FlexLayout();
        FlexLayout layoutR = new FlexLayout();

        /*Element Tab*/
        Person sender = SecurityUtils.getCurrentUser(personRepository);

        EmailField emailField = new EmailField("Changer d'e-mail");
        emailField.setClearButtonVisible(true);
        emailField.setErrorMessage("Veuillez entrer une adresse email valide");
        emailField.setPlaceholder(sender.getEmail());

        PasswordField passwordField = new PasswordField();
        passwordField.setLabel("Changer de mot de passe");

        PasswordField passwordFieldConfirmation = new PasswordField();
        passwordFieldConfirmation.setLabel("Mot de passe actuel");

        Button valide = new Button("Valider");
        valide.getStyle()
                .set("background-color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml())
                .set("color", ViewWithSidebars.ColorHTML.WHITE.getColorHtml())
                .set("top","20px")
                .set("text-align","center");

        valide.addClickListener(buttonClickEvent -> {
            personRepository.updateEmailById(sender.getId(),emailField.getValue());
            dialog.close();
            //TODO: add a password field in the database #83
        });

        /*Tab 1*/
        Tab tab1 = new Tab("Mon compte");
        Div page1 = new Div();
        FlexLayout centerElement1 = new FlexLayout();
        styleTab(tab1, centerElement1);
        centerElement1.add(createUserCard(), emailField, passwordField, passwordFieldConfirmation, valide);
        page1.add(centerElement1);

        /*Tab 2*/
        Tab tab2 = new Tab("Voix et Vidéo");
        Div page2 = new Div();
        page2.setVisible(false);
        FlexLayout centerElement2 = new FlexLayout();
        styleTab(tab2, centerElement2);

        /*Input TODO: set up audio output and input #25*/
        Select<String> intput = new Select<>();
        intput.setItems("Option one", "Option two");
        intput.setLabel("Périphérique d'entrée");
        intput.getStyle().set("margin-top","25px");
        Select<String> output = new Select<>();
        output.setItems("Option one", "Option two");
        output.setLabel("Périphérique de sortie");
        centerElement2.add(createUserCard(), intput, output);
        page2.add(centerElement2);

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

        /*Button logout*/
        Button buttonlogout = new Button("Déconnexion");
        buttonlogout.getStyle()
                .set("background-color", ViewWithSidebars.ColorHTML.DANGER.getColorHtml())
                .set("color", ViewWithSidebars.ColorHTML.WHITE.getColorHtml());
        Anchor logout=new Anchor("logout", "");
        logout.getStyle()
                .set("position","absolute")
                .set("bottom","24px")
                .set("width","25%")
                .set("text-align","center");
        logout.add(buttonlogout);

        /*Panel Admin*/
        Div divAdmin = new Div();
        Button panelAdmin = new Button("Panel Admin", event -> {
            UI.getCurrent().navigate("admin");
            dialog.close();
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

        /*Title Dialog*/
        Paragraph paramUser = new Paragraph("Paramètres utilisateur");
        paramUser.getStyle()
                .set("width","25%")
                .set("text-align","center")
                .set("position","absolute")
                .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml())
                .set("font-weight","700");

        /*Style Layout*/
        layout.add(layoutL,layoutR);
        layout.getStyle()
                .set("height","100%")
                .set("width","100%")
                .set("flex-direction","row");

        layoutR.add(tabs, pages);
        layoutR.getStyle()
                .set("height","100%")
                .set("width","75%")
                .set("padding-left","24px")
                .set("flex-direction","column");

        if (sender.isUserAdmin()) layoutL.add(paramUser, logout, divAdmin);
        else layoutL.add(paramUser, logout);

        layoutL.getStyle()
                .set("height","100%")
                .set("width","25%")
                .set("flex-direction","column")
                .set("border-right","solid .5px #EAEAEA")
                .set("padding-right","36px");

        /*Set Dialog*/
        dialog.add(layout);
        dialog.setWidth("50%");
        dialog.setHeight("65%");
    }

    /**
     * Change the style of tabs
     *
     * @param tab1 tab where we will apply css properties
     * @param div div where we will apply css properties
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
     * Create file containing the icon and the user's nickname
     *
     * @return a user card
     */
    private FlexLayout createUserCard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        FlexLayout card = new FlexLayout();
        card.getStyle()
                .set("padding-top","50px");
        Image profilPicture = new Image("img/Chien 3.jpg","Photo de Profil");
        profilPicture.getStyle()
                .set("width","100px")
                .set("height","100px")
                .set("border-radius","50px");
        Paragraph userName= new Paragraph(authentication.getName());
        userName.getStyle()
                .set("margin","auto auto auto 24px")
                .set("font-size","18px")
                .set("font-weight","700")
                .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
        card.add(profilPicture, userName);
        return card;
    }

    /**
     * Create an empty dock to later put buttons to allow the user to navigate in the application
     *
     * @return an empty dock with a style
     */
    private HorizontalLayout HorizontalLayoutCustom() {
        HorizontalLayout servCardDock = new HorizontalLayout();
        servCardDock.getStyle()
                .set("vertical-align", "middle")
                .set("margin", "auto");
        return servCardDock;
    }

    /**
     * Create a button in one of the docks of the navigation bar
     *
     * @param img      Server image
     * @param shortCut shortcuts to access the server
     * @return the button of a server to be able to access it
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
     * Links an image a button and a link router to have a clickable button redirecting to a server
     *
     * @param servCardDock navig ation menu
     * @param button       clickable button
     * @param routerLink   link to server
     */
    private void linkRouteurImge(HorizontalLayout servCardDock, ComponentButton button, RouterLink routerLink) {
        button.getStyle().set("overflow", "hidden");
        routerLink.getElement().appendChild(button.getElement());
        servCardDock.add(routerLink);
    }

}
