package fi.ill.gremlin.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Member;


/**
 * @author haze
 * @since 4/17/2017
 */
public class AuthoredAudioTrack {
    private AudioTrack track;
    private Member author;

    public AuthoredAudioTrack(AudioTrack track, Member author) {

        this.track = track;
        this.author = author;
    }

    public AudioTrack getTrack() {
        return track;
    }

    public Member getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return "AuthoredAudioTrack{" +
                "track=" + track +
                ", author=" + author +
                '}';
    }
}
