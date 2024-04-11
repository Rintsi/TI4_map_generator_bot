package ti4.actors;

import java.util.function.Consumer;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import ti4.AsyncTI4DiscordBot;

public class ButtonInteractionActor extends AbstractBehavior<ButtonInteractionEvent> {
    
    public static Behavior<ButtonInteractionEvent> create() {
        return Behaviors.setup(ButtonInteractionActor::new);
    }

    private ButtonInteractionActor(ActorContext<ButtonInteractionEvent> context) {
        super(context);
    }

    @Override
    public Receive<ButtonInteractionEvent> createReceive() {
        return newReceiveBuilder()
            .onMessage(ButtonInteractionEvent.class, this::onButtonInteraction)
            .build();
    }

    private Behavior<ButtonInteractionEvent> onButtonInteraction(ButtonInteractionEvent event) {

        String buttonId = event.getComponentId();

        Consumer<ButtonInteractionEvent> buttonHandler = AsyncTI4DiscordBot.buttonHandlers.get(buttonId);

        if(buttonHandler != null) {
            buttonHandler.accept(event);
        } else {
            event.getChannel().sendMessage("No handler available for this button.").queue();
        }
 
        return this;
    }
}
