package fi.ill.gremlin.listener;

import fi.ill.gremlin.audio.GuildMusicManager;
import fi.ill.gremlin.commands.Music;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * @author haze
 * @since 4/8/2017
 */
public class ChannelListener extends ListenerAdapter {
    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        GuildMusicManager manager = Music.musicManagerMap.get(event.getGuild().getId());
        if (manager != null) {
            if (manager.player.getPlayingTrack() != null && event.getChannelLeft().getMembers().size() == 1) {
                event.getGuild().getAudioManager().setSendingHandler(null);
                event.getGuild().getAudioManager().closeAudioConnection();
                System.out.println("soprano.");
            }

            if (Music.following.isPresent() && event.getMember() == Music.following.get()) {
                event.getGuild().getAudioManager().setSendingHandler(manager.sendHandler);
                event.getGuild().getAudioManager().openAudioConnection(event.getChannelJoined());
            }
        }
    }
}
