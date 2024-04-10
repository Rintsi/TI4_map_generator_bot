package ti4.commands;

import akka.actor.typed.ActorRef;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandMessage {
    public final String commandName;
    public final SlashCommandInteractionEvent event;
    public final ActorRef<CommandResponse> replyTo;

    public CommandMessage(String commandName, SlashCommandInteractionEvent event, ActorRef<CommandResponse> replyTo) {
        this.commandName = commandName;
        this.event = event;
        this.replyTo = replyTo;
    }
}
