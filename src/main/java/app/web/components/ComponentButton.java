package app.web.components;

import app.web.views.ViewWithSidebars;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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

    /**
     * Icon alt attribute when button is not selected
     */
    private String altOff;

    /**
     * Icon alt attribute when button is selected
     */
    private String altOn;

    /**
     * The shortcut to activate the button
     */
    private Key shortCut;

    public ComponentButton(String pathImageOn, String pathImageOff, String altOff, String altOn, Key shortCut) {
        setIcon(SetStyle(new Image(pathImageOn, altOn)));
        status = false;
        this.pathImageOn = pathImageOn;
        this.pathImageOff = pathImageOff;
        this.altOff = altOff;
        this.altOn = altOn;
        this.shortCut = shortCut;

    }

    public ComponentButton(Image imgServ) {
        SetStyle(imgServ).setHeightFull();
        SetStyle(imgServ).setWidthFull();
        setIcon(imgServ);
    }

    public ComponentButton(Image imgServ, int width, int height) {
        imgServ.getStyle().set("width", String.valueOf(width) + "px").set("height", String.valueOf(height) + "px");
        setIcon(imgServ);
    }

    public ComponentButton(String cheminImage, String alt, int sizeWitdh, int sizeHeight) {
        Image imgServ = new Image(cheminImage, alt);
        imgServ.getStyle()
                .set("width", String.valueOf(sizeWitdh) + "px")
                .set("height", String.valueOf(sizeHeight) + "px")
                .set("border-radius", "0px");
        setIcon(imgServ);
        getStyle().set("background-color", ViewWithSidebars.ColorHTML.GREY.getColorHtml());
    }

    public ComponentButton(VaadinIcon icon, int sizeWitdh, int sizeHeight) {
        Icon iconI = new Icon(icon);
        iconI.getStyle()
                .set("widht",sizeWitdh+"px")
                .set("height",sizeHeight+"px");
        setIcon(iconI);
        getStyle().set("background-color", ViewWithSidebars.ColorHTML.GREY.getColorHtml());
    }


    /**
     * Add css properties to the image of a button
     *
     * @param img The image that will undergo styling changes
     * @return The image with new css properties
     */
    public Image SetStyle(Image img) {
        img.getStyle()
                .set("width","25px")
                .set("vertical-align","middle")
                .set("horizontal-align","middle");
        return img;
    }

    /**
     * Refresh a button after a click
     *
     * @param event The even that fired the change
     */
    public void changeStatus(ClickEvent<Button> event) {
        this.status = !status;
        changeImage(this.status, event);
    }

    /**
     * Updates the appearance of a button according to its status (on or off)
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
