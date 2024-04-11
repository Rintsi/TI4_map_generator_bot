package ti4;

import org.apache.commons.lang3.StringUtils;

import akka.actor.typed.ActorRef;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ti4.commands.CommandMessage;
import ti4.commands.CommandResponse;
//import ti4.message.BotLogger;
import ti4.map.Game;
import ti4.map.GameManager;

public class BotSystem extends ListenerAdapter {
    private final ActorRef<CommandMessage> slashCommandDispatcher;
    private final ActorRef<CommandResponse> commandResponseActor;
    private final ActorRef<ButtonInteractionEvent> buttonIneractionActor;
    private final ActorRef<CommandAutoCompleteInteractionEvent> autoCompleteActor;

    public BotSystem(
        ActorRef<CommandMessage> slashCommandDispatcher, 
        ActorRef<CommandResponse> commandResponseActor, 
        ActorRef<ButtonInteractionEvent> buttonIneractionActor, 
        ActorRef<CommandAutoCompleteInteractionEvent> autoCompleteActor
    ) {
        this.slashCommandDispatcher = slashCommandDispatcher;
        this.commandResponseActor = commandResponseActor;
        this.buttonIneractionActor = buttonIneractionActor;
        this.autoCompleteActor = autoCompleteActor;
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        autoCompleteActor.tell(event);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        //String userID = event.getUser().getId();
        Game game = GameManager.resolveGameFromEvent(event);
        slashCommandDispatcher.tell(new CommandMessage(commandName, event, game, this.commandResponseActor));
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        buttonIneractionActor.tell(event);
    }
}
