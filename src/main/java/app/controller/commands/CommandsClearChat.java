package app.controller.commands;

import app.controller.Controller;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;

public class CommandsClearChat {


    public CommandsClearChat(Controller controller, long sender, long channel, String arg[]) {
        if (arg.length <= 0 || arg.length > 3) return;
        //TODO: verification à finaliser
        //if(channel.exist())
        //if(sender.getRole() instanceof Perso.Role

        if (arg[1].equalsIgnoreCase("Help")) {
            //faire afficher l'aide
            displayHelp();
        } else if (arg[1].equalsIgnoreCase("all")) {
            controller.clearMessageChat();
            //suppression de tous les messages;
        } else if (arg.length == 1) {
            if (isInteger(arg[1])) {
                int value = Integer.valueOf(arg[1]);
                if (value >= 1 || value <= 50) {
                    controller.clearMessageChat(value);
                } else {
                    displayErreur("Nombre trop grand");
                }
            } else {
                displayErreur("Nombre incorrect");
            }
        } else {
            displayErreur("Sasie incorrect");
        }

    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    public void displayHelp() {
        Dialog d = new Dialog();
        Button valider = new Button("Valider");
        d.add(new Paragraph("Utilisation de la commande /clear"));
        d.add(new Paragraph("/clear help | Afficher l'aide"));
        d.add(new Paragraph("/clear all | Suppresion des tous les messages d'un channel"));
        d.add(new Paragraph("/clear [1-50] | Suppresion des des 1 a 50 messages d'un channel"));
        d.add(valider);
        d.open();
        valider.addClickListener(event -> {
            d.close();
        });
    }

    public void displayErreur(String erreur) {
        Dialog d = new Dialog();
        Button valider = new Button("Valider");
        d.add(new Paragraph("Mauvaise utilisation de la commande !"));
        d.add(new Paragraph("Erreur: " + erreur));
        d.add(new Paragraph("/clear help | Afficher l'aide"));
        d.add(valider);
        d.open();
        valider.addClickListener(event -> {
            d.close();
        });
    }
}
