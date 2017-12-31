package github.scarsz.examinator.util;

import github.scarsz.examinator.Examinator;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DiscordUtil {

    public static char pullLetter(Examinator examinator, User user, Message targetMessage) {
        AtomicReference<Object> answer = new AtomicReference<>(null);
        AtomicBoolean timedOut = new AtomicBoolean(false);

        examinator.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class, event -> {
            boolean correctUser = event.getUser().equals(user);
            boolean sameMessage = event.getMessageId().equals(targetMessage.getId());
            boolean letterReaction = Character.toString(ReactionUtil.getLetterFromEmoji(event.getReactionEmote().getName())).matches("[A-Z]");
            return correctUser && sameMessage && letterReaction;
        }, event -> answer.set(ReactionUtil.getLetterFromEmoji(event.getReactionEmote().getName())), 1, TimeUnit.HOURS, () -> targetMessage.getTextChannel().sendMessage("Exam timed out. Deleting in one minute.").queue(message -> message.getTextChannel().delete().queueAfter(1, TimeUnit.MINUTES)));

        while (answer.get() == null && !timedOut.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (answer.get() == null) answer.set('z');

        return (char) answer.get();
    }

    public static boolean pullYesNo(Examinator examinator, User user, Message targetMessage) {
        AtomicReference<Object> answer = new AtomicReference<>(null);
        AtomicBoolean timedOut = new AtomicBoolean(false);

        examinator.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class, event -> {
            boolean correctUser = event.getUser().equals(user);
            boolean sameMessage = event.getMessageId().equals(targetMessage.getId());
            boolean checkOrX = event.getReactionEmote().getName().equals("✅") || event.getReactionEmote().getName().equals("❌");
            return correctUser && sameMessage && checkOrX;
        }, event -> answer.set(event.getReactionEmote().getName().equals("✅")), 1, TimeUnit.HOURS, () -> targetMessage.getTextChannel().sendMessage("Exam timed out. Deleting in one minute.").queue(message -> message.getTextChannel().delete().queueAfter(1, TimeUnit.MINUTES)));

        while (answer.get() == null && !timedOut.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (answer.get() == null) answer.set(false);

        return (boolean) answer.get();
    }

    public static void clear(TextChannel channel) {
        MessageHistory history = channel.getHistory();
        while (history.retrievePast(100).complete().size() > 0);
        Queue<Message> messages = new LinkedList<>();
        messages.addAll(history.getRetrievedHistory());

        try {
            List<Message> bulk = new ArrayList<>();
            while (bulk.size() < 100 && messages.size() > 0) bulk.add(messages.remove());
            channel.deleteMessages(bulk).queue();
        } catch (Exception e) {
            history.getRetrievedHistory().forEach(m -> {
                try { m.delete().queue(); } catch (Exception ignored) {}
            });
        }
    }

}
