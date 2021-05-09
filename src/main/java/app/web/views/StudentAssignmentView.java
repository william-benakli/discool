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

import java.util.HashMap;
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
                                 @Autowired PublicTextChannelRepository publicTextChannelRepository,
                                 @Autowired AssignmentRepository assignmentRepository,
                                 @Autowired PersonRepository personRepository,
                                 @Autowired StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository,
                                 @Autowired GroupRepository groupRepository,
                                 @Autowired GroupMembersRepository groupMembersRepository,
                                 @Autowired MoodlePageRepository moodlePageRepository,
                                 @Autowired PrivateChatMessageRepository privateChatMessageRepository) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.personRepository = personRepository;
        setPersonRepository(personRepository);
        setController(new Controller(personRepository, publicTextChannelRepository, null,
                                     courseRepository, moodlePageRepository, groupRepository, groupMembersRepository,
                                     privateChatMessageRepository));
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
                if ((assignment.getAllowLate() == 1 && assignment.getCutoffdate()*1000-3600000*2 >= System.currentTimeMillis())
                    || (assignment.getDuedate()*1000-3600000*2 >= System.currentTimeMillis())) {
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

        private void styleDivAssignment(Div div, Div divLeft, Div divRight, Div statusL, Div statusR){
            div.getStyle()
                    .set("display","flex")
                    .set("flex-direction","row")
                    .set("overflow","hidden");
            div.setWidth("100%");

            divLeft.getStyle()
                    .set("width","25%");

            divRight.setWidth("74%");
            divRight.getStyle()
                    .set("border-left","solid 1px " + ColorHTML.GREY.getColorHtml());

            statusL.getStyle()
                    .set("min-height","150px");
            statusR.getStyle()
                    .set("min-height","150px");
        }

        private void writeInfo() {
            Div div=new Div();
            Div divLeft=new Div();
            Div divRight=new Div();
            Div statusL=new Div();
            Div statusR=new Div();

            styleDivAssignment(div, divLeft, divRight, statusL, statusR);
            if (assignment.getAllowLate() == 1) {
                divLeft.add(constTab(new Div(), "Date limite", ColorHTML.GREYTAB, true));
                divRight.add(constTab(new Div(), getController().convertLongToDate(assignment.getCutoffdate()*1000-3600000*2), ColorHTML.GREYTAB, false));
            }

            divLeft.add(
                    constTab(new Div(), "Date d'échéance", ColorHTML.WHITE, true),
                    constTab(new Div(), "Note maximale", ColorHTML.GREYTAB, true),
                    constTab(statusL, "Statut", ColorHTML.WHITE, true));
            divRight.add(
                    constTab(new Div(), getController().convertLongToDate(assignment.getDuedate()*1000-3600000*2), ColorHTML.WHITE, false),
                    constTab(new Div(), Long.toString(assignment.getMaxGrade()), ColorHTML.GREYTAB, false),
                    constTab(statusR, writeGradeInfo(), changeColor(), false));

            if(SecurityUtils.isUserStudent()) {
                HashMap<String, Integer> gradeAllUser = getAssignmentController().showGrade(assignment, currentUser);
                if (gradeAllUser != null && gradeAllUser.get("user") != null) {
                    gradeAllUser.forEach((key, value) -> {
                        divLeft.add(constTab(new Div(), key, ColorHTML.GREYTAB, true));
                        divRight.add(constTab(new Div(), Integer.toString(value), ColorHTML.GREYTAB, false));
                    });
                }
            }

            div.add(divLeft, divRight);
            add(div);
        }

        private ColorHTML changeColor(){
            ColorHTML res=ColorHTML.WHITE;
            if (studentAssignmentUpload != null && assignment.getDuedate()*1000-3600000*2 > System.currentTimeMillis()){
                res= ColorHTML.GREEN;
            }else{
                if (assignment.getAllowLate() == 1 && assignment.getDuedate()*1000-3600000*2 < System.currentTimeMillis() && assignment.getCutoffdate()*1000-3600000*2 > System.currentTimeMillis()){
                    res=ColorHTML.ORANGE;
                }else if((assignment.getCutoffdate()*1000-3600000*2 < System.currentTimeMillis() && assignment.getAllowLate() == 1) || (assignment.getAllowLate() == 0 && assignment.getDuedate()*1000-3600000*2<System.currentTimeMillis())){
                    res=ColorHTML.DANGER;
                }
            }
            return res;
        }

        private String writeGradeInfo() {
            String res="";
            if (studentAssignmentUpload != null) {
                if (studentAssignmentUpload.getGrade() == -1) {
                    res+=" Votre devoir n'a pas encore été noté. ";
                } else {
                    res+=" Votre note est : " + studentAssignmentUpload.getGrade();
                    if (studentAssignmentUpload.getTeacherComments() == null) {
                        res+=" Votre professeur n'a écrit aucun commentaire. ";
                    } else {
                        res+=" Commentaires de l'enseignant : \n" + studentAssignmentUpload.getTeacherComments();
                    }
                }
            } else {
                res+=" Vous n'avez pas encore soumis de document! ";
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
                Notification.show("Vous avez téléchargé votre fichier avec succès!");
            });

            uploadComponent.addFileRejectedListener(event -> {
                Notification.show("Impossible de télécharger le fichier");
                Notification.show(event.getErrorMessage());
            });

            uploadComponent.getStyle()
                    .set("margin","auto");

            this.add(uploadComponent);
        }
    }
}
