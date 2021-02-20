package app.web.layout;

import app.jpa_repo.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class CourseLayout extends MainLayout {

    public CourseLayout(@Autowired CourseRepository courseRepository) {
        super(courseRepository);
    }
}
