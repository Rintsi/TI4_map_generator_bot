package ti4.actors;

import java.util.List;
import java.util.function.Function;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import ti4.AsyncTI4DiscordBot;
import ti4.message.BotLogger;

public class CommandAutoCompleteActor extends AbstractBehavior<CommandAutoCompleteInteractionEvent> {
    public static Behavior<CommandAutoCompleteInteractionEvent> create() {
        return Behaviors.setup(CommandAutoCompleteActor::new);
    }

    private CommandAutoCompleteActor(ActorContext<CommandAutoCompleteInteractionEvent> context) {
        super(context);
    }

    @Override
    public Receive<CommandAutoCompleteInteractionEvent> createReceive() {
        return newReceiveBuilder()
            .onMessage(CommandAutoCompleteInteractionEvent.class, this::onAutocomplete)
            .build();
    }

    private Behavior<CommandAutoCompleteInteractionEvent> onAutocomplete(CommandAutoCompleteInteractionEvent event) {
        String fullCommandName = event.getFullCommandName();

        BotLogger.log("CommandAutoCompleteActor received an autocomplete event: " + fullCommandName);

        Function<String, List<Command.Choice>> autoCompleteFunction = AsyncTI4DiscordBot.autoCompleteFunctions.get(fullCommandName);

        if(autoCompleteFunction != null) {
            String option = event.getFocusedOption().getName();
            List<Command.Choice> choices = autoCompleteFunction.apply(option);
            event.replyChoices(choices).queue();
        } else {
            event.getChannel().sendMessage("No autocomplete options available for this command.").queue();
        }
        
        return this;
    }
}
