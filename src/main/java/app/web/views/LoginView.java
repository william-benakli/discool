package app.web.views;

import app.controller.security.CustomRequestCache;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

@Tag("sa-login-view")
@Route(value = "login")
@PageTitle("Login")
public class LoginView extends VerticalLayout {
    private final LoginOverlay login = new LoginOverlay();

    // the AuthentificationManager will do the login validation
    // the CustomRequestCache will get the redirect URL
    public LoginView(@Autowired AuthenticationManager authenticationManager, CustomRequestCache requestCache) {
        // configures login dialog and adds it to the main view
        login.setOpened(true);
        System.out.println("test");

        login.setTitle("Log into Discool");
        login.setDescription("Please put your username and password");
        Paragraph text = new Paragraph("admin");
        text.setVisible(true);
        Paragraph pass = new Paragraph("password");

        Button valide = new Button("Valider");

        add(text);
        add(pass);
        add(valide);


        add(login);

        valide.addClickListener(e -> { // this listener has access to the provided username and password
            try {
                // try to authenticate with given credentials
                final Authentication authentication = authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(text.getText(), pass.getText()));

                // if authentication was successful we will update the security context and redirect to the page requested first
                SecurityContextHolder.getContext().setAuthentication(authentication);
                //      login.close();
                UI.getCurrent().navigate(requestCache.resolveRedirectUrl());
                System.out.println("Connexion reussite");
            } catch (AuthenticationException ex) { //
                // show default error message
                System.out.println("Connexion échoué");

                login.setError(true);
            }
        });
    }
}



