package app;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

@CssImport("./styles/style.css")
@StyleSheet("https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap")
public class MainLayout extends AppLayout {

    public MainLayout(){
        createHeader();
    }

    /**
     *Cree la nav bar
     */
    private void createHeader() {
        HorizontalLayout header = new HorizontalLayout(/*logo*/);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidth("100%");
        header.addClassName("header");
        header.setHeight("75px");
        addToNavbar(header);
    }

}
