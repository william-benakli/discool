package app.web.views;

import app.web.layout.MainLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;


@Route(value = "", layout = MainLayout.class)
public class MainView extends VerticalLayout {

    public MainView() {
        VerticalLayout layout = new VerticalLayout();
        this.add(new RouterLink("moodle 1", MoodleView.class, 1L));
        this.add(new RouterLink("chat1cours1", TextChannelView.class, 1L));
    }

}
