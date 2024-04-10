package ti4.actors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import ti4.BotApplication;
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

    @SuppressWarnings("unchecked")
    private Behavior<CommandAutoCompleteInteractionEvent> onAutocomplete(CommandAutoCompleteInteractionEvent event) throws ReflectiveOperationException {
        String commandName = event.getName();
        String fullCommandName = event.getFullCommandName();

        try {
            Class<?> rootActor = BotApplication.commandClasses.get(commandName);
            if (rootActor != null) {
                Field subCommandField = rootActor.getDeclaredField("subCommandClasses");
                subCommandField.setAccessible(true);
                Map<String, Class<?>> subCommandClasses = (Map<String, Class<?>>) subCommandField.get(null); // null for static fields
                Class<?> subCommandActor = subCommandClasses.get(fullCommandName);
                
                if (subCommandActor != null) {// && AutoCompleteable.class.isAssignableFrom(subCommandActor)) {

                    Method getAutoCompleteOptionsMethod = subCommandActor.getMethod("getAutoCompleteOptions", String.class);
                    getAutoCompleteOptionsMethod.setAccessible(true);

                    try {
                        List<Choice> choices = (List<Choice>) getAutoCompleteOptionsMethod.invoke(null, event.getFocusedOption().getName());

                        BotLogger.log("Found subcommand actor: " + subCommandActor.getName());
                        event.replyChoices(choices).queue();
                    } catch (Exception e) {
                        BotLogger.log("Error getting subcommand choices: " + e.getMessage());
                    }
                } else {
                    BotLogger.log("No subcommand actor found for command: " + fullCommandName);
                }
            } else {
                BotLogger.log("No handler found for command: " + fullCommandName);
            }
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
            BotLogger.log("Error getting subcommand classes: " + e.getMessage());
            throw(e);
        }
        return this;
    }
}
