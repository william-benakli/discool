package app.web.views;

import app.web.layout.Navbar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;


@Route(value = "me", layout = Navbar.class)
public class UserHomeView extends HorizontalLayout {

    public UserHomeView() {
        VerticalLayout layout = new VerticalLayout();
        this.add(new RouterLink("moodle 1", MoodleView.class, 1L));
        this.add(new RouterLink("chat1cours1", PublicTextChannelView.class, 1L));
    }

}
