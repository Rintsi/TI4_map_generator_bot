package ti4.actors.game;

import java.util.List;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import ti4.AsyncTI4DiscordBot;
import ti4.actors.AutoCompleteable;
import ti4.commands.CommandMessage;
import ti4.commands.CommandResponse;
import ti4.message.BotLogger;

public class GameJoinActor implements AutoCompleteable {

    public static Behavior<CommandMessage> create() {
        AsyncTI4DiscordBot.registerAutoComplete("game join", GameJoinActor::getAutoCompleteOptions);

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

    public static List<Command.Choice> getAutoCompleteOptions(String option) {

        List<Command.Choice> optionsList = null;

        switch (option) {
            case "game_name":
                optionsList = List.of(
                    new Command.Choice("Game 1", "game1"), 
                    new Command.Choice("Game 2", "game2")
                );
            default:
        }

        return optionsList;
    }
}
