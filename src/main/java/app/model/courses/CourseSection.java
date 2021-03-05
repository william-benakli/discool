package app.model.courses;

import app.jpa_repo.CourseSectionRepository;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "course_sections")
@Table(name = "course_sections")
public class CourseSection {


    @Id // to say this is the primary key in the database
    @GeneratedValue(strategy = GenerationType.IDENTITY) // to generate the id
    @Column(
            name = "id", // the name of the column in the database
            updatable = false // so that the value can't be updated
    )
    private long id;

    @Column(
            name = "courseid",
            nullable = false
    )
    private long courseId;

    @Column(
            name = "parentid"
    )
    private Long parentId;

    @Column(
            name = "title",
            nullable = false
    )
    private String title;


    @Column(
            name = "content",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;

    /**
     * The object that represents the parent, useful to convert an ArrayList to a LinkedList of CourseSections
     */
    @Transient // tell JPA not to take this into account when looking in the database
    private CourseSection parent;

    /**
     * Sorts the ArrayList of results and returns a LinkedList with the elements in the right order
     * (that is to say that each CourseSection follows its parent)
     *
     * @param list The ArrayList to
     * @return a LinkedList with all the values in the right order
     */
    public static LinkedList<CourseSection> sort(ArrayList<CourseSection> list) {
        LinkedList<CourseSection> sortedList = new LinkedList<>();
        sortedList.addFirst(findHead(list));
        while (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                CourseSection c = list.get(i);
                if (c.getParentId() == sortedList.getLast().getId()) {
                    sortedList.addLast(c);
                    list.remove(c);
                }
            }
        }
        return sortedList;
    }

    /**
     * Returns the element that is at the top of the list (when parentId is -1) and removes this element from the list
     *
     * @param list The ArrayList that contains all the values
     * @return The CourseSection that is at the top (or null)
     */
    private static CourseSection findHead(ArrayList<CourseSection> list) {
        for (CourseSection c : list) {
            if (c.parentId == null) {
                list.remove(c);
                return c;
            }
        }
        return null;
    }

    /**
     * Finds the parent in the database by its id and assigns it to the variable.
     *
     * @param repository The JPA repo to get the parent from
     */
    public void addParent(CourseSectionRepository repository) {
        if (parentId == null) return;
        Optional<CourseSection> courseSectionParent = repository.findById(parentId);
        parent = courseSectionParent.orElse(null);
    }
}
