package web;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;

public class ComponentBuilder extends VerticalLayout {

    public enum ColorHTML {
        PURPLE("#7510F7"),
        WHITE("#FFFFFF"),
        GREY("#EAEAEA"),
        DARKGRAY("#DEDEDE");
        @Getter
        private String colorHtml;

        ColorHTML(String s) {
            this.colorHtml=s;
        }
    }

    /**
     * Cree des boutons
     * @param Text
     * @param color
     * @param paramStyle
     * @return
     */
    public Button createButtonText(String Text, String color, String[]... paramStyle){//TODO: add style simple
        Button button= new Button(Text);
        button.getStyle()
                .set("background-color",color)
                .set("color", ColorHTML.WHITE.getColorHtml());
                //.set("margin","0 0 0 2.5px");
        for (String[] a:paramStyle) {
            button.getStyle().set(a[0],a[1]);
        }
        return button;
    }

    /**
     * Cree le champ pour permettre a l'utilisateur d'envoyer des messages
     * @return
     */
    public Component textField(){
        TextField textField = new TextField();
        textField.setPlaceholder("Envoyer un message");
        textField.setWidthFull();
        textField.addFocusShortcut(Key.KEY_T, KeyModifier.ALT);//textField.setLabel("Press ALT + T to focus");
        textField.getStyle().set("margin","0 2.5px");
        return textField;
    }

    /**
     * Ajoute des options suplémentaires a la div de chat
     * @param card
     * @param messageUser
     * @param button
     */
    public void cardCenter(FlexLayout card,String[] messageUser ,Button... button ){
        FlexLayout chatButton = new FlexLayout();
        chatButton.getStyle()
                .set("padding","0 2.5px");
        for (Component component: button) {
            chatButton.add(component);
        }

        FlexLayout messageMenu = new FlexLayout();
        messageMenu.add(
                textField(),
                chatButton
                //TODO: add button mute and demute
        );

        FlexLayout cardTop = new FlexLayout();
        cardTop.setHeightFull();
        messageMenu.getStyle()
                .set("position","-webkit-sticky")
                .set("position","sticky")
                .set("bottom","0px")
                .set("background-color",ColorHTML.GREY.getColorHtml());
        //TODO: add text

        for (String message: messageUser) {
            cardTop.add(messageCard(message));
        }
        cardTop.getStyle().set("flex-direction","column-reverse");
        card.add(cardTop,messageMenu);
    }

    /**
     * Cree les divs de couleur GREY
     * @param color
     * @param width
     * @param className
     * @param messageUser
     * @param button
     * @return
     */
    public Component createCard(ColorHTML color, String width, String className,String[] messageUser ,Button... button) {
        FlexLayout card = new FlexLayout();
        card.addClassName("card");
        card.addClassName(className);
        card.getStyle()
                .set("overflow","auto")
                .set("width",width)
                .set("margin","0px")
                .set("background",color.getColorHtml())
                .set("display","flex")
                .set("flex-direction","column")
                .set("padding","10px");
        switch (className){
            case "cardCenter":
                cardCenter(card,messageUser , button);
                break;

            default:
                //TODO: Définir une largeur max pour les div DARKGREY : card.getStyle().set("max-width", "275px");
                break;
        }

        return card;
    }

    /**
     * Affiche dans un chat un message envoye par un utilisateur de maniere lisible.
     * @param text
     * @return
     */
    public Component messageCard(String text){
        Paragraph para = new Paragraph();
        para.setTitle(text);
        para.setText(text);
        para.getStyle().set("padding","10px");
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
