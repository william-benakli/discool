package app.controller.commands;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;

public class CommandsClearChat {


    public CommandsClearChat(long sender, long channel, String arg[]) {
        if (arg.length <= 0) return;
        //TODO: verification à finaliser
        //if(channel.exist())
        //if(sender.getRole() instanceof Perso.Role
        if (arg[1].equalsIgnoreCase("Help")) {
            //faire afficher l'aide
            return;
        }
    }


    public void displayHelp() {
        Dialog d = new Dialog();
        Button valider = new Button("Valider");
        d.add(new Paragraph("Utilisation de la commande /clear"));
        d.add(new Paragraph("/clear all | Suppresion des tous les messages d'un channel"));
        d.add(new Paragraph("/clear [1-50] top | Suppresion des des 1 a 50 premier message d'un channel"));
        d.add(new Paragraph("/clear [1-50] bottom | Suppresion des des 1 a 50 derniere message d'un channel"));
        d.add(new Paragraph("/clear help | afficher l'aide"));
        d.add(valider);
        d.open();
        valider.addClickListener(event -> {
            d.close();
        });
    }
}
