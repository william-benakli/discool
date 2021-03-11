package app.controller.commands;

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


}
