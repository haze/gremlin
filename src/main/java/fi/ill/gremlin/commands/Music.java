package fi.ill.gremlin.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fi.ill.gremlin.Gremlin;
import fi.ill.gremlin.audio.GuildMusicManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.GameImpl;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pw.haze.command.Command;
import pw.haze.command.utility.DigitClamp;
import pw.haze.command.utility.LengthClamp;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * @author haze
 * @since 3/26/2017
 */
public class Music {

    public static final Map<String, GuildMusicManager> musicManagerMap = new HashMap<>();
    public static String API_KEY = "n/a";
    public static Optional<Member> following = Optional.empty();
    private static Integer volume = 15;
    private AudioPlayerManager audioPlayerManager;

    public Music() {
        audioPlayerManager = new DefaultAudioPlayerManager();
        audioPlayerManager.registerSourceManager(new YoutubeAudioSourceManager());
        audioPlayerManager.registerSourceManager(new SoundCloudAudioSourceManager());
        audioPlayerManager.registerSourceManager(new VimeoAudioSourceManager());
        audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
        audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        audioPlayerManager.registerSourceManager(new HttpAudioSourceManager());
        audioPlayerManager.registerSourceManager(new LocalAudioSourceManager());
    }

    // thanks, Jon Skeet (http://stackoverflow.com/a/266846)
    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }

    public Map<String, GuildMusicManager> getMusicManagerMap() {
        return musicManagerMap;
    }

    @Command(value = {"volume", "vol", "v"}, desc = "set the volume for the music bot")
    public EmbedBuilder setVolume(MessageReceivedEvent event, @DigitClamp(max = 100F) Optional<Integer> newVolume) {
        if (newVolume.isPresent()) {
            Music.volume = newVolume.get();
            GuildMusicManager man = musicManagerMap.get(event.getGuild().getId());
            if (man != null)
                man.player.setVolume(Music.volume);
        }
        return new EmbedBuilder().setDescription(String.format("%s %s%%", newVolume.isPresent() ? "Set volume to" : "Current volume is", new DecimalFormat("###.#")
                .format(Music.volume)));
    }

    @Command(value = {"follow"}, desc = "set the music bot to follow  you")
    public EmbedBuilder setFollow(MessageReceivedEvent event) {
        if (following.isPresent()) {
            if (following.get() == event.getMember()) {
                following = Optional.empty();
                event.getJDA().getPresence().setGame(new GameImpl(null, null, Game.GameType.DEFAULT));
                return Gremlin.embedDesc("I've stopped following " + event.getAuthor().getAsMention() + ".");
            }
            return Gremlin.embedDesc(String.format("Already following %s! I can only follow you once they tell me to stop following them.", following.get().getAsMention()));
        } else {
            following = Optional.of(event.getMember());
            event.getJDA().getPresence().setGame(new GameImpl("Following " + following.get().getNickname(), null, Game.GameType.TWITCH));
            return Gremlin.embedDesc("I've started following " + following.get().getAsMention() + ".");
        }
    }

    @Command(value = {"play", "q", "p"}, desc = "music ultra-command. see p (help) for more.")
    public EmbedBuilder playTrack(MessageReceivedEvent event, @LengthClamp() Optional<String> track) {
        if (!musicManagerMap.containsKey(event.getGuild().getId())) {
            GuildMusicManager temp = new GuildMusicManager(this.audioPlayerManager, event);
            temp.player.setVolume(Math.round(volume));
            musicManagerMap.put(event.getGuild().getId(), new GuildMusicManager(this.audioPlayerManager, event));
        }

        GuildMusicManager guildMusicManager = musicManagerMap.get(event.getGuild().getId());

        if (track.isPresent()) {
            if (!event.getMember().getVoiceState().inVoiceChannel())
                return new EmbedBuilder().setDescription("You are not in a voice channel.");
            VoiceChannel vc = event.getMember().getVoiceState().getChannel();
            AudioManager am = event.getGuild().getAudioManager();
            switch (track.get().toLowerCase()) {
                case "pause": {
                    boolean pauseState = guildMusicManager.player.isPaused();
                    if (pauseState)
                        return Gremlin.embedDesc("Already paused!");
                    else {
                        guildMusicManager.player.setPaused(true);
                        return Gremlin.embedDesc("Player is now paused.");
                    }
                }
                case "queue": {
                    final StringBuilder builder = new StringBuilder();
                    final AudioTrack[] queue = guildMusicManager.scheduler.queue.toArray(new AudioTrack[]{});
                    IntStream.range(0, guildMusicManager.scheduler.queue.size()).forEach(ind -> {
                        AudioTrack tr = queue[ind];
                        builder.append(String.format("[%s] **%s** - [%s]\n", ind + 1, tr.getInfo().title, formatDuration(Duration.ofMillis(tr.getDuration()))));
                    });
                    return Gremlin.easyAuthor(String.format("Queue [%s]", guildMusicManager.scheduler.queue.size()), String.format("%s", guildMusicManager.scheduler.queue.size() == 0 ? "Empty" : builder.toString()));
                }
                case "stop": {
                    guildMusicManager.player.stopTrack();
                    return Gremlin.embedDesc("Stopped playing.");
                }
                case "join": {
                    am.setSendingHandler(guildMusicManager.sendHandler);
                    am.openAudioConnection(vc);
                    return Gremlin.emptyEmbed();
                }
                case "clear": {
                    guildMusicManager.scheduler.queue.clear();
                    return Gremlin.embedDesc("Cleared queue!");
                }
                case "help": {
                    return Gremlin.easyAuthor("Sub-Commands (7)", "queue - show queue\nrepeat - toggle repeat\nclear - clear the queue\npause - pause player\nunpause - unpause player\nleave - leave voice channel\nhelp - shows this message\nskip - skip the current song");
                }
                case "repeat": {
                    guildMusicManager.scheduler.setRepeating(!guildMusicManager.scheduler.isRepeating());
                    return Gremlin.embedDesc(String.format("Repeat %s", guildMusicManager.scheduler.isRepeating() ? "on" : "off"));
                }
                case "skip": {
                    guildMusicManager.scheduler.nextTrack();
                    return Gremlin.emptyEmbed();
                }
                case "leave": {
                    am.setSendingHandler(null);
                    am.closeAudioConnection();
                    return Gremlin.emptyEmbed();
                }
                case "unpause":
                case "resume": {
                    boolean pauseState = guildMusicManager.player.isPaused();
                    if (!pauseState)
                        return Gremlin.embedDesc("Already playing!");
                    else {
                        guildMusicManager.player.setPaused(false);
                        return Gremlin.embedDesc("Player resumed.");
                    }
                }
                default: {
                    if (track.get().contains(" ") || !track.get().startsWith("http")) {
                        final String query = track.get().replaceAll(" ", "+");
                        final String url = String.format("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&order=relevance&q=%s&key=%s", query, API_KEY);
                        System.out.println(url);
                        Document doc = null;
                        try {
                            doc = Jsoup.connect(url).ignoreContentType(true).timeout(5 * 1000).get();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String getJson = doc.text();
                        JSONObject jsonObject = (JSONObject) new JSONTokener(getJson).nextValue();
                        JSONArray items = jsonObject.getJSONArray("items");
                        String id;
                        try {
                            id = items.getJSONObject(0).getJSONObject("id").getString("videoId");
                        } catch (JSONException e) {
                            return Gremlin.embedDesc("No results found for \"" + track.get() + "\".");
                        }
                        load(vc, am, event, guildMusicManager, String.format("https://youtube.com/watch?v=%s", id));
                        return new EmbedBuilder();
                    }

                }
            }
            load(vc, am, event, guildMusicManager, track.get());
            return new EmbedBuilder();
        } else
            return new EmbedBuilder().setAuthor("Now Playing...", guildMusicManager.player.getPlayingTrack().getInfo().uri, Gremlin.PIXEL_LINK).setDescription(guildMusicManager.player.getPlayingTrack() != null ? String.format("%s - [%s/%s]", guildMusicManager.player.getPlayingTrack().getInfo().title, Music.formatDuration(Duration.ofMillis(guildMusicManager.player.getPlayingTrack().getPosition())), Music.formatDuration(Duration.ofMillis(guildMusicManager.player.getPlayingTrack().getDuration()))) : "None");
    }

    private void load(VoiceChannel vc, AudioManager am, MessageReceivedEvent event, GuildMusicManager guildMusicManager, String track_url) {
        audioPlayerManager.loadItemOrdered(guildMusicManager, track_url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                if (guildMusicManager.scheduler.queue.isEmpty() && guildMusicManager.player.getPlayingTrack() == null) {
                    event.getChannel().sendMessage(new EmbedBuilder().setDescription(String.format("Loaded track **%s - %s** [%s]", audioTrack.getInfo().title, audioTrack.getInfo().author, formatDuration(Duration.ofMillis(audioTrack.getDuration())))).build()).queue();
                } else {
                    event.getChannel().sendMessage(new EmbedBuilder().setDescription(String.format("Queued track **%s - %s** [%s]", audioTrack.getInfo().title, audioTrack.getInfo().author, formatDuration(Duration.ofMillis(audioTrack.getDuration())))).build()).queue();
                }
                guildMusicManager.scheduler.queue(audioTrack);
                am.setSendingHandler(guildMusicManager.sendHandler);
                am.openAudioConnection(vc);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                audioPlaylist.getTracks().forEach(guildMusicManager.scheduler::queue);
                event.getChannel().sendMessage(new EmbedBuilder().setDescription(String.format("Loaded playlist of %s items.", audioPlaylist.getTracks().size())).build()).queue();
                am.setSendingHandler(guildMusicManager.sendHandler);
                am.openAudioConnection(vc);
            }

            @Override
            public void noMatches() {
                event.getChannel().sendMessage(new EmbedBuilder().setDescription("No matches for track description.").build()).queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                event.getChannel().sendMessage(new EmbedBuilder().setDescription("Failed to load track. Skipping...").build()).queue();
            }
        });
    }

}
