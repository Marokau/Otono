package client.command.commands.gm0;

import client.Client;
import client.command.Command;
import constants.id.NpcId;

public class WhereLevelCommand extends Command {
    {
        setDescription("Open training spots, live bot spots, and the Night Lord build guide.");
    }

    @Override
    public void execute(Client client, String[] params) {
        client.getAbstractPlayerInteraction().openNpc(NpcId.STEWARD, "wherelevel");
    }
}
