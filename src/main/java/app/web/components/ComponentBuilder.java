package app.web.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;

/**
 * Contains methods to create Component with a specific style
 */
public class ComponentBuilder extends VerticalLayout {

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
        //.set("margin","0 0 0 2.5px");
        for (String[] a : paramStyle) {
            button.getStyle().set(a[0], a[1]);
        }
        return button;
    }

    /**
     * Cree les divs de couleur GREY
     *
     * @param color       The color
     * @param width       The width of the div
     * @param className   The name of the CSS class name
     * @param messageUser The messages to put in the div
     * @param button      The Button(s) to add to the div
     * @return a grey div with the user messages
     */
    public static Component createCard(ColorHTML color, String width, String className,String[] messageUser ,Button... button) {
        FlexLayout card = new FlexLayout();
        card.addClassName("card");
        card.addClassName(className);
        card.getStyle()
                .set("overflow", "auto")
                .set("width", width)
                .set("margin", "0px")
                .set("background", color.getColorHtml())
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("padding", "10px");
        switch (className){
            case "cardCenter":
                cardCenter(card, messageUser, button);
                break;

            default:
                //TODO: Définir une largeur max pour les div DARKGREY : card.getStyle().set("max-width", "275px");
                break;
        }

        return card;
    public static Component createCard(ColorHTML color, String width, String className, String[] messageUser, Button... button) {
        FlexLayout card = new FlexLayout();
        card.addClassName("card");
        card.addClassName(className);
        card.getStyle()
                .set("overflow", "auto")
                .set("width", width)
                .set("margin", "0px")
                .set("background", color.getColorHtml())
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("padding", "10px");
        switch (className){
            case "cardCenter":
                cardCenter(card, messageUser, button);
                break;

            default:
                //TODO: Définir une largeur max pour les div DARKGREY : card.getStyle().set("max-width", "275px");
                break;
        }

        return card;
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
     * Creates a Paragraph with the user's message
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
