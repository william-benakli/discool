package app.web.layout;

import app.jpa_repo.CourseRepository;
import app.model.courses.Course;
import app.web.views.MoodleView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

@CssImport("./styles/style.css")
@StyleSheet("https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap")
public class MainLayout extends AppLayout {

    private final CourseRepository courseRepository;

    public MainLayout(@Autowired CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
        createHeader();
        printCourseBar();
    }

    /**
     * Cree la nav bar
     */
    private void createHeader() {
        HorizontalLayout header = new HorizontalLayout(/*logo*/);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
        header.addClassName("header");
        header.setHeight("75px");
        addToNavbar(header);
    }


    private void printCourseBar() {
        // methode temporaire pour chercher tous les cours
        VerticalLayout layout = new VerticalLayout();
        ArrayList<Course> courses = (ArrayList<Course>) courseRepository.findAll();
        for (Course c : courses) {
            layout.add(new RouterLink(c.getName(), MoodleView.class, c.getId()));
        }
        addToNavbar(layout);
    }


}
