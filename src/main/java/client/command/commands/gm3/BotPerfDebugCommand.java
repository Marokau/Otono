package client.command.commands.gm3;

import client.Character;
import client.Client;
import client.command.Command;
import server.bots.BotPerformanceMonitor;

public class BotPerfDebugCommand extends Command {
    {
        setDescription("Toggle bot performance monitor: !botperfdebug [on|off]");
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        boolean nowEnabled;
        if (params.length == 0) {
            nowEnabled = BotPerformanceMonitor.toggleEnabled();
        } else {
            String arg = params[0].toLowerCase();
            switch (arg) {
                case "on":
                    BotPerformanceMonitor.setEnabled(true);
                    nowEnabled = true;
                    break;
                case "off":
                    BotPerformanceMonitor.setEnabled(false);
                    nowEnabled = false;
                    break;
                default:
                    player.yellowMessage("Syntax: !botperfdebug [on|off]");
                    return;
            }
        }
        player.yellowMessage("bot performance monitor: " + (nowEnabled ? "ON" : "OFF"));
    }
}
