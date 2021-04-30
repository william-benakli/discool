package app.web.views;

import app.jpa_repo.CourseRepository;
import app.jpa_repo.PersonRepository;
import app.model.courses.Course;
import app.model.users.Person;
import app.web.components.UserForm;
import app.web.layout.Navbar;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Route(value = "admin", layout = Navbar.class)
@PageTitle("Discool : Admin Panel")
@CssImport("./styles/formStyle.css")

public class PanelAdminView extends VerticalLayout {

    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private UserForm form;
    TextField filterText = new TextField() ;
    TextField filterText2 = new TextField() ;
    TextField filterText3 = new TextField() ;
    private Button addUser = new Button("Add +");
    private final Tab usersTab = new Tab("Users");
    private final Tab coursesTab = new Tab("Courses");
    private final Tabs tabs = new Tabs(usersTab, coursesTab);
    private final Grid<Person> usersGrid = new Grid<>();
    private final Grid<Course> coursesGrid = new Grid<>();

    public PanelAdminView(@Autowired PersonRepository personRepository, @Autowired CourseRepository courseRepository) {
        this.personRepository = personRepository;
        this.courseRepository = courseRepository;
        Div div = new Div();
        div.add(filterText,filterText2,filterText3,addUser);
        div.getStyle().set("display","inline-block");
        addClassName("list-view");
        addUser.addClickListener(buttonClickEvent -> {
            addPerson();
        });
        add(div);
        createUserGrid();
        createCoursesGrid();
        createTabs();
        if(filterText.getValue()==null || filterText.getValue().isEmpty()){
            configureFilter2();
            configureFilter();
            configureFilter3();
        }
        else if(filterText2.getValue()==null || filterText2.getValue().isEmpty()) {
            configureFilter3();
        }
        else{
            configureFilter();
            configureFilter3();
            configureFilter2();
        }
        form.addListener(UserForm.SaveEvent.class,this::savePerson);
        form.addListener(UserForm.DeleteEvent.class,this::deletePerson);
        form.addListener(UserForm.CloseEvent.class,e ->closeEditor());
        updateList();
        closeEditor();

    }
    public void deletePerson(UserForm.DeleteEvent evt){
        personRepository.delete(evt.getPerson());
        updateList();
        closeEditor();
    }

    public void savePerson(UserForm.SaveEvent evt){
        personRepository.save(evt.getPerson());
        updateList();
        closeEditor();
    }

    public void addPerson(){
        usersGrid.asSingleSelect().clear();
        editPerson(new Person());
    }

    public void updateList() {

        if((filterText2.getValue()==null || filterText2.getValue().isEmpty()) && (filterText.getValue()==null || filterText.getValue().isEmpty())){
            usersGrid.setItems(findAll(filterText3.getValue()));
        }
        else if((filterText2.getValue()==null || filterText2.getValue().isEmpty()) && (filterText3.getValue()==null || filterText3.getValue().isEmpty())) {
            usersGrid.setItems(findAll(filterText.getValue()));
        }else{
            usersGrid.setItems(findAll(filterText2.getValue()));
        }
    }

    public List<Person>findAll(String stringFilter){
        if((filterText2.getValue()==null || filterText2.getValue().isEmpty()) && (filterText.getValue()==null || filterText.getValue().isEmpty())&& (filterText3.getValue()==null || filterText3.getValue().isEmpty())){
            return personRepository.findAll();
        }
        else if ((filterText.getValue()==null || filterText.getValue().isEmpty()) && (filterText3.getValue()==null || filterText3.getValue().isEmpty())){
            return personRepository.searchByEmail(stringFilter);
        }
        else if ((filterText2.getValue()==null || filterText2.getValue().isEmpty())&& (filterText.getValue()==null || filterText.getValue().isEmpty())){
            return personRepository.searchByUserName(stringFilter);
        }
        else{
            return personRepository.search(stringFilter);
        }

    }

    public void configureFilter() {
        filterText.setPlaceholder("filtrer par nom...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());
    }
    public void configureFilter2(){
        filterText2.setPlaceholder("filtrer par mail...");
        filterText2.setClearButtonVisible(true);
        filterText2.setValueChangeMode(ValueChangeMode.LAZY);
        filterText2.addValueChangeListener(e ->updateList());
    }

    public void configureFilter3(){
        filterText3.setPlaceholder("filtrer par Username...");
        filterText3.setClearButtonVisible(true);
        filterText3.setValueChangeMode(ValueChangeMode.LAZY);
        filterText3.addValueChangeListener(e ->updateList());
    }
    private void closeEditor() {
        form.setPerson(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void createUserGrid() {

        usersGrid.setItems(personRepository.findAll());
        usersGrid.addColumn(Person::getUsername).setHeader("Pseudo");
        usersGrid.addColumn(Person::getLastName).setHeader("Last Name");
        usersGrid.addColumn(Person::getFirstName).setHeader("First Name");
        usersGrid.addColumn(Person::getEmail).setHeader("Email");
        usersGrid.addColumn(Person::getDescription).setHeader("Description");
        usersGrid.addColumn(Person::getRole).setHeader("Role");
        usersGrid.addColumn(Person::getWebsite).setHeader("Website");
        usersGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                              GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        usersGrid.getStyle().set("flex","2");
        usersGrid.getColumns().forEach(col ->col.setAutoWidth(true));
        usersGrid.asSingleSelect().addValueChangeListener(event -> editPerson(event.getValue()));
        usersTab.add(usersGrid);
    }

    private void editPerson(Person person) {
        if(person==null){
            closeEditor();
        }
        else{
            form.setPerson(person);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void createCoursesGrid() {
        coursesGrid.setItems(courseRepository.findAll());
        coursesGrid.addColumn(Course::getName).setHeader("Name");
        coursesGrid.addColumn(Course::getTeacherId).setHeader("TeacherID");
        coursesGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                                     GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        coursesTab.add(coursesGrid);
    }

    private void createTabs() {

        form = new UserForm(personRepository);
        form.getStyle().set("flex","1");
        form.getStyle().set("display","list-item");
        Div content = new Div(usersGrid, form);
        Div content2 = new Div(coursesGrid);
        content.setSizeFull();
        content2.setSizeFull();
        content2.setVisible(true);
        content.addClassName("content");
        usersTab.getStyle().set("flex-direction","column");
        tabs.add(usersTab,coursesTab);
        Map<Tab, Div > tabsToPages = new HashMap<>();
        tabsToPages.put(usersTab, content);
        tabsToPages.put(coursesTab, content2);
        tabsToPages.values().forEach(page -> page.setVisible(false));
        Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
        selectedPage.setVisible(true);
        tabs.addSelectedChangeListener(event -> {
            tabsToPages.values().forEach(page -> page.setVisible(false));
            Component insideSelectedPage = tabsToPages.get(tabs.getSelectedTab());
            insideSelectedPage.setVisible(true);
        });

        add(tabs, content, content2);
    }
}

