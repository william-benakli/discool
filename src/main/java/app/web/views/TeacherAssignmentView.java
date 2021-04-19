package app.web.views;

import app.controller.AssignmentController;
import app.controller.Controller;
import app.controller.DownloadController;
import app.jpa_repo.*;
import app.model.courses.Assignment;
import app.model.courses.Course;
import app.model.courses.StudentAssignmentUpload;
import app.model.users.Person;
import app.web.layout.Navbar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Route(value = "teacher_assignment", layout = Navbar.class)
public class TeacherAssignmentView extends ViewWithSidebars implements HasDynamicTitle, HasUrlParameter<Long> {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private Course course;
    private Assignment assignment;

    private final FlexLayout assignmentBar = new FlexLayout();

    public TeacherAssignmentView(@Autowired CourseRepository courseRepository,
                                 @Autowired TextChannelRepository textChannelRepository,
                                 @Autowired AssignmentRepository assignmentRepository,
                                 @Autowired PersonRepository personRepository,
                                 @Autowired StudentAssignmentsUploadsRepository studentAssignmentsUploadsRepository,
                                 @Autowired GroupRepository groupRepository,
                                 @Autowired GroupMembersRepository groupMembersRepository) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
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
        createSidebar(course.getId());
        createMembersBar(course.getId());
        createAdminPanelBar();
        createLayout(assignmentBar);
    }

    @Override
    public String getPageTitle() {
        return course.getName();
    }

    private void createAdminPanelBar() {
        assignmentBar.removeAll();
        setCardStyle(assignmentBar, "60%", ColorHTML.GREY);
        H1 title = new H1(assignment.getName());
        assignmentBar.add(title);
        TeacherLayout layout = new TeacherLayout();
        layout.createGrid();
        assignmentBar.add(layout);
    }

    private class TeacherLayout extends VerticalLayout {
        private Grid<RowModel> grid;

        private void createGrid() {
            grid = new Grid<>();
            assignValues();
            createColumnsAndEditor();
            grid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                                  GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
            this.add(grid);
        }

        private void assignValues() {
            ArrayList<Person> students = getController().getAllStudentsForCourse(course.getId());
            ArrayList<RowModel> values = new ArrayList<>();
            students.forEach(student -> {
                RowModel u = new RowModel(student.getId(), student.getLastName() + " " + student.getFirstName(),
                                          getAssignmentController().findStudentAssignmentSubmission(assignment.getId(), student.getId()));
                values.add(u);
            });
            grid.setItems(values);
        }

        /**
         * Creates the columns and a way to edit the values inside
         */
        private void createColumnsAndEditor() {
            grid.addColumn(RowModel::getName).setHeader("Name");
            Grid.Column<RowModel> gradeColumn = grid.addColumn(RowModel::getGrade).setHeader("Grade");
            Grid.Column<RowModel> commentsColumn = grid.addColumn(RowModel::getComments).setHeader("Comments").setAutoWidth(true);
            grid.addComponentColumn(RowModel::getDownloadButton).setHeader(".tar.gz");
            TextField gradeField = new TextField();
            TextField commentsField = new TextField();
            Editor<RowModel> editor = grid.getEditor();
            Div validationStatus = new Div();
            validationStatus.setId("validation");

            createBinder(editor, gradeColumn, commentsColumn, gradeField, commentsField, validationStatus);
            createEditor(editor, gradeField);
            add(validationStatus, grid);
        }

        private void createBinder(Editor<RowModel> editor, Grid.Column<RowModel> gradeColumn,
                                  Grid.Column<RowModel> commentsColumn, TextField gradeField,
                                  TextField commentsField, Div validationStatus) {
            Binder<RowModel> binder = new Binder<>(RowModel.class);
            editor.setBinder(binder);
            editor.setBuffered(true);

            binder.forField(gradeField)
                    .withConverter(
                            new StringToIntegerConverter("Grade must be a number."))
                    .withStatusLabel(validationStatus).bind("grade");
            gradeColumn.setEditorComponent(gradeField);

            binder.forField(commentsField)
                    .withStatusLabel(validationStatus).bind("comments");
            commentsColumn.setEditorComponent(commentsField);
        }

        private void createEditor(Editor<RowModel> editor, TextField gradeField) {
            Collection<Button> editButtons = Collections.newSetFromMap(new WeakHashMap<>());
            Grid.Column<RowModel> editorColumn = grid.addComponentColumn(row -> {
                Button edit = new Button("Edit");
                edit.addClassName("edit");
                edit.addClickListener(e -> {
                    editor.editItem(row);
                    gradeField.focus();
                });
                edit.setEnabled(!editor.isOpen());
                editButtons.add(edit);
                return edit;
            });

            editor.addOpenListener(e -> editButtons.stream()
                    .forEach(button -> button.setEnabled(!editor.isOpen())));
            editor.addCloseListener(e -> editButtons.stream()
                    .forEach(button -> button.setEnabled(!editor.isOpen())));

            Button save = new Button("Save", e -> editor.save());
            save.addClassName("save");

            Button cancel = new Button("Cancel", e -> editor.cancel());
            cancel.addClassName("cancel");

            // add a key listener for "escape" to cancel the editing
            grid.getElement().addEventListener("keyup", event -> editor.cancel())
                    .setFilter("event.key === 'Escape' || event.key === 'Esc'");

            Div buttons = new Div(save, cancel);
            editorColumn.setEditorComponent(buttons);

            editor.addSaveListener(event -> {
                RowModel model = event.getItem();
                if (model.getUpload() != null) {
                    getAssignmentController().saveGrading(model);
                } else {
                    System.out.println("ok");
                    getAssignmentController().saveGrading(model.getStudentId(), assignment.getId(), assignment.getCourseId(),
                                                          model.getGrade(), model.getComments());
                }
            });
        }
    }


    @Getter
    @Setter
    public class RowModel {
        private String name;
        private StudentAssignmentUpload upload;
        private String comments;
        private int grade;
        private long studentId;
        private DownloadController downloadButton;

        public RowModel(long studentId, String name, StudentAssignmentUpload upload) {
            this.name = name;
            this.upload = upload;

            if (upload != null) {
                this.comments = upload.getTeacherComments();
                this.grade = upload.getGrade();
                this.studentId = upload.getStudentId();
                createDownloadButton();
            } else {
                this.comments = "";
                this.grade = 0;
                this.studentId = studentId;
                downloadButton = new DownloadController();
            }
        }

        private void createDownloadButton() {
            String outputName = "downloads/" + name + "_" + studentId + ".tar.gz";
            String sourceDir = "uploads/assignments/" + assignment.getId() + "_" + studentId;

            downloadButton = new DownloadController("Download", outputName,
            outputStream -> {
                downloadButton.createTarGZ(sourceDir, outputName);
                try {
                    File file = new File(outputName);
                    byte[] toWrite = FileUtils.readFileToByteArray(file);
                    outputStream.write(toWrite);
                } catch (IOException e) {
                    System.out.println("Problem while writing the file");
                    e.printStackTrace();
                }
            });
        }
    }

}
