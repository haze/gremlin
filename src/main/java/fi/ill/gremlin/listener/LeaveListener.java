package fi.ill.gremlin.listener;

import fi.ill.gremlin.commands.Music;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.impl.GameImpl;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Optional;

/**
 * @author apteryx
 * @since 4/10/2017 - 16:52
 */
public class LeaveListener extends ListenerAdapter {
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (Music.following.isPresent()) {
            if (event.getMember() == Music.following.get()) {
                Music.following = Optional.empty();
                event.getJDA().getPresence().setGame(new GameImpl(null, null, Game.GameType.DEFAULT));
            }
        }
    }
}
