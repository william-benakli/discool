package app.web.components;

import app.jpa_repo.TextChannelRepository;
import app.model.chat.TextChannel;
import app.web.views.MoodleView;
import app.web.views.TextChannelView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouterLink;
import lombok.Getter;

import java.util.ArrayList;

/**
 * Contains methods to create Component with a specific style
 */
public class ComponentBuilder extends VerticalLayout {

    /**
     * Creates a layout for the page from the Component passed as args.
     * Gives each card general style settings and puts them next to each other.
     *
     * @param card The Components to add in the layout
     * @return The HorizontalLayout to display on the page
     */
    public static HorizontalLayout createLayout(Component... card) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.getStyle()
                .set("position", "absolute")
                .set("top", "150px")
                .set("bottom", "0")
                .set("margin", "0")
                .set("padding", "0");
        for (Component item : card) layout.add(item);
        return layout;
    }

    /**
     * Cree une div avec les messages de l'utilisateur
     *
     * @param color       The color
     * @param width       The width of the div
     * @param className   The name of the CSS class name
     * @param messageUser The messages to put in the div
     * @param button      The Button(s) to add to the div
     * @return the center panel for the chat messages
     */
    public static Component createMessageCard(ColorHTML color, String width, String className, String[] messageUser, Button... button) {
        FlexLayout card = new FlexLayout();
        card.addClassName("card");
        card.addClassName(className);
        setCardStyle(card, width, color);
        cardCenter(card, messageUser, button);
        return card;
    }

    /**
     * Cree un bouton avec le texte et le style specifie
     *
     * @param label      The text to put on the button
     * @param color      The color of the button
     * @param paramStyle An array of CSS options to specify the style of the button
     * @return a button with a label and a style
     */
    public static Button createButtonText(String label, String color, String[]... paramStyle) {
        //TODO: add style simple
        Button button = new Button(label);
        button.getStyle()
                .set("background-color", color)
                .set("color", ColorHTML.WHITE.getColorHtml());
        for (String[] a : paramStyle) {
            button.getStyle().set(a[0], a[1]);
        }
        return button;
    }

    /**
     * Sets general style parameters for the 3 big panels (left, right and center cards)
     *
     * @param card  The card to modify
     * @param width The width of the card
     * @param color The color of the background
     */
    public static void setCardStyle(FlexLayout card, String width, ColorHTML color) {
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
     * Creates the center card for the Moodle sections
     *
     * @param color          The color of the background
     * @param width          The width of the card
     * @param courseSections A LinkedList of the course sections, in order
     * @return the center panel for the Moodle pages
     */
    public static Component createMoodleSectionsCard(ColorHTML color, String width, ArrayList<MoodleView.SectionLayout> courseSections) {
        FlexLayout card = new FlexLayout();
        card.addClassName("card");
        card.addClassName("cardCenter");
        setCardStyle(card, width, color);
        courseSections.forEach(card::add);

        return card;
    }

    /**
     * Creates the card on the right of the page that contains all the members of each chat
     *
     * @param color The color of the background
     * @param width the width of the card
     * @return the right hand panel containing the members of the channel
     */
    public static Component createMembersCard(ColorHTML color, String width) {
        FlexLayout card = new FlexLayout();
        card.addClassName("card");
        card.addClassName("cardCenter");
        setCardStyle(card, width, color);
        return card;
    }

    /**
     * Creates the sidebar for Moodle pages and TextChannels.
     * It contains
     *
     * @param width                 The width of the div
     * @param courseId              The id of the course
     * @param textChannelRepository The JPA Repo to query the TextChannels from
     * @return the left-hand panel containing the sidebar to navigate between channels and Moodle on a spcific course
     */
    public static Component createSideBar(String width, long courseId, TextChannelRepository textChannelRepository) {
        FlexLayout sidebar = new FlexLayout();
        // add the RouterLinks
        sidebar.add(new RouterLink("Page d'accueil", MoodleView.class, courseId));
        ArrayList<TextChannel> textChannels = textChannelRepository.findAllByCourseId(courseId);
        textChannels.forEach(channel -> sidebar.add(new RouterLink(channel.getName(), TextChannelView.class, channel.getId())));
        // add the style
        sidebar.addClassName("card");
        sidebar.addClassName("cardLeft");
        setCardStyle(sidebar, width, ColorHTML.DARKGRAY);
        return sidebar;
    }


    public enum ColorHTML {
        PURPLE("#7510F7"),
        WHITE("#FFFFFF"),
        GREY("#EAEAEA"),
        DARKGRAY("#DEDEDE");

        @Getter
        private final String colorHtml;

        ColorHTML(String s) {
            this.colorHtml = s;
        }
    }

    /**
     * Ajoute des options suplémentaires a la div de chat
     *
     * @param card        The div to add the options to
     * @param messageUser The user messages
     * @param button      The Button(s) to add to the div
     */
    public static void cardCenter(FlexLayout card, String[] messageUser, Button... button) {
        FlexLayout chatButton = new FlexLayout();
        chatButton.getStyle()
                .set("padding", "0 2.5px");
        for (Component component : button) {
            chatButton.add(component);
        }

        FlexLayout messageMenu = new FlexLayout();
        messageMenu.add(
                createTextField(),
                chatButton
                //TODO: add button mute and demute
        );

        FlexLayout cardTop = new FlexLayout();
        cardTop.setHeightFull();
        messageMenu.getStyle()
                .set("position", "-webkit-sticky")
                .set("position", "sticky")
                .set("bottom", "0px")
                .set("background-color", ColorHTML.GREY.getColorHtml());
        //TODO: add text

        for (String message : messageUser) {
            cardTop.add(createMessageCard(message));
        }
        cardTop.getStyle().set("flex-direction", "column-reverse");
        card.add(cardTop, messageMenu);
    }

    /**
     * Cree le champ pour permettre a l'utilisateur d'envoyer des messages
     *
     * @return a TextField for the user to input a message
     */
    public static Component createTextField() {
        TextField textField = new TextField();
        textField.setPlaceholder("Envoyer un message");
        textField.setWidthFull();
        textField.addFocusShortcut(Key.KEY_T, KeyModifier.ALT);//textField.setLabel("Press ALT + T to focus");
        textField.getStyle().set("margin", "0 2.5px");
        return textField;
    }

    /**
     * Affiche dans un chat un message envoye par l'utilisateur
     *
     * @param text The text to put on the card
     * @return a Paragraph containing the message card
     */
    public static Component createMessageCard(String text) {
        Paragraph para = new Paragraph();
        para.setTitle(text);
        para.setText(text);
        para.getStyle().set("padding", "10px");
        return para;
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
