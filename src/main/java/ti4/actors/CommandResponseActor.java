package ti4.actors;


import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
//import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ti4.commands.CommandResponse;
//import ti4.commands.CommandResponse;
import ti4.message.BotLogger;

public class CommandResponseActor {

    public static Behavior<CommandResponse> create() {
        // Directly return the behavior setup to avoid any confusion
        return Behaviors.receive((context, response) -> {
            // Log receipt of command
            BotLogger.log("CommandResponseActor received a command: " + response.response);

            // Continue with the same behavior for the next message
            return Behaviors.same();
        });
    }
}