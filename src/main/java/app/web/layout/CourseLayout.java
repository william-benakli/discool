package app.web.layout;

import app.jpa_repo.CourseRepository;
import app.jpa_repo.TextChannelRepository;
import app.model.chat.TextChannel;
import app.web.views.MoodleView;
import app.web.views.TextChannelView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

public class CourseLayout extends MainLayout {


    public CourseLayout(@Autowired CourseRepository courseRepository) {
        super(courseRepository);
    }

    /**
     * Returns the sidebar with links to each text channel and each Moodle page
     *
     * @param courseId              The id of the course
     * @param textChannelRepository The JPA Repo to query the text channel data
     * @return
     */
    public static VerticalLayout createSideBar(long courseId, TextChannelRepository textChannelRepository) {
        VerticalLayout sideBar = new VerticalLayout();

        sideBar.add(new RouterLink("Page d'accueil", MoodleView.class, courseId));
        ArrayList<TextChannel> textChannels = textChannelRepository.findAllByCourseId(courseId);
        textChannels.forEach(channel -> sideBar.add(new RouterLink(channel.getName(), TextChannelView.class, channel.getId())));
        return sideBar;
    }

}
