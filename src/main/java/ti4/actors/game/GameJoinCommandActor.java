package ti4.actors.game;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import ti4.commands.CommandMessage;
import ti4.commands.CommandResponse;
import ti4.message.BotLogger;

public class GameJoinCommandActor {

    public static Behavior<CommandMessage> create() {
        // Directly return the behavior setup to avoid any confusion
        return Behaviors.receive((context, message) -> {
            
            SlashCommandInteractionEvent event = message.event;
            String gameName = event.getOption("game_name").getAsString();
            String fullCommandName = event.getFullCommandName();
            
            // Log receipt of command
            BotLogger.log("GameJoinCommandActor received a command: " + fullCommandName);
            
            // Respond to the event
            event.reply("Game " + gameName).queue();

            message.replyTo.tell(new CommandResponse(gameName));
            
            // Continue with the same behavior for the next message
            return Behaviors.same();
        });
    }
}
