package ti4.commands;

import akka.actor.typed.ActorRef;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import ti4.map.Game;

public class CommandMessage {
    public final String commandName;
    public final SlashCommandInteractionEvent event;
    public final Game game;
    public final ActorRef<CommandResponse> replyTo;

    public CommandMessage(String commandName, SlashCommandInteractionEvent event, Game game, ActorRef<CommandResponse> replyTo) {
        this.commandName = commandName;
        this.event = event;
        this.game = game;
        this.replyTo = replyTo;
    }
}
