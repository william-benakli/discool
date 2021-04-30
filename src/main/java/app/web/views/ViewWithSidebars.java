package app.web.views;

import app.controller.AssignmentController;
import app.controller.Controller;
import app.controller.security.SecurityUtils;
import app.jpa_repo.PersonRepository;
import app.model.chat.TextChannel;
import app.model.courses.Assignment;
import app.model.courses.Course;
import app.model.courses.MoodlePage;
import app.model.users.Person;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.server.Page;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.vaadin.firitin.fields.LocalDateTimeField;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public abstract class ViewWithSidebars extends VerticalLayout {

    private FlexLayout sideBar;
    private FlexLayout membersBar;
    @Getter @Setter
    private Controller controller;
    @Getter @Setter
    private AssignmentController assignmentController;
    @Getter @Setter
    private PersonRepository personRepository;
    @Getter @Setter
    private Course course;

    public void createLayout(FlexLayout centerElement) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.getStyle()
                .set("position", "absolute")
                .set("top", "75px")
                .set("bottom", "0")
                .set("margin", "0")
                .set("padding", "0");
        layout.add(sideBar, centerElement, membersBar);
        this.add(layout);
    }

    /**
     *Take a user as a parameter and display his information
     *
     * @param p The user and his information
     * @return a card containing a user and his connection status
     */
    public FlexLayout styleStatusUsers(Person p){
        FlexLayout divUser = new FlexLayout();
        FlexLayout div = new FlexLayout();
        div.getStyle()
                .set("display","flex")
                .set("flex-direction","column")
                .set("margin","5px 0");
        Paragraph pseudo = new Paragraph(p.getUsername());
        pseudo.getStyle()
                .set("color",ColorHTML.PURPLE.getColorHtml())
                .set("font-weight","700")
                .set("margin","0")
                .set("margin-top","5px");
        FlexLayout divstatus = new FlexLayout();
        Paragraph status = new Paragraph((p.isConnected())?"En ligne":"Hors-ligne");
        status.getStyle()
                .set("color", ColorHTML.TEXTGREY.getColorHtml())
                .set("margin","0");
        Image img = new Image((p.isConnected())?"img/dotgreen.svg":"img/dotred.svg", "Status");
        img.getStyle()
                .set("margin-right","5px")
                .set("margin-top","-2.5px");
        divstatus.add(img);
        divstatus.add(status);
        div.add(pseudo);
        div.add(divstatus);
        Image iconUser = p.getProfilePicture();
        iconUser.getStyle()
                .set("width","50px")
                .set("height","50px")
                .set("margin","10px")
                .set("border-radius","25px");

        divUser.add(iconUser);
        divUser.add(div);
        return divUser;
    }

    /**
     * Adding members to the right navigation bar
     *
     * @param courseId id of the course concerned
     */
    public void createMembersBar(long courseId) {
        membersBar = new FlexLayout();
        ArrayList<Person> usersList = controller.getAllUsersForCourse(courseId);
        for (Person p : usersList) if (p.isConnected()) membersBar.add(styleStatusUsers(p));
        for (Person p : usersList) if (!p.isConnected()) membersBar.add(styleStatusUsers(p));
        membersBar.addClassName("card");
        membersBar.addClassName("cardRight");
        setCardStyle(membersBar, "20%", ColorHTML.DARKGREY);
    }

    /**
     * Sets general style parameters for the 3 big panels (left, right and center cards)
     *
     * @param card  The card to modify
     * @param width The width of the card
     * @param color The color of the background
     */
    public void setCardStyle(FlexLayout card, String width, TextChannelView.ColorHTML color) {
        card.getStyle()
                .set("overflow", "auto")
                .set("width", width)
                .set("margin", "0px")
                .set("background", color.getColorHtml())
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("padding", "10px");
    }

    /**
     * Creates the sidebar for Moodle pages and TextChannels.
     * It contains
     *
     * @param courseId The id of the course
     */
    @SneakyThrows
    public void createSidebar(long courseId) {
        VaadinServletRequest req = (VaadinServletRequest) VaadinService.getCurrentRequest();
        StringBuffer uriString = req.getRequestURL();
        URI uri = new URI(uriString.toString()); //TODO: Del
        String s=uri.toString();
        String t=s.substring(s.length()-1);//TODO: edit with the correct redirect values
        String[] s2=s.split("/");
        // TODO : clean up the s2 and t Strings (what do they even do ???) +
        //  comment the doc of addChannels() and addAssignments()

        sideBar = new FlexLayout();
        if (! SecurityUtils.isUserStudent()) {
            createAddButton();
        }
        addMoodleLinksToSidebar(courseId);
        addAssignmentsLinksToSidebar(courseId, s2, t);
        addChannelsLinksToSidebar(courseId, s2, t);
        // add the style
        sideBar.addClassName("card");
        sideBar.addClassName("cardLeft");
        setCardStyle(sideBar, "20%", TextChannelView.ColorHTML.DARKGREY);
    }

    /**
     * Creates the button & dialog to add a new text channel/assignment/moodle page
     */
    private void createAddButton() {
        Button button = new Button("Add", new Icon(VaadinIcon.PLUS_CIRCLE));
        button.addClickListener(event -> {
            CustomAddDialog dialog = new CustomAddDialog();
            dialog.setHeight("50%");
            dialog.setWidth("60%");
            dialog.open();
        });
        sideBar.add(button);
    }

    private void addMoodleLinksToSidebar(long courseId) {
        long homePageId = getController().findHomePageId(courseId);
        RouterLink linkHome = new RouterLink("Page d'accueil", MoodleView.class, homePageId);
        styleMoodleLink(linkHome, true);
        sideBar.add(linkHome);

        ArrayList<MoodlePage> moodlePages = getController().getAllMoodlePagesForCourse(courseId);
        moodlePages.forEach(page -> {
            if (page.getId() != homePageId) {
                RouterLink link = new RouterLink(page.getTitle(), MoodleView.class, page.getId());
                styleMoodleLink(link, false);
                sideBar.add(link);
            }
        });
    }

    private void styleMoodleLink(RouterLink link, boolean isHomePage) {
        link.getStyle()
                .set("color",ColorHTML.TEXTGREY.getColorHtml())
                .set("padding-left","25px")
                .set("margin","20px 10px 10px -8px")
                .set("font-weight","700")
                .set("pointer-event","none");
        if (isHomePage) {
            link.getStyle().set("border-bottom","1px solid rgba(112, 112, 122, .75)");
        }
    }

    /**
     * Add links to all the assignments for this course
     *
     * @param courseId The id of the course
     * @param s2
     * @param t
     */
    private void addAssignmentsLinksToSidebar(long courseId, String s2[], String t) {
        ArrayList<Assignment> assignments = assignmentController.getAssignmentsForCourse(courseId);
        assignments.forEach(assignment -> {
            RouterLink studentLink = new RouterLink("", StudentAssignmentView.class, assignment.getId());
            styleNavButtonsForAssignments(assignment, s2, t, studentLink, false);
            if (! SecurityUtils.isUserStudent()) {
                RouterLink teacherLink = new RouterLink("", TeacherAssignmentView.class, assignment.getId());
                styleNavButtonsForAssignments(assignment, s2, t, teacherLink, true);
            }
        });
    }

    private void styleNavButtonsForAssignments(Assignment assignment, String[] s2, String t,
                                               RouterLink link, boolean isTeacherPage) {
        Button button = new Button(assignment.getName());
        button.addClassName(assignment.getId() + "");
        if (isTeacherPage) {
            button.setIcon(new Icon(VaadinIcon.ACADEMY_CAP));
        }
        styleButton(link, button);
        button.addClassName("color" + assignment.getId());
        if (s2.length>=4 && s2[3].equals("assignment") && t.equals(assignment.getId()+"")) {
            button.getStyle().set("color",ColorHTML.PURPLE.getColorHtml());
        } else {
            button.getStyle().set("color",ColorHTML.TEXTGREY.getColorHtml());
        }
        sideBar.add(link);
    }

    /**
     * Add links to all the text channels of the course
     *
     * @param courseId The id of the course
     * @param s2
     * @param t
     */
    private void addChannelsLinksToSidebar(long courseId, String s2[], String t) {
        ArrayList<TextChannel> textChannels = controller.getAllChannelsForCourse(courseId);
        textChannels.forEach(channel -> {
            RouterLink link = new RouterLink("", TextChannelView.class, channel.getId());
            Button button = new Button(channel.getName());
            button.addClassName(channel.getId() + "");
            styleButton(link, button);
            button.addClassName("color" + channel.getId());
            if (s2.length >= 4 && s2[3].equals("channels") && t.equals(channel.getId() + "")) {
                button.getStyle().set("color", ColorHTML.PURPLE.getColorHtml());
            } else button.getStyle().set("color", ColorHTML.TEXTGREY.getColorHtml());
            sideBar.add(link);
        });
    }

    /**
     * Adds style to the sidebar buttons (links to assignments and text channels)
     * @param link      The RouterLink to the assignment/channel
     * @param button    The Button to style
     */
    private void styleButton(RouterLink link, Button button) {
        link.getElement().appendChild(button.getElement());
        link.getStyle()
                .set("pointer-event","none")
                .set("padding-bottom","2.5px")
                .set("background","none")
                .set("margin-left","30px");
        button.getStyle()
                .set("font-weight","700")
                .set("background","none")
                .set("cursor","pointer");
    }

    public enum ColorHTML {
        PURPLE("#7510F7"),
        WHITE("#FFFFFF"),
        GREY("#EAEAEA"),
        DARKGREY("#DEDEDE"),
        TEXTGREY("#707070"),
        DARK("#808080"),
        DANGER("#F04747"),
        GREYTAB("#f4f5f8"),
        GREEN("#cfefcf"),
        ORANGE("#F4A460");

        @Getter
        private final String colorHtml;

        ColorHTML(String s) {
            this.colorHtml = s;
        }
    }

    private class CustomAddDialog extends Dialog {

        private final Div bottomLayout = new Div();
        private FlexLayout moodleLayout;
        private FlexLayout assignmentLayout;
        private FlexLayout chanelLayout;

        public CustomAddDialog() {
            addCloseListeners();
            addTabs();
            createChannelPage();
            createAssignmentPage();
            createMoodlePage();
        }

        /**
         * The dialog closes when the "Close" button is clicked, on Esc, or on any outside clicks
         */
        private void addCloseListeners() {
            setCloseOnEsc(true);
            setCloseOnOutsideClick(true);
            Button closeButton = new Button("Close");
            closeButton.addClickListener(event -> close());
            bottomLayout.add(closeButton);
        }

        private void addTabs() {
            Tab channelTab = new Tab("Add a text channel");
            Div channelDiv = new Div();
            chanelLayout = new FlexLayout();
            styleTab(channelTab, chanelLayout);
            channelDiv.add(chanelLayout);

            Tab assignmentTab = new Tab("Add an assignment");
            Div assignmentDiv = new Div();
            assignmentDiv.setVisible(false);
            assignmentLayout = new FlexLayout();
            styleTab(assignmentTab, assignmentLayout);
            assignmentDiv.add(assignmentLayout);

            Tab moodleTab = new Tab("Add a Moodle page");
            Div moodleDiv = new Div();
            moodleDiv.setVisible(false);
            moodleLayout = new FlexLayout();
            styleTab(moodleTab, moodleLayout);
            moodleDiv.add(moodleLayout);

            /*navigation between tabs*/
            Map<Tab, Component> tabsToPages = new HashMap<>();
            tabsToPages.put(channelTab, channelDiv);
            tabsToPages.put(assignmentTab, assignmentDiv);
            tabsToPages.put(moodleTab, moodleDiv);
            Tabs tabs = new Tabs(channelTab, assignmentTab, moodleTab);
            Div pages = new Div(channelDiv, assignmentDiv, moodleDiv);

            tabs.addSelectedChangeListener(event -> {
                tabsToPages.values().forEach(page -> page.setVisible(false));
                Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
                selectedPage.setVisible(true);
            });
            this.add(tabs, pages);
        }

        /**
         * Change the style of tabs
         *
         * @param tab1 Tab to apply the CSS properties to
         * @param div  Div to apply the CSS properties to
         */
        private void styleTab(Tab tab1, FlexLayout div) {
            tab1.getStyle().set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml());
            div.getStyle()
                    .set("margin","auto")
                    .set("min-height","100px")
                    .set("width","75%")
                    .set("flex-direction","column");
        }

        private void closeUpdate(String text){
            Notification notification = new Notification(text, 9000);
            notification.open();
            UI.getCurrent().getPage().reload();
        }

        private void createChannelPage() {
            TextField name = new TextField();
            name.setLabel("Create a new text channel");
            name.setPlaceholder("Channel name");
            name.focus();

            Button valider = new Button("Valider");
            valider.addClickListener(event -> {
                controller.createChannel(name.getValue(), getCourse().getId());
                closeUpdate("text chat created");
            });

            chanelLayout.add(name, valider);
        }

        private void createAssignmentPage() {
            AddAssignmentForm assignmentForm = new AddAssignmentForm();
            assignmentLayout.add(assignmentForm);
        }

        private void createMoodlePage() {
            TextField title = new TextField();
            title.setPlaceholder("Title");
            title.setLabel("Title");

            Button valider = new Button("Valider");
            valider.addClickListener(event -> {
                controller.createMoodlePage(title.getValue(), getCourse().getId());
                closeUpdate("moodle page created");
            });

            moodleLayout.add(title, valider);
        }

    }

    private class AddAssignmentForm extends FormLayout {
        Binder<AssignmentModel> binder = new Binder<>();
        TextField title;
        TextField description;
        TextField maxGrade;
        DateTimePicker dueDate;
        DateTimePicker cutoffDate;
        Checkbox allowLate;

        public AddAssignmentForm() {
            createTextFields();
            createDatePickers();
        }

        private void closeUpdate(String text){
            Notification notification = new Notification(text, 9000);
            notification.open();
            UI.getCurrent().getPage().reload();
        }

        private void createTextFields() {
            title = new TextField();
            title.setLabel("Title");
            title.setRequired(true);
            title.setValueChangeMode(ValueChangeMode.EAGER);
            binder.forField(title)
                    .bind(AssignmentModel::getDescription, AssignmentModel::setDescription);

            description = new TextField();
            description.setLabel("Description");
            description.setRequired(true);
            description.setValueChangeMode(ValueChangeMode.EAGER);
            binder.forField(description)
                    .bind(AssignmentModel::getDescription, AssignmentModel::setDescription);

            maxGrade = new TextField("Max grade");
            maxGrade.setRequired(true);
            binder.forField(maxGrade)
                    .withConverter(new StringToIntegerConverter("Must be a number"))
                    .bind(AssignmentModel::getMaxGrade, AssignmentModel::setMaxGrade);

            VerticalLayout layout = new VerticalLayout();
            layout.add(title, description, maxGrade, createButtons());
            this.add(layout);
        }

        private void createDatePickers() {
            dueDate = new DateTimePicker("Due date");
            dueDate.addValueChangeListener(event -> cutoffDate.setValue(dueDate.getValue()));
            cutoffDate = new DateTimePicker("Cut-off date");
            cutoffDate.setVisible(false);
            setDatePickers(dueDate, cutoffDate);

            allowLate = new Checkbox("Allow late ?", false);
            allowLate.setRequiredIndicatorVisible(true);
            allowLate.addClickListener(event -> {
                // date picker only visible if late work is accepted
                cutoffDate.setVisible(allowLate.getValue());
            });

            VerticalLayout datePickers = new VerticalLayout();
            datePickers.add(dueDate);
            datePickers.add(allowLate);
            datePickers.add(cutoffDate);

            LocalDateTimeField now = new LocalDateTimeField();
            now.setValue(LocalDateTime.now());
            binder.forField(dueDate)
                    .withValidator(date -> !date.isBefore(LocalDateTime.now()), "Due date can't be in the past")
                    .bind(AssignmentModel::getDueDate, AssignmentModel::setDueDate);
            binder.forField(cutoffDate)
                    .withValidator(date -> !date.isBefore(dueDate.getValue()), "Can't be before the due date")
                    .bind(AssignmentModel::getCutoffDate, AssignmentModel::setCutoffDate);
            binder.forField(allowLate).bind(AssignmentModel::isAllowLate, AssignmentModel::setAllowLate);

            this.add(datePickers);
        }

        private void setDatePickers(DateTimePicker... datePickers) {
            for (DateTimePicker datePicker : datePickers) {
                LocalDateTime now = LocalDateTime.now();
                datePicker.setStep(Duration.ofMinutes(15));
                datePicker.setMin(now);
            }
        }

        private HorizontalLayout createButtons() {
            Label infoLabel = new Label();
            NativeButton save = new NativeButton("Save");
            NativeButton reset = new NativeButton("Reset");

            reset.addClickListener(event -> {
                binder.readBean(null);
                infoLabel.setText("");
            });

            save.addClickListener(event -> {
                if (binder.isValid()) {
                    getAssignmentController().createAssignment(title.getValue(), description.getValue(),
                                                               getCourse().getId(), maxGrade.getValue(),
                                                               allowLate.getValue(),
                                                               dueDate.getValue(), cutoffDate.getValue());
                    infoLabel.setText("Saved");
                    closeUpdate("assignement created");
                } else {
                    BinderValidationStatus<AssignmentModel> validate = binder.validate();
                    String errorText = validate.getFieldValidationStatuses()
                            .stream().filter(BindingValidationStatus::isError)
                            .map(BindingValidationStatus::getMessage)
                            .map(Optional::get).distinct()
                            .collect(Collectors.joining(", "));
                    infoLabel.setText("Errors : " + errorText);
                }
            });

            HorizontalLayout actions = new HorizontalLayout();
            actions.add(save, reset, infoLabel);
            return actions;
        }
    }

    @Getter @Setter
    public static class AssignmentModel {
        private String title;
        private String description;
        private boolean allowLate;
        private LocalDateTime dueDate;
        private LocalDateTime cutoffDate;
        private int maxGrade;
    }

}
