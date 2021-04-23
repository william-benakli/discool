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
        setCourse(c.orElse(null));
        if (getCourse() == null) {
            throw new Exception("There is no course with this ID.");
            // TODO : take care of the exception (issue 28)
        }
        currentUser = SecurityUtils.getCurrentUser(personRepository);
        studentAssignmentUpload = getAssignmentController()
                .findStudentAssignmentSubmission(assignment.getId(), currentUser.getId());
        createSidebar(getCourse().getId());
        createMembersBar(getCourse().getId());
        createAssignmentBar();
        createLayout(assignmentBar);
    }

    @Override
    public String getPageTitle() {
        return getCourse().getName();
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
                // and if they are not late
                if ((assignment.getAllowLate() == 1 && assignment.getCutoffdate() >= System.currentTimeMillis())
                    || (assignment.getDuedate() >= System.currentTimeMillis())) {
                    createUploadZone();
                }
            }
        }

        private Div constTab(Div div, String text, ColorHTML color, boolean bold){
            div.getStyle()
                    .set("width","100%")
                    .set("height","50px")
                    .set("background-color", color.getColorHtml())
                    .set("display","flex")
                    .set("padding-left","40px")
                    .set("padding-right","30px");
            Paragraph p = new Paragraph(text);
            if(bold) p.getStyle().set("font-weight","700");
            div.add(p);
            return div;
        }

        private Div tabHorizontal(){
            Div div=new Div();
            Div divLeft=new Div();
            Div divRight=new Div();
            Div statusL=new Div();
            Div statusR=new Div();

            div.getStyle()
                    .set("display","flex")
                    .set("flex-direction","row")
                    .set("overflow","hidden");
            div.setWidth("100%");

            divLeft.getStyle()
                    .set("width","25%");

            divRight.setWidth("74%");
            divRight.getStyle()
                    .set("border-left","solid 1px "+ColorHTML.GREY.getColorHtml());

            statusL.getStyle()
                    .set("min-height","150px");
            statusR.getStyle()
                    .set("min-height","150px");

            if (assignment.getAllowLate() == 1) {
                divLeft.add(constTab(new Div(), "cut off date", ColorHTML.GREYTAB, true));
                divRight.add(constTab(new Div(), Long.toString(assignment.getCutoffdate()), ColorHTML.GREYTAB, false));
            }

            divLeft.add(
                    constTab(new Div(), "due date", ColorHTML.WHITE, true),
                    constTab(new Div(), "max grade", ColorHTML.GREYTAB, true),
                    constTab(statusL, "status", ColorHTML.WHITE, true));
            divRight.add(
                    constTab(new Div(), Long.toString(assignment.getDuedate()), ColorHTML.WHITE, false),
                    constTab(new Div(), Long.toString(assignment.getMaxGrade()), ColorHTML.GREYTAB, false),
                    constTab(statusR, writeGradeInfo(), ColorHTML.WHITE, false));

            div.add(divLeft, divRight);
            return div;
        }

        private void writeInfo() {
            this.add(new Paragraph(assignment.getDescription()));
            add(tabHorizontal());
            //this.add(new Paragraph("max number of attempts : " + assignment.getMaxAttempts()));
        }

        private String writeGradeInfo() {
            String res="";
            if (studentAssignmentUpload != null) {
                if (studentAssignmentUpload.getGrade() == -1) {
                    res+="Your assignment hasn't been graded yet";
                } else {
                    res+="Your grade is : " + studentAssignmentUpload.getGrade();
                    if (studentAssignmentUpload.getTeacherComments() == null) {
                        res+="Your teacher didn't write any comments";
                    } else {
                        res+="Teacher's comments : \n" + studentAssignmentUpload.getTeacherComments();
                    }
                }
            } else {
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
