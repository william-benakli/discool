package app.web.views;

import app.controller.Controller;
import app.model.chat.TextChannel;
import app.model.users.Person;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.URI;
import java.util.ArrayList;

public abstract class ViewWithSidebars extends VerticalLayout {
    @Getter
    @Setter
    private Controller controller;
    private FlexLayout sideBar;
    private FlexLayout membersBar;

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
        Paragraph status = new Paragraph((p.isConected())?"En ligne":"Hors-ligne");
        status.getStyle()
                .set("color", ColorHTML.TEXTGREY.getColorHtml())
                .set("margin","0");
        Image img=new Image((p.isConected())?"img/dotgreen.svg":"img/dotred.svg","Status");
        img.getStyle()
                .set("margin-right","5px")
                .set("margin-top","-2.5px");
        divstatus.add(img);
        divstatus.add(status);
        div.add(pseudo);
        div.add(divstatus);
        //TODO: support the management of the icon in the database
        Image iconUser = new Image("img/books.jpg","Status");
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
        ArrayList<Person> usersList = controller.getAllUser();
        for (Person p : usersList) if (p.isConected()) membersBar.add(styleStatusUsers(p));
        for (Person p : usersList) if (!p.isConected()) membersBar.add(styleStatusUsers(p));
        membersBar.addClassName("card");
        membersBar.addClassName("cardRight");
        setCardStyle(membersBar, "20%", ColorHTML.DARKGREY);
        // TODO add the members for the chat/course
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

        sideBar = new FlexLayout();
        // add the RouterLinks
        RouterLink linkHome=new RouterLink("Page d'accueil", MoodleView.class, courseId);
        linkHome.getStyle()
                .set("border-bottom","1px solid rgba(112, 112, 122, .75)")
                .set("color",ColorHTML.TEXTGREY.getColorHtml())
                .set("padding-left","25px")
                .set("margin","20px 10px 10px -8px")
                .set("font-weight","700")
                .set("pointer-event","none");
        sideBar.add(linkHome);
        ArrayList<TextChannel> textChannels = controller.getAllChannelsForCourse(courseId);

        textChannels.forEach(channel -> {
            RouterLink link=new RouterLink("", TextChannelView.class, channel.getId());
            Button button = new Button(channel.getName());
            button.addClassName(channel.getId()+"");
            link.getElement().appendChild(button.getElement());
            link.getStyle()
                    .set("pointer-event","none")
                    .set("padding-bottom","2.5px")
                    .set("background","none")
                    .set("margin-left","-60px");
            button.getStyle()
                    .set("font-weight","700")
                    .set("width","100%")
                    .set("background","none")
                    .set("cursor","pointer");
            button.addClassName("color"+channel.getId());
            if (s2.length>=4 && s2[3].equals("channels") && t.equals(channel.getId()+"")){
                button.getStyle().set("color",ColorHTML.PURPLE.getColorHtml());
            }else button.getStyle().set("color",ColorHTML.TEXTGREY.getColorHtml());
            sideBar.add(link);
        });
        // add the style
        sideBar.addClassName("card");
        sideBar.addClassName("cardLeft");
        setCardStyle(sideBar, "20%", TextChannelView.ColorHTML.DARKGREY);
    }

    public enum ColorHTML {
        PURPLE("#7510F7"),
        WHITE("#FFFFFF"),
        GREY("#EAEAEA"),
        DARKGREY("#DEDEDE"),
        TEXTGREY("#707070"),
        DANGER("#F04747");

        @Getter
        private final String colorHtml;

        ColorHTML(String s) {
            this.colorHtml = s;
        }
    }


    //unused
    /*public ComponentButton createButtonImage(String pathImage, String alt, Key shortCut){
        Image img =new Image(pathImage, alt);
        img.getStyle()
                .set("width","25px")
                .set("vertical-align","middle")
                .set("horizontal-align","middle");
        ComponentButton imgButton = new ComponentButton(img);
        imgButton.getStyle()
                .set("padding", "2.5px")
                .set("margin","0 2.5px");
        imgButton.addFocusShortcut(shortCut, KeyModifier.ALT);
        return imgButton;
    }

    public  Dialog createDialog(Button button){
        Dialog dialog = new Dialog();
        dialog.add(new Text("Close me with the esc-key or an outside click"));

        dialog.setWidth("400px");
        dialog.setHeight("150px");
        button.addClickListener(event -> dialog.open());
        return dialog;
    }*/
}
