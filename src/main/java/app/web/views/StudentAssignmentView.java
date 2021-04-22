package app.web.views;

import app.controller.AssignmentController;
import app.controller.Controller;
import app.controller.security.SecurityUtils;
import app.jpa_repo.*;
import app.model.courses.Assignment;
import app.model.courses.Course;
import app.model.courses.StudentAssignmentUpload;
import app.model.users.Person;
import app.web.components.UploadComponent;
import app.web.layout.Navbar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route(value = "assignment", layout = Navbar.class)
public class StudentAssignmentView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final PersonRepository personRepository;
    private Course course;
    private Assignment assignment;

    private Person currentUser;
    private StudentAssignmentUpload studentAssignmentUpload;
    private final FlexLayout assignmentBar = new FlexLayout();

    public StudentAssignmentView(@Autowired CourseRepository courseRepository,
                                 @Autowired TextChannelRepository textChannelRepository,
                                 @Autowired AssignmentRepository assignmentRepository,
                                 @Autowired PersonRepository personRepository,
                                 @Autowired StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository,
                                 @Autowired GroupRepository groupRepository,
                                 @Autowired GroupMembersRepository groupMembersRepository) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.personRepository = personRepository;
        setPersonRepository(personRepository);
        setController(new Controller(personRepository, textChannelRepository, null,
                                     courseRepository, null, groupRepository, groupMembersRepository));
        setAssignmentController(new AssignmentController(personRepository, assignmentRepository,
                                                         studentAssignmentsUploadsRepository, courseRepository));
    }

    @SneakyThrows // so that javac doesn't complain about not catching the exception
    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        Optional<Assignment> a = assignmentRepository.findById(parameter);
        assignment = a.orElse(null);
        if (assignment == null) {
            throw new Exception("There is no course with this ID.");
            // TODO : take care of the exception (issue 28)
        }
        Optional<Course> c = courseRepository.findById(assignment.getCourseId());
        course = c.orElse(null);
        if (course == null) {
            throw new Exception("There is no course with this ID.");
            // TODO : take care of the exception (issue 28)
        }
        currentUser = SecurityUtils.getCurrentUser(personRepository);
        studentAssignmentUpload = getAssignmentController()
                .findStudentAssignmentSubmission(assignment.getId(), currentUser.getId());
        createSidebar(course.getId());
        createMembersBar(course.getId());
        createAssignmentBar();
        createLayout(assignmentBar);
    }

    @Override
    public String getPageTitle() {
        return course.getName();
    }

    private void createAssignmentBar() {
        assignmentBar.removeAll();
        setCardStyle(assignmentBar, "60%", ColorHTML.GREY);
        H1 title = new H1(assignment.getName());
        title.getStyle().set("color",ColorHTML.PURPLE.getColorHtml());
        assignmentBar.add(title);
        StudentAssignmentLayout layout = new StudentAssignmentLayout(assignment);
        assignmentBar.add(layout);
    }

    private class StudentAssignmentLayout extends VerticalLayout {
        private final Assignment assignment;

        public StudentAssignmentLayout(Assignment assignment) {
            this.assignment = assignment;
            writeInfo();

            if (studentAssignmentUpload == null) {
                // only allow uploads if the student hasn't already submitted a file
                createUploadZone();
            }
        }

        private void constTab(Div div, String text, ColorHTML color){
            div.getStyle()
                    .set("width","100%")
                    .set("height","50px")
                    .set("background-color", String.valueOf(color))
                    .set("display","flex")
                    .set("padding-left","40px")
                    .set("padding-right","30px");
            div.add(new Paragraph(text));
        }

        private Div tabHorizontal(){
            Div div=new Div();
            Div divLeft=new Div();
            divLeft.getStyle().set("width","25%");

            Div divRight=new Div();
            div.getStyle()
                    .set("display","flex")
                    .set("flex-direction","row")
                    .set("overflow","hidden");
            div.setWidth("100%");

            divRight.setWidth("74%");

            Div dueDate=new Div();
            Div dueDateR=new Div();
            constTab(dueDate, "date", ColorHTML.WHITE);
            constTab(dueDateR, Long.toString(assignment.getDuedate()), ColorHTML.WHITE);

            if (assignment.getAllowLate() == 1) {
                Div cutOffDate = new Div();
                Div cutOffDateR = new Div();
                constTab(cutOffDate, "cut off date", ColorHTML.DARKGREY);
                constTab(cutOffDateR, Long.toString(assignment.getCutoffdate()), ColorHTML.DARKGREY);
                divLeft.add(cutOffDate);
                divRight.add(cutOffDateR);
            }

            Div maxGrade=new Div();
            Div maxGradeR=new Div();
            constTab(maxGrade, "max grade", ColorHTML.DARKGREY);
            constTab(maxGradeR, Long.toString(assignment.getMaxGrade()), ColorHTML.DARKGREY);

            Div status=new Div();
            Div statusR=new Div();
            constTab(status, "status", ColorHTML.WHITE);
            constTab(statusR, writeGradeInfo(), ColorHTML.WHITE);
            status.setHeight("100px");
            statusR.setHeight("100px");

            divLeft.add(dueDate, maxGrade, status);
            divRight.add(dueDateR, maxGradeR, statusR);

            div.add(divLeft, divRight);
            return div;
        }

        private void writeInfo() {
            this.add(new Paragraph(assignment.getDescription()));
            add(tabHorizontal());
            //this.add(new Paragraph("max number of attempts : " + assignment.getMaxAttempts()));
        }

        private String writeGradeInfo() {
            //Paragraph grade = new Paragraph();
            String res="";
            if (studentAssignmentUpload != null) {
                if (studentAssignmentUpload.getGrade() == -1) {
                    //grade.setText("Your assignment hasn't been graded yet");
                    res+="Your assignment hasn't been graded yet";
                } else {
                    //grade.setText("Your grade is : " + studentAssignmentUpload.getGrade());
                    res+="Your grade is : " + studentAssignmentUpload.getGrade();
                    if (studentAssignmentUpload.getTeacherComments() == null) {
                        //grade.add(new Paragraph("Your teacher didn't write any comments"));
                        res+="Your teacher didn't write any comments";
                    } else {
                        //grade.add(new Paragraph("Teacher's comments : \n" + studentAssignmentUpload.getTeacherComments()));
                        res+="Teacher's comments : \n" + studentAssignmentUpload.getTeacherComments();
                    }
                }
            } else {
                //grade.add(new Paragraph("You have not submitted an answer yet !"));
                res+="You have not submitted an answer yet !";
            }
            return res;
        }

        private void createUploadZone() {
            String newDirName = "uploads/assignments/" + String.valueOf(assignment.getId()) + "_" +
                    String.valueOf(SecurityUtils.getCurrentUser(personRepository).getId());
            UploadComponent uploadComponent = new UploadComponent("50px", "96%", 1, 30000000,
                                                         newDirName);

            uploadComponent.addSucceededListener(event -> {
                getAssignmentController().saveStudentUploadIfNeeded(assignment.getId(), assignment.getCourseId(),
                                                                    currentUser.getId());
                Notification.show("You successfully uploaded your file !");
            });

            uploadComponent.addFileRejectedListener(event -> {
                Notification.show("Couldn't upload the file");
                Notification.show(event.getErrorMessage());
            });

            uploadComponent.getStyle()
                    .set("margin","auto");

            this.add(uploadComponent);
        }
    }
}
