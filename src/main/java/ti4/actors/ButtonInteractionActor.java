package ti4.actors;

import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import ti4.AsyncTI4DiscordBot;
import ti4.buttons.ButtonInteractionContext;
import ti4.map.Game;
import ti4.map.GameManager;

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
        Game game = GameManager.resolveGameFromEvent(event);
        String handlerName = StringUtils.substringBefore(buttonId, "|");
        String attributes = StringUtils.substringAfter(buttonId, handlerName + "|");

        ButtonInteractionContext context = new ButtonInteractionContext(event, game, handlerName, attributes);

        Consumer<ButtonInteractionContext> buttonHandler = AsyncTI4DiscordBot.buttonHandlers.get(handlerName);

        if(buttonHandler != null) {
            buttonHandler.accept(context);
        } else {
            event.getChannel().sendMessage("No handler available for this button.").queue();
        }
 
        return this;
    }
}
