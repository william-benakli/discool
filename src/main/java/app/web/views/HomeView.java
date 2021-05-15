package app.web.views;

import app.web.layout.Navbar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import javax.annotation.security.PermitAll;

@Route(value = "home", layout = Navbar.class)
@PermitAll
@PageTitle("Discool")
@CssImport("./styles/homeStyle.css")
@RouteAlias(value = "", layout = Navbar.class)
public class HomeView extends VerticalLayout {

    public HomeView(){
        imageRight(
                new Paragraph("Bienvenue sur Discool !"),
                new Paragraph("Le lieu parfait qui recrée à distance les meilleurs conditions réelles" +
                        " d'apprentissage tout en supprimant les contraintes du presentiel."),
                new Image("img/img.svg", "alt")
                );
        imageLeft(
                new Paragraph("Apprenez ou vous voulez !"),
                new Paragraph("Peu importe l'endroit ou vous etes, vous pouvez suivre vos cours et " +
                        "progresser en ayant qu'une connexion internet."),
                new Image("img/wfh_1.svg", "alt")
        );
        imageRight(
                new Paragraph("Un véritable encadrement scolaire"),
                new Paragraph("Les Administrateurs et les professeurs sont la afin d'avoir le meme" +
                        " encadrement qu'en présentiel."),
                new Image("img/Chat.svg", "alt")
        );
        imageLeft(
                new Paragraph("Preserver le contacte humain"),
                new Paragraph("Nous savons à quel point le contacte humain est important, c'est pour" +
                        " cela que nous avons mis a votre disposition un chat public et privé."),
                new Image("img/Chat.svg", "alt")
        );
    }

    public void imageLeft(Paragraph title, Paragraph text, Image image){

        this.add(title);
        this.add(text);
        this.add(image);
    }

    public void imageRight(Paragraph title, Paragraph text, Image image){

        this.add(title);
        this.add(text);
        this.add(image);
    }

}