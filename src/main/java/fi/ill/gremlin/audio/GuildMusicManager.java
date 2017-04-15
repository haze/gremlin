package fi.ill.gremlin.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * @author DV8FromTheWorld
 */
public class GuildMusicManager {
    /**
     * Audio player for the guild.
     */
    public final AudioPlayer player;
    /**
     * Track scheduler for the player.
     */
    public final TrackScheduler scheduler;
    /**
     * Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    public final AudioPlayerSendHandler sendHandler;

    /**
     * Creates a player and a track scheduler.
     *
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(AudioPlayerManager manager, MessageReceivedEvent ev) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player, ev);
        sendHandler = new AudioPlayerSendHandler(player);
        player.addListener(scheduler);
    }
}