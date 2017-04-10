package fi.ill.gremlin.commands;

import fi.ill.gremlin.Gremlin;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.restaction.ChannelAction;
import pw.haze.command.Command;
import pw.haze.command.utility.DigitClamp;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author haze
 * @since 4/1/2017
 * Note: if the description is empty, it wont send.
 */
public class General {

    private String encaseAndJoin(String[] arguments) {
        return Stream.of(arguments).map(x -> "(" + x + ")").collect(Collectors.joining());
    }

    @Command(value = {"help", "?", "h"}, desc = "shows the help for all or one command")
    public EmbedBuilder requestHelp(MessageReceivedEvent event, Optional<String> command) {
        if (command.isPresent()) {
            Optional<Method> method = Optional.empty();
            methods:
            for (Method m : Gremlin.getCommandManager().getContents().keySet())
                for (String s : m.getAnnotation(Command.class).value())
                    if (s.equalsIgnoreCase(command.get())) {
                        method = Optional.of(m);
                        break methods;
                    }
            if (!method.isPresent())
                return Gremlin.easyAuthor("Gremlin Help Desk", String.format("Command \"**%s**\" not found", command.get()));
            Command cmd = method.get().getAnnotation(Command.class);
            return Gremlin.easyAuthor(cmd.value()[0], String.format("```Markdown\n%s\nusage: %s```", cmd.desc(), Gremlin.getCommandManager().getUsage(method.get())));
        }
        final StringBuilder builder = new StringBuilder();
        Gremlin.getCommandManager().getContents().keySet().stream().forEach(m -> builder.append(String.format("%s - %s \n", m.getAnnotation(Command.class).value()[0], encaseAndJoin(Gremlin.getCommandManager().getUsage(m).split(" ")))));
        return Gremlin.easyAuthor(String.format("Commands [%s]", Gremlin.getCommandManager().getContents().size()), String.format("```Markdown\n%s```", builder.toString()));
    }

    @Command(value = {"purge", "remove"}, desc = "purge x amount of messages")
    public EmbedBuilder purgeMessages(MessageReceivedEvent event, @DigitClamp(min = 1, max = 100) Optional<Integer> amount) {
        if(amount.isPresent()) {
            event.getTextChannel().deleteMessages(event.getChannel().getHistoryAround(event.getMessage(), amount.get()).complete().getRetrievedHistory()).queue();
        } else {
            List<PermissionOverride> perms = event.getTextChannel().getRolePermissionOverrides();
            String name = event.getTextChannel().getName();
            event.getTextChannel().delete().queue();
            ChannelAction ac = event.getGuild().getController().createTextChannel(name);
            for(PermissionOverride perm: perms)
                ac = ac.addPermissionOverride(perm.getRole(), perm.getAllowedRaw(), perm.getDeniedRaw());
            ac.queue();
        }
        return Gremlin.emptyEmbed();
    }

}
