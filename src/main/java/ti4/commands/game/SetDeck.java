package ti4.commands.game;

import java.util.*;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.collections4.CollectionUtils;
import ti4.generator.Mapper;
import ti4.helpers.Constants;
import ti4.map.Game;
import ti4.map.Player;
import ti4.message.MessageHelper;
import ti4.model.DeckModel;
import ti4.model.StrategyCardSetModel;

public class SetDeck extends GameSubcommandData {

    private final List<String> deckTypes;

    public SetDeck() {
        super(Constants.SET_DECK, "Change game card decks");
        deckTypes = new ArrayList<>();
        addDefaultOption(Constants.AC_DECK, "AC");
        addDefaultOption(Constants.SO_DECK, "SO");
        addDefaultOption(Constants.STAGE_1_PUBLIC_DECK, "Stage 1 public");
        addDefaultOption(Constants.STAGE_2_PUBLIC_DECK, "Stage 2 public");
        addDefaultOption(Constants.RELIC_DECK, "Relic");
        addDefaultOption(Constants.AGENDA_DECK, "Agenda");
        addDefaultOption(Constants.EVENT_DECK, "Event");
        addDefaultOption(Constants.EXPLORATION_DECKS, "Exploration");
        addDefaultOption(Constants.STRATEGY_CARD_SET, "Strategy card");
        addDefaultOption(Constants.TECHNOLOGY_DECK, "Technology");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Game activeGame = getActiveGame();

        Map<String, DeckModel> changedDecks = new HashMap<>();

        deckTypes.forEach(deckType -> {
            String value = event.getOption(deckType, null, OptionMapping::getAsString);
            if (Optional.ofNullable(value).isPresent()) {
                if (deckType.equals(Constants.STRATEGY_CARD_SET)) {
                    activeGame.setStrategyCardSet(value);
                } else {
                    DeckModel deckModel = Mapper.getDecks().get(value);
                    if (setDeck(event, activeGame, deckType, deckModel)) {
                        changedDecks.put(deckModel.getType(), deckModel);
                    } else {
                        MessageHelper.sendMessageToChannel(event.getChannel(),
                            "Something went wrong and the deck ***" + value + "*** could not be set, please see error above or try executing the command again (without copy/pasting).");
                    }
                }
                if (deckType.equals(Constants.TECHNOLOGY_DECK)) {
                    activeGame.swapOutVariantTechs();
                    activeGame.swapInVariantTechs();
                }
            }
        });

        if (CollectionUtils.isNotEmpty(changedDecks.keySet())) {
            List<String> changeMessage = new ArrayList<>();
            changedDecks.values().forEach(deck -> changeMessage.add(deck.getType() + " deck has been changed to:\n`" + deck.getAlias() + "`: " + deck.getName() + "\n>>> " + deck.getDescription()));
            MessageHelper.sendMessageToChannel(event.getChannel(), String.join("\n", changeMessage));
        }
    }

    private void addDefaultOption(String constantName, String descName) {
        addOptions(new OptionData(OptionType.STRING, constantName, descName + " deck").setRequired(false).setAutoComplete(true));
        deckTypes.add(constantName);
    }

    public static boolean setDeck(SlashCommandInteractionEvent event, Game activeGame, String deckType, DeckModel deckModel) {
        if (Optional.ofNullable(deckModel).isPresent()) {
            switch (deckType) {
                case Constants.AC_DECK -> {
                    return activeGame.validateAndSetActionCardDeck(event, deckModel);
                }
                case Constants.SO_DECK -> {
                    return activeGame.validateAndSetSecretObjectiveDeck(event, deckModel);
                }
                case Constants.STAGE_1_PUBLIC_DECK -> {
                    activeGame.setPublicObjectives1(new ArrayList<>(deckModel.getNewShuffledDeck()));
                    activeGame.setStage1PublicDeckID(deckModel.getAlias());
                    return true;
                }
                case Constants.STAGE_2_PUBLIC_DECK -> {
                    activeGame.setPublicObjectives2(new ArrayList<>(deckModel.getNewShuffledDeck()));
                    activeGame.setStage2PublicDeckID(deckModel.getAlias());
                    return true;
                }
                case Constants.RELIC_DECK -> {
                    return activeGame.validateAndSetRelicDeck(event, deckModel);
                }
                case Constants.AGENDA_DECK -> {
                    return activeGame.validateAndSetAgendaDeck(event, deckModel);
                }
                case Constants.EVENT_DECK -> {
                    return activeGame.validateAndSetEventDeck(event, deckModel);
                }
                case Constants.EXPLORATION_DECKS -> {
                    activeGame.setExploreDeck(new ArrayList<>(deckModel.getNewShuffledDeck()));
                    activeGame.setExplorationDeckID(deckModel.getAlias());
                    return true;
                }
                case Constants.TECHNOLOGY_DECK -> {
                    activeGame.setTechnologyDeckID(deckModel.getAlias());
                    if (deckModel.getAlias().contains("absol")) {
                        for (Player player : activeGame.getRealPlayers()) {
                            List<String> techs = new ArrayList<>();
                            techs.addAll(player.getTechs());
                            for (String tech : techs) {
                                if (!tech.contains("absol") && Mapper.getTech("absol_" + tech) != null) {
                                    if (!player.hasTech("absol_" + tech)) {
                                        player.addTech("absol_" + tech);
                                    }
                                    player.removeTech(tech);
                                }
                            }
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
