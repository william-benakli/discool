package app.web.views;

import app.controller.AssignmentController;
import app.controller.Controller;
import app.controller.security.SecurityUtils;
import app.jpa_repo.PersonRepository;
import app.model.chat.PublicTextChannel;
import app.model.courses.Assignment;
import app.model.courses.Course;
import app.model.courses.MoodlePage;
import app.model.users.Person;
import app.web.components.ServerFormComponent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
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
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.vaadin.firitin.fields.LocalDateTimeField;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ViewWithSidebars extends VerticalLayout {

    protected FlexLayout sideBar;
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
        if (membersBar != null) {
            layout.add(sideBar, centerElement, membersBar);
        } else {
            layout.add(sideBar, centerElement);
        }
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
        Image img = new Image((p.isConnected())?"img/dotgreen.svg":"img/dotred.svg", "Statut");
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
    public void createSidebar(long courseId) {
        String s = getUrl();
        String[] s2 = s.split("/");
        String t = s2[s2.length - 1];

        sideBar = new FlexLayout();
        if (!SecurityUtils.isUserStudent()) {
            createSettingsButton();
            createAddButton();
        }
        addMoodleLinksToSidebar(courseId, s2, t);
        addAssignmentsLinksToSidebar(courseId, s2, t);
        addChannelsLinksToSidebar(courseId, s2, t);
        // add the style
        sideBar.addClassName("card");
        sideBar.addClassName("cardLeft");
        setCardStyle(sideBar, "20%", TextChannelView.ColorHTML.DARKGREY);
    }

    @SneakyThrows
    private String getUrl(){
        VaadinServletRequest req = (VaadinServletRequest) VaadinService.getCurrentRequest();
        StringBuffer uriString = req.getRequestURL();
        URI uri = new URI(uriString.toString());
        return uri.toString();
    }

    /**
     * Creates the button & dialog to add a new text channel/assignment/moodle page
     */
    private void createAddButton() {
        Button button = new Button("Ajouter", new Icon(VaadinIcon.PLUS_CIRCLE));
        button.getStyle()
                .set("color", ColorHTML.WHITE.getColorHtml())
                .set("background-color", ColorHTML.PURPLE.getColorHtml());
        button.addClickListener(event -> {
            CustomAddDialog dialog = new CustomAddDialog();
            dialog.setHeight("50%");
            dialog.setWidth("60%");
            dialog.open();
        });
        sideBar.add(button);
    }

    /**
     * Creates the button & dialog to add a new text channel/assignment/moodle page
     */
    private void createSettingsButton() {
        Button button = new Button("Parametres", new Icon(VaadinIcon.COG));
        button.getStyle()
                .set("color", ColorHTML.WHITE.getColorHtml())
                .set("background-color", ColorHTML.PURPLE.getColorHtml());
        button.addClickListener(event -> {
            ServerFormComponent serverFormComponent = new ServerFormComponent(controller, SecurityUtils.getCurrentUser(personRepository), getCourse());
            serverFormComponent.open();
        });
        sideBar.add(button);
    }


    private void addMoodleLinksToSidebar(long courseId, String[] s2, String t) {

        long homePageId = getController().findHomePageId(courseId);
        RouterLink linkHome = new RouterLink("", MoodleView.class, homePageId);
        Button buttonHome = new Button("Page d'accueil");
        buttonHome.addClassName(homePageId + "");
        styleButton(linkHome, buttonHome);
        styleMoodleLink(linkHome);
        if (s2.length >= 4 && s2[3].equals("moodle") && t.equals(homePageId + "")) {
            buttonHome.getStyle().set("color", ColorHTML.PURPLE.getColorHtml());
        } else buttonHome.getStyle().set("color", ColorHTML.TEXTGREY.getColorHtml());
        sideBar.add(linkHome);

        ArrayList<MoodlePage> moodlePages = getController().getAllMoodlePagesForCourse(courseId);
        moodlePages.forEach(page -> {
            if (page.getId() != homePageId) {
                RouterLink link = new RouterLink("", MoodleView.class, page.getId());
                Button button = new Button(page.getTitle());
                button.addClassName(page.getId() + "");
                styleButton(link, button);
                button.addClassName("color" + page.getId());
                if (s2.length >= 4 && s2[3].equals("moodle") && t.equals(page.getId() + "")) {
                    button.getStyle().set("color", ColorHTML.PURPLE.getColorHtml());
                } else button.getStyle().set("color", ColorHTML.TEXTGREY.getColorHtml());
                sideBar.add(link);
            }
        });
    }

    private void styleMoodleLink( RouterLink link) {
        link.getStyle()
                .set("margin-top","20px")
                .set("font-weight","700")
                .set("pointer-event","none")
                .set("color", ColorHTML.TEXTGREY.getColorHtml())
                .set("border-bottom","1px solid rgba(112, 112, 122, .75)");
    }

    /**
     * Add links to all the assignments for this course
     *
     * @param courseId The id of the course
     * @param s2 parameter to check if the user is in a text page
     * @param t have the number of the channel in which the user is
     */
    private void addAssignmentsLinksToSidebar(long courseId, String[] s2, String t) {
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
        if (s2.length>=4 && (s2[3].equals("assignment") || s2[3].equals("teacher_assignment")) && t.equals(assignment.getId()+"")) {
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
     * @param s2 parameter to check if the user is in a text page
     * @param t have the number of the channel in which the user is
     */
    private void addChannelsLinksToSidebar(long courseId, String[] s2, String t) {
        final Person userCurrent = SecurityUtils.getCurrentUser(personRepository);
        ArrayList<PublicTextChannel> publicTextChannels = controller.getAllChannelsForCourse(courseId);
        publicTextChannels.forEach(channel -> {
            if(channel.isPrivateTeacher()){
                if(userCurrent.getRole() == Person.Role.TEACHER ||
                        userCurrent.getRole() == Person.Role.ADMIN){
                    RouterLink link = new RouterLink("", PublicTextChannelView.class, channel.getId());
                    Button button = new Button(channel.getName(), new Icon(VaadinIcon.LOCK));
                    button.addClassName(channel.getId() + "");
                    styleButton(link, button);
                    button.addClassName("color" + channel.getId());
                    if (s2.length >= 4 && s2[3].equals("channels") && t.equals(channel.getId() + "")) {
                        button.getStyle().set("color", ColorHTML.PURPLE.getColorHtml());
                    } else button.getStyle().set("color", ColorHTML.TEXTGREY.getColorHtml());
                    sideBar.add(link);
                }
            }else{
                RouterLink link = new RouterLink("", PublicTextChannelView.class, channel.getId());
                Button button = new Button(channel.getName());
                button.addClassName(channel.getId() + "");
                styleButton(link, button);
                button.addClassName("color" + channel.getId());
                if (s2.length >= 4 && s2[3].equals("channels") && t.equals(channel.getId() + "")) {
                    button.getStyle().set("color", ColorHTML.PURPLE.getColorHtml());
                } else button.getStyle().set("color", ColorHTML.TEXTGREY.getColorHtml());
                sideBar.add(link);
            }
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
                .set("margin-left","3px");
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
            Tab channelTab = new Tab("Créer un salon textuel");
            Div channelDiv = new Div();
            chanelLayout = new FlexLayout();
            styleTab(channelTab, chanelLayout);
            channelDiv.add(chanelLayout);

            Tab assignmentTab = new Tab("Créer un devoir");
            Div assignmentDiv = new Div();
            assignmentDiv.setVisible(false);
            assignmentLayout = new FlexLayout();
            styleTab(assignmentTab, assignmentLayout);
            assignmentDiv.add(assignmentLayout);

            Tab moodleTab = new Tab("Créer une page moodle");
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
            this.close();
        }

        private void createChannelPage() {
            TextField name = new TextField();
            HorizontalLayout layout = new HorizontalLayout();
            Checkbox mute = new Checkbox("Channel écriture reservé aux professeurs");
            Checkbox visible = new Checkbox("Channel reservé aux professeurs");
            name.setLabel("Créer un salon textuel");
            name.setPlaceholder("Nom du salon");
            name.focus();

            Button valider = new Button("Valider");
            valider.addClickListener(event -> {
                boolean priveBoolean = false;
                boolean muteBoolean = false;
                if (visible.getValue()) priveBoolean = true;
                if (mute.getValue()) muteBoolean = true;
                controller.createChannel(name.getValue(), muteBoolean, priveBoolean, getCourse().getId());
                closeUpdate("Salon textuel créé");
                UI.getCurrent().navigate("channels/"+getController().getLastTextChannelRepository(course.getId()).getId());
            });
            layout.add( mute, visible);
            chanelLayout.add(name,layout, valider);
        }

        private void createAssignmentPage() {
            AddAssignmentForm assignmentForm = new AddAssignmentForm(this);
            assignmentLayout.add(assignmentForm);
        }

        private void createMoodlePage() {
            TextField title = new TextField();
            title.setPlaceholder("Titre");
            title.setLabel("Titre");

            Button valider = new Button("Valider");
            valider.addClickListener(event -> {
                controller.createMoodlePage(title.getValue(), getCourse().getId());
                closeUpdate("Page moodle créée");
                UI.getCurrent().navigate("moodle/"+getController().getLastMoodlePage(course.getId()).getId());
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

        public AddAssignmentForm(CustomAddDialog customAddDialog) {
            createTextFields(customAddDialog);
            createDatePickers();
        }

        private void closeUpdate(String text, CustomAddDialog customAddDialog){
            Notification notification = new Notification(text, 9000);
            notification.open();
            customAddDialog.close();
        }

        private void createTextFields(CustomAddDialog customAddDialog) {
            title = new TextField();
            title.setLabel("Titre");
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

            maxGrade = new TextField("Note maximale");
            maxGrade.setRequired(true);
            binder.forField(maxGrade)
                    .withConverter(new StringToIntegerConverter("Veuillez saisir un nombre"))
                    .bind(AssignmentModel::getMaxGrade, AssignmentModel::setMaxGrade);

            VerticalLayout layout = new VerticalLayout();
            layout.add(title, description, maxGrade, createButtons(customAddDialog));
            this.add(layout);
        }

        private void createDatePickers() {
            dueDate = new DateTimePicker("Date d'échéance");
            dueDate.addValueChangeListener(event -> cutoffDate.setValue(dueDate.getValue()));
            cutoffDate = new DateTimePicker("Date limite");
            cutoffDate.setVisible(false);
            setDatePickers(dueDate, cutoffDate);

            allowLate = new Checkbox("Ajouter une date limite de rendu ?", false);
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
                    .withValidator(date -> !date.isBefore(LocalDateTime.now()), "Impossible de créer un évènement dans le passé")
                    .bind(AssignmentModel::getDueDate, AssignmentModel::setDueDate);
            binder.forField(cutoffDate)
                    .withValidator(date -> !date.isBefore(dueDate.getValue()), "Impossible de créer un évènement dans le passé")
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

        private HorizontalLayout createButtons(CustomAddDialog customAddDialog) {
            Label infoLabel = new Label();
            Button save = new Button("Sauvegarder");
            Button reset = new Button("Réinitialiser");

            save.getStyle()
                    .set("color",ColorHTML.WHITE.getColorHtml())
                    .set("background",ColorHTML.PURPLE.getColorHtml());
            reset.getStyle()
                    .set("color",ColorHTML.WHITE.getColorHtml())
                    .set("background",ColorHTML.PURPLE.getColorHtml());

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
                    infoLabel.setText("Sauvegarder");
                    closeUpdate("Devoir créé", customAddDialog);
                    UI.getCurrent().navigate("assignment/"+getAssignmentController().getLastAssignement(course.getId()).getId());
                } else {
                    BinderValidationStatus<AssignmentModel> validate = binder.validate();
                    String errorText = validate.getFieldValidationStatuses()
                            .stream().filter(BindingValidationStatus::isError)
                            .map(BindingValidationStatus::getMessage)
                            .map(Optional::get).distinct()
                            .collect(Collectors.joining(", "));
                    infoLabel.setText("Problème : " + errorText);
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
