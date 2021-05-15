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

    public HomeView() {
        H1 bienvenue = new H1("Bienvenue sur Discool !");

        // TODO add a RouterLink to the LoginView when the button is clicked
        Button join = new Button("Lancer Discool");
        Paragraph p = new Paragraph("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.");
        Image img = new Image("img/img.svg", "alt");
        add(img);

        bienvenue.getStyle().set("display", "block");
        bienvenue.getStyle().set("color", "#845ec2");

        join.getStyle().set("display", "block");
        join.getStyle().set("width", "200px");
            join.getStyle().set("height","80px");
            join.getStyle().set("background-color","#845ec2");
            join.getStyle().set("color","white");
            join.getStyle().set("border-radius","10px");
            join.getStyle().set("font-size","25px");
            join.addClickListener(click -> {
                UI.getCurrent().navigate("login");
            });
            p.getStyle().set("display","block");
            p.getStyle().set("font-size","30px");
            p.getStyle().set("margin","10px");

            img.getStyle().set("position","relative");
            img.getStyle().set("padding","1em");
            img.getStyle().set("width","800px");
            img.getStyle().set("height","800px");

            FlexLayout div2 = new FlexLayout();
            div2.add(bienvenue,p , join);
            div2.getStyle().set("display","block");
            div2.getStyle().set("left","0px");
            div2.getStyle().set("padding","3em");
            div2.getStyle().set("horizontal-align","middle");
            div2.getStyle().set("vertical-align","middle");
            div2.getStyle().set("width","600px");
            div2.getStyle().set("position","block");

            FlexLayout div1 = new FlexLayout();
            div1.add(img,div2);
            div1.getStyle().set("background-color","#f4f9f9");
            div1.setWidth("100%");
            div1.setHeight("40%");
            div1.getStyle().set("flex-direction","row-reverse");
            div1.getStyle().set("padding","0");
            add(div1);

            div("Bienvenue","Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.");
            divReverse("Bienvenue","Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.");
            div("Bienvenue","Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.");
        }

        public void div(String title, String text){
            Image img2 = new Image("img/books.jpg", "alt");
            img2.getStyle().set("width", "500px");
            img2.getStyle().set("height", "300px");
            img2.getStyle().set("left", "200px");

            H1 titre2 = new H1(title);
            titre2.getStyle().set("color", "#845ec2");

            Paragraph p2 = new Paragraph(text);
            p2.getStyle().set("font-size", "20px");
            p2.getStyle().set("margin", "10px");

            FlexLayout div3 = new FlexLayout();
            div3.add(titre2, p2);
            div3.getStyle().set("display", "inline-block");
            div3.getStyle().set("position", "relative");
            div3.getStyle().set("word-wrap", "break-word");
            div3.getStyle().set("height", "300px");
            div3.getStyle().set("margin-left", "20px");
            div3.getStyle().set("horizontal-align", "middle");


            FlexLayout div4 = new FlexLayout();
            div4.add(img2, div3);
            div4.getStyle().set("padding", "20px");
            div4.getStyle().set("width", "1200px");
            div4.getStyle().set("height", "300px");
            div4.getStyle().set("margin-left", "auto");
            div4.getStyle().set("margin-right", "auto");
            add(div4);
        }

        public void divReverse(String title , String text){
            Image img3= new Image("img/books.jpg", "alt");
            img3.getStyle().set("width","500px");
            img3.getStyle().set("height","300px");
            img3.getStyle().set("left","200px");
            img3.getStyle().set("float","right");

            H1 titre3 = new H1(title);
            titre3.getStyle().set("color","#845ec2");

            Paragraph p3 = new Paragraph(text);
            p3.getStyle().set("font-size","20px");
            p3.getStyle().set("margin","10px");

            FlexLayout div5 = new FlexLayout();
            div5.add(titre3,p3);
            div5.getStyle().set("display","inline-block");
            div5.getStyle().set("position","relative");
            div5.getStyle().set("word-wrap","break-word");
            div5.getStyle().set("height","300px");
            div5.getStyle().set("margin-left","20px");
            div5.getStyle().set("horizontal-align","middle");

            FlexLayout div6 = new FlexLayout();
            div6.add(img3, div5);
            div6.getStyle().set("padding","20px");
            div6.getStyle().set("width","1200px");
            div6.getStyle().set("height","300px");
            div6.getStyle().set("margin-left","auto");
            div6.getStyle().set("margin-right","auto");
            div6.getStyle().set("flex-direction","row-reverse");
            add(div6);
        }

}