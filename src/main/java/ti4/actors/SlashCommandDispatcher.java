package ti4.actors;

import java.util.Map;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import ti4.commands.CommandMessage;
import ti4.commands.CommandResponse;

public class SlashCommandDispatcher {
    
    private final ActorContext<CommandMessage> context;
    private final Map<String, Behavior<CommandMessage>> commandHandlers;

    public static Behavior<CommandMessage> create(Map<String, Behavior<CommandMessage>> commandHandlers) {
        return Behaviors.setup(context -> new SlashCommandDispatcher(context, commandHandlers).behavior());
    }

    private SlashCommandDispatcher(ActorContext<CommandMessage> context, Map<String, Behavior<CommandMessage>> commandHandlers) {
        this.context = context;
        this.commandHandlers = commandHandlers;
    }

    private Behavior<CommandMessage> behavior() {
        return Behaviors.receive(CommandMessage.class)
                .onMessage(CommandMessage.class, this::onCommandMessage)
                .build();
    }

    private Behavior<CommandMessage> onCommandMessage(CommandMessage message) {
        Behavior<CommandMessage> handler = commandHandlers.get(message.commandName);
        if (handler != null) {
            ActorRef<CommandMessage> actorRef = context.spawn(handler, message.commandName);
            actorRef.tell(message);
        } else {
            message.replyTo.tell(new CommandResponse("Unknown command: " + message.commandName));
        }
        return Behaviors.same();
    }
}
