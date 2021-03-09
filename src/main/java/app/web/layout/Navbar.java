package app.web.layout;

import app.jpa_repo.CourseRepository;
import app.model.courses.Course;
import app.web.components.ComponentButton;
import app.web.views.HomeView;
import app.web.views.MoodleView;
import app.web.views.ViewWithSidebars;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.ArrayList;

@Push
@CssImport("./styles/style.css")
@StyleSheet("https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap")
public class Navbar extends AppLayout {

    //TODO: adapt the display of the buttons according to the user
    //TODO: the background of the selected button changes color when clicked

    private final CourseRepository courseRepository;
    private String[][] settingMenu = {
            {"img/chatBubble.svg", "Messages privés"},
            {"img/manageAccounts.svg", "Paramètres des utilisateurs"},
            {"img/settings.svg", "Paramètres de serveurs"}
    };

    public Navbar(@Autowired CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
        subMenuLeft();
        printCourseBar();
        subMenuRight(settingMenu);
    }

    /**
     * Generates the navigation bar submenu that contains the logo
     */
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
        String t=s.substring(s.length()-1);//TODO: edit with the correct redirect values
        //System.out.println(s);
        //System.out.println(t);

        long tmp = 0;//TODO: edit with the correct redirect values
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
/**/
            if (t.equals(c.getId()+"")){//TODO: edit with the correct redirect values
                routerLink.getStyle()
                        .set("border-radius","10px 10px 0 0")
                        .set("padding","0 10px")
                        .set("background-color", ViewWithSidebars.ColorHTML.GREY.getColorHtml());
            }

        }
        ComponentButton button = createServDockImage(new Image("img/add.svg", "Create serveur"), Key.NAVIGATE_NEXT);
        button.getStyle()
                .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
        RouterLink routerLink = new RouterLink("", MoodleView.class, tmp);
/**/
        if (t.equals(0+"")){//TODO: edit with the correct redirect values
            routerLink.getStyle()
                    .set("border-radius","10px 10px 0 0")
                    .set("padding","0 10px")
                    .set("background-color", ViewWithSidebars.ColorHTML.GREY.getColorHtml());
        }
        routerLink.addClassName("colored");//TODO: Supp

        linkRouteurImge(servCardDock, button, routerLink);
        addToNavbar(servCardDock);
    }

    /**
     * Generates the submenu of the navigation bar which contains user, server and private message settings
     */
    private void subMenuRight(String[][] pathImage) {
        long tmp = 0;//TODO: edit with the correct redirect values
        HorizontalLayout servCardDock = HorizontalLayoutCustom();
        servCardDock.getStyle().set("margin", "0");
        for (String[] imageInfo : pathImage) {
            ComponentButton button = createServDockImage(new Image(imageInfo[0], imageInfo[1]), Key.NAVIGATE_NEXT);
            button.getStyle()
                    .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
            RouterLink routerLink = new RouterLink("", MoodleView.class, tmp);
            linkRouteurImge(servCardDock, button, routerLink);
        }
        addToNavbar(servCardDock);
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

    /*formerly in Componenent Builder*/

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
