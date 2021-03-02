package app.web.views;

import app.controller.Controller;
import app.model.chat.TextChannel;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import lombok.Getter;
import lombok.Setter;

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

    public void createMembersBar(long courseId) {
        membersBar = new FlexLayout();
        membersBar.addClassName("card");
        membersBar.addClassName("cardCenter");
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
    public void createSidebar(long courseId) {
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
            RouterLink link=new RouterLink(channel.getName(), TextChannelView.class, channel.getId());
            link.getStyle()
                    .set("color",ColorHTML.TEXTGREY.getColorHtml())
                    .set("padding-left","35px")
                    .set("font-weight","700")
                    .set("pointer-event","none")
                    .set("padding-bottom","2.5px");
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
        TEXTGREY("#707070");

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
