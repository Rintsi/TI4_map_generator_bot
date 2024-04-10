package ti4.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

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
            .onMessage(ButtonInteractionEvent.class, this::onButtonInteraction) // Updated type parameter
            .build();
    }

    private Behavior<ButtonInteractionEvent> onButtonInteraction(ButtonInteractionEvent event) {
        // Handle the button interaction
        // Example: Acknowledge the button click and send a response
        event.deferEdit().queue(); // Acknowledge the button click
        event.getChannel().sendMessage("Button clicked!").queue(); // Respond to the interaction
        return this;
    }
}
