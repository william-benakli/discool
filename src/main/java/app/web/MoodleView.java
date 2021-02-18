package app.web;

import app.jpa_repo.CourseSectionRepository;
import app.model.courses.CourseSection;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * test class to try to display a Moodle page
 */
@Route("")
public class MoodleView extends VerticalLayout {
    private CourseSectionRepository repo;
    private VerticalLayout sectionList = new VerticalLayout();

    public MoodleView(@Autowired CourseSectionRepository repo) {
        this.repo = repo;
        add(new H1("test"));
        add(sectionList);
    }

    private void refresh() {
        repo.findAll() // get all the sections from the table
                .stream() // stream them
                .map(SectionLayout::new) // create a new SectionLayout for each
                .forEach(sectionList::add); // add them to the sectionList layout to be displayed
    }

    class SectionLayout extends VerticalLayout {
        private H2 title; // will be filled with the value of the title field in the CourseSection
        private TextField content;  // same with the content field

        public SectionLayout(CourseSection section) {
            add(title, content);

            // The UI fields on *this* will be bound to the fields of the same name in the CourseSection class,
            // taking the value from the *section* object
            Binder<CourseSection> binder = new Binder<>(CourseSection.class);
            binder.bindInstanceFields(this);
            binder.setBean(section);

            binder.addValueChangeListener(e -> {
                repo.delete(section); // delete the old section
                repo.save(binder.getBean()); // add the new one
                refresh();
            });
        }
    }
}
