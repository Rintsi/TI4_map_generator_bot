package ti4;

import ti4.actors.ButtonInteractionActor;
import ti4.actors.CommandAutoCompleteActor;
import ti4.actors.CommandResponseActor;
import ti4.actors.SlashCommandDispatcher;
import ti4.actors.game.GameCommandActor;
import ti4.commands.CommandMessage;
import ti4.commands.CommandResponse;
import ti4.message.BotLogger;

import java.util.Map;

import javax.security.auth.login.LoginException;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class BotApplication {

    public static Map<String, Behavior<CommandMessage>> commandHandlers;

    public static void main(String[] args) throws LoginException {
        
        BotApplication.commandHandlers = Map.of(
            "game", GameCommandActor.create()
        );

        //BotLogger.log("Mapping commands to actors" + commandHandlers.toString());

        ActorSystem<CommandMessage> commandDispatcherSystem = ActorSystem.create(SlashCommandDispatcher.create(BotApplication.commandHandlers), "commands");
        ActorSystem<CommandResponse> commmandResponseActorSystem = ActorSystem.create(CommandResponseActor.create(), "commandResponses");
        ActorSystem<ButtonInteractionEvent> buttonIneractionActorSystem = ActorSystem.create(ButtonInteractionActor.create(), "interactions");
        ActorSystem<CommandAutoCompleteInteractionEvent> commandAutoCompleteActor = ActorSystem.create(CommandAutoCompleteActor.create(), "autocompletes");

        // Initialize JDA and connect your bot
        JDABuilder.createDefault(args[0])
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .enableIntents(GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .setChunkingFilter(ChunkingFilter.ALL)
            .setEnableShutdownHook(false)
            .addEventListeners(new BotSystem(commandDispatcherSystem, commmandResponseActorSystem, buttonIneractionActorSystem, commandAutoCompleteActor))
            .build();
    }
}
