package ti4.actors.game;

import java.util.Map;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import ti4.commands.CommandMessage;
import ti4.commands.CommandResponse;
//import ti4.commands.CommandResponse;
//import ti4.message.BotLogger;

public class GameCommandActor {

    public static Map<String, Behavior<CommandMessage>> gameCommandHandlers = Map.of(
        "game create_game_button", GameCreateGameButtonActor.create(),
        "game join", GameJoinActor.create(),
        "game add", GameAddActor.create()
    );

    public static Behavior<CommandMessage> create() {
        // Directly return the behavior setup to avoid any confusion
        return Behaviors.receive((context, message) -> {
            
            SlashCommandInteractionEvent event = message.event;
            String fullCommandName = event.getFullCommandName();
            
            //BotLogger.log("GameCommandActor received a command: " + fullCommandName);

            Behavior<CommandMessage> handler = GameCommandActor.gameCommandHandlers.get(fullCommandName);
            if (handler != null) {
                //BotLogger.log("GameCommandActor found a handler for the command: " + fullCommandName);
                ActorRef<CommandMessage> actorRef = context.spawn(handler, fullCommandName.replace(" ", "_"));
                actorRef.tell(message);
            } else {
                message.replyTo.tell(new CommandResponse("Unknown command: " + fullCommandName));
            }
            // Continue with the same behavior for the next message
            return Behaviors.same();
        });
    }
}
