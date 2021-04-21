package app.controller.security;

import app.web.views.HomeView;
import app.web.views.LoginView;
import app.web.views.PanelAdminView;
import app.web.views.TeacherAssignmentView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

@Component
public class ConfigureUIServiceInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            final UI ui = uiEvent.getUI();
            ui.addBeforeEnterListener(this::beforeEnter);
        });
    }

    /**
     * Reroutes the user if (s)he is not authorized to access the view.
     * If the view is Login or Home, don't reroute
     *
     * @param event before navigation event with event details
     */
    private void beforeEnter(BeforeEnterEvent event) {
        if ((!LoginView.class.equals(event.getNavigationTarget())
                || !HomeView.class.equals(event.getNavigationTarget()))
                && !SecurityUtils.isUserLoggedIn()) {
            event.rerouteTo(LoginView.class);
        } else if (PanelAdminView.class.equals(event.getNavigationTarget()) && !SecurityUtils.isUserAdmin()) {
            event.rerouteTo(HomeView.class);
        } else if (TeacherAssignmentView.class.equals(event.getNavigationTarget()) && SecurityUtils.isUserStudent()) {
            event.rerouteTo(HomeView.class);
        }
    }
}