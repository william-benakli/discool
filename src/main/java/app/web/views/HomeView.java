package app.web.views;

import app.web.layout.Navbar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
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
                new Paragraph("Le lieu parfait qui recrée à distance les meilleures conditions réelles" +
                        " d'apprentissage tout en supprimant les contraintes du présentiel."),
                new Image("img/homeimage.svg", "alt")
                );
        imageLeft(
                new Paragraph("Apprenez ou vous voulez !"),
                new Paragraph("Peu importe l'endroit où vous êtes, vous pouvez suivre vos cours et " +
                        "progresser en ayant qu'une connexion internet."),
                new Image("img/wfh_1.svg", "alt")
        );
        imageRight(
                new Paragraph("Un véritable encadrement scolaire"),
                new Paragraph("Les administrateurs et les professeurs sont là afin d'avoir le même" +
                        " encadrement qu'en présentiel."),
                new Image("img/Headhunter.svg", "alt")
        );
        imageLeft(
                new Paragraph("Préserver le contact humain"),
                new Paragraph("Nous savons à quel point le contacte humain et important, c'est pour" +
                        " cela que nous avons mis à votre disposition un chat public et privé."),
                new Image("img/Chat.svg", "alt")
        );
    }

    public void imageLeft(Paragraph title, Paragraph text, Image image){
        Div main = new Div();
        Div inter = new Div();
        Div left = new Div();
        Div right = new Div();
        Div interText = new Div();

        //Style Div
        styleDiv(main, title, text, interText, image, inter);
        left.getStyle().set("margin","auto");
        right.getStyle()
                .set("display","flex")
                .set("margin","auto");

        interText.add(title, text);
        left.add(image);
        right.add(interText);
        inter.add(left, right);
        main.add(inter);
        this.add(main);
    }

    public void imageRight(Paragraph title, Paragraph text, Image image){
        Div main = new Div();
        Div inter = new Div();
        Div left = new Div();
        Div right = new Div();
        Div interText = new Div();

        //Style Div
        styleDiv(main, title, text, interText, image, inter);
        right.getStyle().set("margin","auto");
        left.getStyle()
                .set("display","flex")
                .set("margin","auto");

        interText.add(title, text);
        right.add(image);
        left.add(interText);
        inter.add(left, right);
        main.add(inter);
        this.add(main);
    }

    void styleDiv(Div main, Paragraph title, Paragraph text, Div interText, Image image, Div inter){
        main.getStyle()
                .set("height","100vh")
                .set("width","100%")
                .set("display","flex")
                .set("margin","auto")
                .set("border-bottom","solid 1px"+ ViewWithSidebars.ColorHTML.GREYTAB.getColorHtml());
        title.getStyle()
                .set("color", ViewWithSidebars.ColorHTML.PURPLE.getColorHtml())
                .set("font-weight","700")
                .set("font-size","20px");
        text.getStyle()
                .set("font-size","20px")
                .set("text-align","justify");
        interText.getStyle()
                .set("max-width","350px")
                .set("margin","auto");
        image.getStyle()
                .set("height","600px")
                .set("width","600px");
        inter.getStyle()
                .set("display","flex")
                .set("flex-direction","row")
                .set("margin","auto")
                .set("flex-wrap","wrap");
    }

}