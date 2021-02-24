package app.web.layout;

import app.jpa_repo.CourseRepository;
import app.model.courses.Course;
import app.web.views.MoodleView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

@CssImport("./styles/style.css")
@StyleSheet("https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap")
public class Navbar extends AppLayout {

    private final CourseRepository courseRepository;

    public Navbar(@Autowired CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
        setPrimarySection(AppLayout.Section.NAVBAR);
        // create logo
        Image img = new Image("img/Discool.svg", "Discool Logo");
        img.setHeight("44px");
        img.getStyle().set("margin-left", "20px");
        // create logout link
        Anchor logout = new Anchor("logout", "Log out");

        // add the element to the navbar
        addToNavbar(img, createHeaderContent(), printCourseBar(), logout);
    }

    private Component createHeaderContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setId("header");
        layout.getThemeList().set("white", true);
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        return layout;
    }

    private VerticalLayout printCourseBar() {
        // methode temporaire pour chercher tous les cours
        VerticalLayout layout = new VerticalLayout();
        ArrayList<Course> courses = (ArrayList<Course>) courseRepository.findAll();
        for (Course c : courses) {
            layout.add(new RouterLink(c.getName(), MoodleView.class, c.getId()));
        }
        return layout;
    }
}

