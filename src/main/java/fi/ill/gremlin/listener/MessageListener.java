package fi.ill.gremlin.listener;

import fi.ill.gremlin.Gremlin;
import javafx.util.Pair;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import pw.haze.command.CommandManager;

import java.awt.*;

/**
 * @author haze
 * @since 3/26/2017
 */
public class MessageListener extends ListenerAdapter {

    public static Color COMMAND_SUCCESS = new Color(163, 227, 170);
    public static Color COMMAND_FAILURE = new Color(227, 170, 163);

    public Color[] color = new Color[]{COMMAND_SUCCESS, COMMAND_FAILURE};

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            System.out.printf("{%s} [#%s] \"%s\" %s#%s: %s\n", event.getGuild().getName(), event.getChannel().getName(), event.getMember().getNickname(), event.getAuthor().getName(), event.getAuthor().getDiscriminator(), event.getMessage().getContent());
            Pair<Integer, EmbedBuilder> result = Gremlin.getCommandManager().execute(event.getMessage().getContent(), event);
            if (result.getValue() != null && !result.getValue().getDescriptionBuilder().toString().isEmpty()) {
                EmbedBuilder builder = result.getValue();
                builder.setColor(color[result.getKey()]);
                if (result.getKey() == CommandManager.SUCCESS)
                    event.getMessage().delete().queue();
                event.getChannel().sendMessage(builder.build()).queue();
            }
        }
    }
}
