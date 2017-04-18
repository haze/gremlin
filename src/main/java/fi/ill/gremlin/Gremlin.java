package fi.ill.gremlin;

import fi.ill.gremlin.commands.Fun;
import fi.ill.gremlin.commands.General;
import fi.ill.gremlin.commands.Music;
import fi.ill.gremlin.listener.ChannelListener;
import fi.ill.gremlin.listener.LeaveListener;
import fi.ill.gremlin.listener.MessageListener;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Invite;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import pw.haze.command.CommandManager;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Optional;

/**
 * @author haze
 * @since 3/26/2017
 */
public class Gremlin {

    public static final String PIXEL_LINK = "http://i.imgur.com/VYSxAyf.png";
    private static Optional<CommandManager> commandManager = Optional.empty();
    // TODO: Remove if needed
    private JDA jdaInstance = null;
    private Gremlin(String token, String secret, String yt_api) {

        Music.API_KEY = yt_api;

        getCommandManager().register(new Music());
        getCommandManager().register(new General());
        getCommandManager().register(new Fun());


        // unused??
        // final String clientSecret = botInfo.get().getKey();

        try {
            this.jdaInstance = new JDABuilder(AccountType.BOT)
                    .setToken(token)
                    .addListener(new MessageListener())
                    .addListener(new ChannelListener())
                    .addListener(new LeaveListener())
                    .addListener(new ListenerAdapter() {

                        @Override
                        public void onReady(ReadyEvent event) {
//                            event.getJDA().getGuilds().parallelStream().forEach(g -> System.out.printf("[%s] {%s} %s\n", g.getName(), g.getId(), g.getInvites().complete().parallelStream().map(i -> String.format("<%s> [%s]", i.getCode(), i.toString())).collect(Collectors.joining(", "))));
                            for (Guild g : event.getJDA().getGuilds()) {
                                System.out.println("\n" + g);
                                if (g.getMember(event.getJDA().getSelfUser()).hasPermission(Permission.MANAGE_SERVER)) {
                                    List<Invite> invites = g.getInvites().complete();
                                    if (invites.isEmpty())
                                        System.out.println(g.getPublicChannel().createInvite().complete().getCode());
                                    else
                                        System.out.println(invites);
                                } else if (g.getMember(event.getJDA().getSelfUser()).hasPermission(Permission.CREATE_INSTANT_INVITE)) {
                                    System.out.println(g.getPublicChannel().createInvite().complete().getCode());
                                } else {
                                    System.out.println("Permissions I have: " + g.getMember(event.getJDA().getSelfUser()).getPermissions());
                                }
                            }
                        }

                        @Override
                        public void onReconnect(ReconnectedEvent event) {
                            super.onReconnect(event);
                        }

                        @Override
                        public void onDisconnect(DisconnectEvent event) {
                            super.onDisconnect(event);
                        }
                    })
                    .buildAsync();
            System.out.println(this.jdaInstance.asBot().getInviteUrl());
        } catch (LoginException | RateLimitedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String... args) {
        if (args.length == 3) {
            new Gremlin(args[0], args[1], args[2]);
            return;
        }
        System.out.println("Missing necessary arguments, consult the documentation...");
        System.exit(1);
    }

    public static CommandManager getCommandManager() {
        if (!commandManager.isPresent())
            commandManager = Optional.of(new CommandManager(">"));
        return commandManager.get();
    }

    public static EmbedBuilder embedDesc(String x) {
        return new EmbedBuilder().setDescription(x);
    }

    public static EmbedBuilder emptyEmbed() {
        return embedDesc("");
    }

    public static EmbedBuilder easyAuthor(String header, String desc) {
        return new EmbedBuilder().setAuthor(header, "http://haze.pw/gremlin", PIXEL_LINK).setDescription(desc);
    }

}
