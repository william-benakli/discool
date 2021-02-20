package app.web;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route("")
public class MainView extends ComponentBuilder {

    public MainView() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(new RouterLink("MOODLE", MoodleView.class, 1L));
        layout.add(new RouterLink("CHAT", TextChannelView.class, 1L));
        add(layout);
    }
}
