package fi.ill.gremlin.commands;

import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import pw.haze.command.Command;

import java.util.Optional;
import java.util.Random;

/**
 * @author haze
 * @since 4/8/2017
 */
public class Fun {
    /*@Command("carousel")
    public void carousel(MessageReceivedEvent event, Optional<Integer> times) {
        if(event.getGuild().getVoiceChannels().parallelStream().filter(x -> x.getMembers().parallelStream().filter(z -> z.getUser() == event.getAuthor()).findAny().isPresent()).findAny().isPresent())
            new Thread(() -> {
                for (int i = 0; i < times.orElse(100); i++) {
                    try {
                        event.getGuild().getController().moveVoiceMember(event.getMember(), event.getGuild().getVoiceChannels().get(new Random().nextInt(event.getGuild().getVoiceChannels().size()))).complete();
                        Thread.sleep(250L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
    }*/
}
