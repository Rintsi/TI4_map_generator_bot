package ti4.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
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
        BotLogger.log("Options for: " + event.getFocusedOption().getName());
        if ("game_name".equals(event.getFocusedOption().getName())) {
            event.replyChoices(
                new Command.Choice("Option 1", "option1"),
                new Command.Choice("Option 2", "option2")
            ).queue();
        }
        return this;
    }
}
