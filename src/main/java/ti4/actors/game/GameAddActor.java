package ti4.actors.game;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import ti4.actors.AutoCompleteable;
import ti4.commands.CommandMessage;
import ti4.helpers.Constants;
import ti4.helpers.Helper;
import ti4.map.Game;
import ti4.map.GameSaveLoadManager;
//import ti4.message.BotLogger;
import ti4.message.MessageHelper;

public class GameAddActor implements AutoCompleteable {

    public static Behavior<CommandMessage> create() {

        return Behaviors.receive((context, message) -> {
            SlashCommandInteractionEvent event = message.event;
            event.reply("Processing your command...").setEphemeral(true).queue();
            Game game = message.game;
            if (game == null){
                MessageHelper.sendMessageToChannel(event.getChannel(), "Game not found");
                return Behaviors.same();
            }
            
            User user = event.getUser();
            addExtraUsers(event, game);
            Helper.fixGameChannelPermissions(event.getGuild(), game);
            GameSaveLoadManager.saveMap(game, event);
            MessageHelper.replyToMessage(event, getResponseMessage(game, user));

            event.reply("Processing your command...").setEphemeral(true).queue();

            // Continue with the same behavior for the next message
            return Behaviors.same();
        });
    }

    protected static String getResponseMessage(Game game, User user) {
        return user.getName() + " added players to game: " + game.getName() + " - successful";
    }

    protected static void addExtraUsers(SlashCommandInteractionEvent event, Game game) {
        addExtraUser(event, game, Constants.PLAYER1);
        addExtraUser(event, game, Constants.PLAYER2);
        addExtraUser(event, game, Constants.PLAYER3);
        addExtraUser(event, game, Constants.PLAYER4);
        addExtraUser(event, game, Constants.PLAYER5);
        addExtraUser(event, game, Constants.PLAYER6);
        addExtraUser(event, game, Constants.PLAYER7);
        addExtraUser(event, game, Constants.PLAYER8);
    }

    private static void addExtraUser(SlashCommandInteractionEvent event, Game game, String playerID) {
        OptionMapping option;
        option = event.getOption(playerID);
        if (option != null){
            User extraUser = option.getAsUser();
            game.addPlayer(extraUser.getId(), extraUser.getName());
        }
    }
}
