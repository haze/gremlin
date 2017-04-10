package fi.ill.gremlin;

import fi.ill.gremlin.commands.Fun;
import fi.ill.gremlin.commands.General;
import fi.ill.gremlin.commands.Music;
import fi.ill.gremlin.listener.ChannelListener;
import fi.ill.gremlin.listener.MessageListener;
import javafx.util.Pair;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import pw.haze.command.CommandManager;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * @author haze
 * @since 3/26/2017
 */
public class Gremlin {

    private static final String PIXEL_LINK = "http://i.imgur.com/VYSxAyf.png";

    public static void main(String... args) { new Gremlin(); }
    // TODO: Remove if needed
    public JDA jdaInstance = null;
    private static Optional<CommandManager> commandManager = Optional.empty();

    public static CommandManager getCommandManager() {
        if(!commandManager.isPresent())
            commandManager = Optional.of(new CommandManager(">"));
        return commandManager.get();
    }

    public static EmbedBuilder embedDesc(String x) { return new EmbedBuilder().setDescription(x); }

    public static EmbedBuilder emptyEmbed() { return embedDesc(""); }

    public static EmbedBuilder easyAuthor(String header, String desc) {
        return new EmbedBuilder().setAuthor(header, "http://haze.pw/gremlin", PIXEL_LINK).setDescription(desc);
    }

    private Pair<String, String> parseUserInfo() throws IOException {
        final Properties prop = new Properties();
        prop.load(new FileInputStream(new File("src/main/resources/ident.properties")));
        return new Pair<>(prop.getProperty("CLIENT_SECRET"), prop.getProperty("CLIENT_TOKEN"));
    }

    private Gremlin() {

        getCommandManager().register(new Music());
        getCommandManager().register(new General());
        getCommandManager().register(new Fun());

        Optional<Pair<String, String>> botInfo = Optional.empty();
        try {
            botInfo = Optional.of(parseUserInfo());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        final String clientToken = botInfo.get().getValue();
        // unused??
        // final String clientSecret = botInfo.get().getKey();

        try {
            this.jdaInstance = new JDABuilder(AccountType.BOT)
                    .setToken(clientToken)
                    .addListener(new MessageListener())
                    .addListener(new ChannelListener())
                    .buildAsync();
            System.out.println(this.jdaInstance.asBot().getInviteUrl());
        } catch (LoginException | RateLimitedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
