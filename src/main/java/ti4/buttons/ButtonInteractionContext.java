package ti4.buttons;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import ti4.map.Game;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.Guild;

public class ButtonInteractionContext {
    private final ButtonInteractionEvent event;
    private final Game game;
    private final User user;
    private final Guild guild;
    private final Channel channel;
    private final String customId;
    
    private final String handlerName;
    private final String attributes;

    public ButtonInteractionContext(ButtonInteractionEvent event, Game game, String handlerName, String attributes) {
        this.event = event;
        this.game = game;
        this.user = event.getUser();
        this.guild = event.getGuild();
        this.channel = event.getChannel();
        this.customId = event.getComponentId();
        this.attributes = attributes;
        this.handlerName = handlerName;
    }

    // Getters for each field
    public ButtonInteractionEvent getEvent() {
        return event;
    }

    public Game getGame() {
        return game;
    }

    public User getUser() {
        return user;
    }

    public Guild getGuild() {
        return guild;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getCustomId() {
        return customId;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public String getAttributes() {
        return attributes;
    }
}
