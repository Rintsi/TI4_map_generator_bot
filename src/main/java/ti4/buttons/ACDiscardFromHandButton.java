package ti4.buttons;

import ti4.AsyncTI4DiscordBot;
import ti4.commands.cardsac.ACInfo;
import ti4.commands.player.TurnStart;
import ti4.generator.Mapper;
import ti4.helpers.ButtonHelper;
import ti4.helpers.Constants;
import ti4.helpers.Emojis;
import ti4.helpers.Helper;
import ti4.map.Game;
import ti4.map.Player;
import ti4.message.BotLogger;
import ti4.message.MessageHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class ACDiscardFromHandButton {
    
    public static final String buttonId = "acDiscardFromHand";

    public static void register() {

        AsyncTI4DiscordBot.registerButtonHandler(
            ACDiscardFromHandButton.buttonId, 
            ACDiscardFromHandButton::buttonHandler
        );
    }

    public static void buttonHandler(ButtonInteractionContext context) {
        ButtonInteractionEvent event = context.getEvent();
        Game game = context.getGame();
        String attributes = context.getAttributes(); // this would contain 123
        Player player = game.getPlayer(event.getUser().getId());

        String acIndex = attributes;
        boolean stalling = false;
        if (acIndex.contains("stall")) {
            acIndex = acIndex.replace("stall", "");
            stalling = true;
        }

        MessageChannel channel = game.getMainGameChannel();

        if (channel != null) {
            try {
                String acID = null;
                for (Map.Entry<String, Integer> so : player.getActionCards().entrySet()) {
                    if (so.getValue().equals(Integer.parseInt(acIndex))) {
                        acID = so.getKey();
                    }
                }

                boolean removed = game.discardActionCard(player.getUserID(), Integer.parseInt(acIndex));
                if (!removed) {
                    MessageHelper.sendMessageToChannel(event.getChannel(),
                        "No such Action Card ID found, please retry");
                    return;
                }
                String sb = "Player: " + player.getUserName() + " - " +
                    "Discarded Action Card:" + "\n" +
                    Mapper.getActionCard(acID).getRepresentation() + "\n";
                MessageChannel channel2 = game.getMainGameChannel();
                if (game.isFoWMode()) {
                    channel2 = player.getPrivateChannel();
                }
                MessageHelper.sendMessageToChannel(channel2, sb);
                ACInfo.sendActionCardInfo(game, player);
                String message = "Use buttons to end turn or do another action.";
                if (stalling) {
                    String message3 = "Use buttons to drop a mech on a planet or decline";
                    List<Button> buttons = new ArrayList<>(Helper.getPlanetPlaceUnitButtons(player, game,
                        "mech", "placeOneNDone_skipbuild"));
                    buttons.add(Button.danger("deleteButtons", "Decline to drop Mech"));
                    MessageHelper.sendMessageToChannelWithButtons(channel2, message3, buttons);
                    List<Button> systemButtons = TurnStart.getStartOfTurnButtons(player, game, true, event);
                    MessageHelper.sendMessageToChannelWithButtons(channel2, message, systemButtons);
                }
                ButtonHelper.checkACLimit(game, event, player);
                event.getMessage().delete().queue();
                if (player.hasUnexhaustedLeader("cymiaeagent")) {
                    List<Button> buttons2 = new ArrayList<>();
                    Button hacanButton = Button
                        .secondary("exhaustAgent_cymiaeagent_" + player.getFaction(), "Use Cymiae Agent")
                        .withEmoji(Emoji.fromFormatted(Emojis.cymiae));
                    buttons2.add(hacanButton);
                    MessageHelper.sendMessageToChannelWithButtons(
                        ButtonHelper.getCorrectChannel(player, game),
                        player.getRepresentation(true, true)
                            + " you can use Cymiae agent to make yourself draw an AC",
                        buttons2);
                }

                if ("Action".equalsIgnoreCase(Mapper.getActionCard(acID).getWindow())) {

                    for (Player p2 : game.getRealPlayers()) {
                        if (p2 == player) {
                            continue;
                        }
                        if (p2.getActionCards().containsKey("reverse_engineer")
                            && !ButtonHelper.isPlayerElected(game, player, "censure")
                            && !ButtonHelper.isPlayerElected(game, player, "absol_censure")) {
                            List<Button> reverseButtons = new ArrayList<>();
                            String key = "reverse_engineer";
                            String ac_name = Mapper.getActionCard(key).getName();
                            if (ac_name != null) {
                                reverseButtons.add(Button.success(
                                    Constants.AC_PLAY_FROM_HAND + p2.getActionCards().get(key)
                                        + "_reverse_" + Mapper.getActionCard(acID).getName(),
                                    "Reverse engineer " + Mapper.getActionCard(acID).getName()));
                            }
                            reverseButtons.add(Button.danger("deleteButtons", "Decline"));
                            String cyberMessage = "" + p2.getRepresentation(true, true)
                                + " reminder that you can use reverse engineer on "
                                + Mapper.getActionCard(acID).getName();
                            MessageHelper.sendMessageToChannelWithButtons(p2.getCardsInfoThread(),
                                cyberMessage, reverseButtons);
                        }
                    }
                }

            } catch (Exception e) {
                BotLogger.log(event, "Something went wrong discarding", e);
            }
        } else {
            event.getChannel().sendMessage("Could not find channel to play card. Please ping Bothelper.").queue();
        }

    }
}
