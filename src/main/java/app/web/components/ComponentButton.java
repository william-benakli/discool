package app.web.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import lombok.Getter;
import lombok.Setter;

/**
 * An extension of the Button class to add our style and several manipulation methods
 */
@Getter
@Setter
public class ComponentButton extends Button {

    /**
     * True if the button is selected, false otherwise
     */
    private boolean status;

    /**
     * Path of the icon when the button is selected
     */
    private String pathImageOn;

    /**
     * Path of the icon when the button is not selected
     */
    private String pathImageOff;

    private String altOff;

    private String altOn;

    /**
     * The shortcut to activate the button
     */
    private Key shortCut;

    public ComponentButton(String pathImageOn, String pathImageOff, String altOff, String altOn, Key shortCut) {
        super(new Image(pathImageOn, altOn));
        status = false;
        this.pathImageOn = pathImageOn;
        this.pathImageOff = pathImageOff;
        this.altOff = altOff;
        this.altOn = altOn;
        this.shortCut = shortCut;

    }

    /**
     * Actualise un bouton apres un clic
     *
     * @param event The even that fired the change
     */
    public void changeStatus(ClickEvent<Button> event) {
        this.status = !status;
        changeImage(this.status, event);
    }

    /**
     * Actualise l'apparence d'un bouton selon son status (on ou off)
     *
     * @param status True if the button is selected, false otherwise
     * @param event  The event that fired the change
     */
    public void changeImage(boolean status, ClickEvent<Button> event) {
        Image img = new Image(
                (status) ? pathImageOff : pathImageOn,
                (status) ? altOff : altOn
        );
        img.getStyle()
                .set("width", "25px")
                .set("vertical-align", "middle")
                .set("horizontal-align", "middle");
        event.getSource().setIcon(img);
    }

}
