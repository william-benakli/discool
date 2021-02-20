package app.web.views;

import app.jpa_repo.CourseRepository;
import app.jpa_repo.CourseSectionRepository;
import app.jpa_repo.TextChannelRepository;
import app.web.layout.MainLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "", layout = MainLayout.class)
public class MainView extends VerticalLayout {

    private final CourseRepository courseRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final TextChannelRepository textChannelRepository;


    public MainView(@Autowired CourseRepository courseRepository,
                    @Autowired TextChannelRepository textChannelRepository,
                    @Autowired CourseSectionRepository courseSectionRepository) {
        this.courseRepository = courseRepository;
        this.courseSectionRepository = courseSectionRepository;
        this.textChannelRepository = textChannelRepository;

        VerticalLayout layout = new VerticalLayout();
        this.add(new RouterLink("moodle 1", MoodleView.class, 1L));
        this.add(new RouterLink("chat1cours1", TextChannelView.class, 1L));
    }

}
