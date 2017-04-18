package fi.ill.gremlin.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import fi.ill.gremlin.commands.AuthoredAudioTrack;
import fi.ill.gremlin.commands.Music;
import fi.ill.gremlin.commands.SkipData;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author MinnDevelopment, DV8FromTheWorld, haze
 * @since 3/26/2017
 */
public class TrackScheduler extends AudioEventAdapter {
    final AudioPlayer player;
    public Queue<AuthoredAudioTrack> queue;
    public AuthoredAudioTrack current;
    public MessageReceivedEvent event;
    AudioTrack lastTrack;
    private boolean repeating = false;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player, MessageReceivedEvent event) {
        this.player = player;
        this.event = event;
        this.queue = new ConcurrentLinkedQueue<>();
    }


    public void queue(AuthoredAudioTrack info) {
        Music.getSkipMap().put(info, new SkipData(Music.GLOBAL_SKIPS_REQUIRED, new ArrayList<>()));
        Music.getSkipMap().entrySet().parallelStream().forEach(e -> System.out.printf("%s - %s\n", e.getKey(), e.getValue()));
        Boolean val = player.startTrack(info.getTrack(), true);
        if (!val) {
            queue.add(info);
        } else {
            this.current = info;
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        AuthoredAudioTrack data = queue.poll();
        if (data != null) {
            Music.getSkipMap().remove(this.current);
            player.startTrack(data.getTrack(), false);
            this.current = data;
        } else {
            this.current = null;
            player.startTrack(null, false);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            if (repeating)
                player.startTrack(lastTrack.makeClone(), false);
            else
                nextTrack();
            AudioTrack curTrack = player.getPlayingTrack();
            event.getChannel().sendMessage(new EmbedBuilder().setDescription(String.format("Now playing: **%s - %s** [%s]", curTrack.getInfo().title, curTrack.getInfo().author, Music.formatDuration(Duration.ofMillis(curTrack.getDuration())))).build());
        }

        if (endReason == AudioTrackEndReason.FINISHED && this.queue.isEmpty()) {
            event.getChannel().sendMessage(new EmbedBuilder().setDescription("Finished playing! Leaving...").build());
            event.getGuild().getAudioManager().setSendingHandler(null);
            event.getGuild().getAudioManager().closeAudioConnection();
        }

    }

    public boolean isRepeating() {
        return repeating;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    public void shuffle() {
        Collections.shuffle((List<?>) queue);
    }
}