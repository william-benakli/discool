package web;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import lombok.Getter;
import lombok.Setter;

public class ComponentButton extends Button {

    @Getter @Setter boolean status;

    @Getter @Setter String pathImageOn;

    @Getter @Setter String pathImageOff;

    @Getter @Setter String altOff;

    @Getter @Setter String altOn;

    @Getter @Setter Key shortCut;

    public ComponentButton(String pathImageOn, String pathImageOff, String altOff, String altOn, Key shortCut){
        super(new Image(pathImageOn, altOn));
        status=false;
        this.pathImageOn=pathImageOn;
        this.pathImageOff=pathImageOff;
        this.altOff=altOff;
        this.altOn=altOn;
        this.shortCut=shortCut;

    }

    /**
     * Actualise l'apparence d'un bouton
     * @param status
     * @param event
     */
    public void changeImage(boolean status, ClickEvent<Button> event){
        Image img = new Image(
                (status)?pathImageOff:pathImageOn,
                (status)?altOff:altOn
        );
        img.getStyle()
                .set("width","25px")
                .set("vertical-align","middle")
                .set("horizontal-align","middle");
        event.getSource().setIcon(img);
    }

    /**
     * Actualise un bouton apres un clique
     * @param event
     */
    public void changeStatus(ClickEvent<Button> event){
        this.status=!status;
        changeImage(this.status, event);
    }

}
