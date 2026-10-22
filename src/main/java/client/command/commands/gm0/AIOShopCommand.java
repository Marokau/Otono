package client.command.commands.gm0;

import client.Client;
import client.command.Command;

public class AIOShopCommand extends Command {

    {
        setDescription("Open the AIO shop.");
    }

    @Override
    public void execute(Client client, String[] params) {
        client.getAbstractPlayerInteraction().openNpc(9000000, "AIOshop");
    }
}