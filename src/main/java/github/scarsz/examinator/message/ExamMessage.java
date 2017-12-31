package github.scarsz.examinator.message;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import github.scarsz.examinator.manager.GuildConfigManager;
import github.scarsz.examinator.util.Emoji;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.requests.RestAction;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * It's horrible. I know it's horrible. Sorry.
 */
public class ExamMessage extends ReactiveMessage {

    private static final List<String> emojis = new LinkedList<>(Arrays.asList(Emoji.Label, Emoji.GreyExclamation, Emoji.Exclamation, Emoji.HeavyPlusSign, Emoji.HeavyMinusSign, Emoji.Grinning, Emoji.NeutralFace, Emoji.AlarmClock, Emoji.X));
    private final Exam exam;

    public ExamMessage(Examinator examinator, Message message, GuildConfigManager manager, Exam exam) {
        super(examinator, message, manager);
        this.exam = exam;

        setName();
        setAddTrigger();
        setEditTrigger();
        setAddQuestion();
        setEditQuestion();
        setAddAction();
        setEditAction();
        setCooldown();
        setDelete();
    }

    public void destroy() {
        exam.destroy();
    }

    public void setName() {
        examinator.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class, event -> {
            return !event.getUser().isBot() && event.getMessageId().equals(message.getId()) && event.getReactionEmote().getName().equals(Emoji.Label);
        }, event -> {
            Message promptMessage = manager.getChannel().sendMessage("Type the new name for exam `" + exam.getName() + "`").complete();
            manager.setExpectingMessage(true);
            setName2(promptMessage);
        });
    }
    public void setName2(Message promptMessage) {
        examinator.getWaiter().waitForEvent(GuildMessageReceivedEvent.class, event -> {
            if (event.getAuthor().isBot() || !event.getChannel().equals(manager.getChannel())) return false;
            String strippedMessage = event.getMessage().getContentRaw().replaceAll("[^A-Za-z0-9 ]", "");
            if (strippedMessage.length() < 5) {
                event.getChannel().sendMessage(":x: ***__The new name needs to be at least 5 characters long__***").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
                return false;
            }
            return true;
        }, event -> {
            String strippedMessage = event.getMessage().getContentRaw().replaceAll("[^A-Za-z0-9 ]", "");
            exam.setName(strippedMessage);
            manager.getChannel().sendMessage("Set name to `" + exam.getName() + "`").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            event.getMessage().delete().queue();

            promptMessage.delete().queue();
            manager.setExpectingMessage(false);
            reset();
            setName();
        }, 30, TimeUnit.SECONDS, () -> {
            promptMessage.delete().queue();
            manager.setExpectingMessage(false);
            reset();
            setName();
        });
    }
    public void setAddTrigger() {
        examinator.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class, event -> {
            return !event.getUser().isBot() && event.getMessageId().equals(message.getId()) && event.getReactionEmote().getName().equals(Emoji.GreyExclamation);
        }, event -> {

        });
    }
    public void setEditTrigger() {
        examinator.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class, event -> {
            return !event.getUser().isBot() && event.getMessageId().equals(message.getId()) && event.getReactionEmote().getName().equals(Emoji.Exclamation);
        }, event -> {

        });
    }
    public void setAddQuestion() {
        examinator.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class, event -> {
            return !event.getUser().isBot() && event.getMessageId().equals(message.getId()) && event.getReactionEmote().getName().equals(Emoji.HeavyPlusSign);
        }, event -> {

        });
    }
    public void setEditQuestion() {
        examinator.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class, event -> {
            return !event.getUser().isBot() && event.getMessageId().equals(message.getId()) && event.getReactionEmote().getName().equals(Emoji.HeavyMinusSign);
        }, event -> {

        });
    }
    public void setAddAction() {
        examinator.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class, event -> {
            return !event.getUser().isBot() && event.getMessageId().equals(message.getId()) && event.getReactionEmote().getName().equals(Emoji.Grinning);
        }, event -> {

        });
    }
    public void setEditAction() {
        examinator.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class, event -> {
            return !event.getUser().isBot() && event.getMessageId().equals(message.getId()) && event.getReactionEmote().getName().equals(Emoji.NeutralFace);
        }, event -> {

        });
    }
    public void setCooldown() {
        examinator.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class, event -> {
            return !event.getUser().isBot() && event.getMessageId().equals(message.getId()) && event.getReactionEmote().getName().equals(Emoji.AlarmClock);
        }, event -> {
            Message promptMessage = manager.getChannel().sendMessage("Type the new amount of seconds between attempts for exam `" + exam.getName() + "`. Currently: `" + exam.getFailureCooldown() + "`").complete();
            manager.setExpectingMessage(true);
            setCooldown2(promptMessage);
        });
    }
    public void setCooldown2(Message promptMessage) {
        examinator.getWaiter().waitForEvent(GuildMessageReceivedEvent.class, event -> {
            if (event.getAuthor().isBot() || !event.getChannel().equals(manager.getChannel())) return false;
            if (!StringUtils.isNumeric(event.getMessage().getContentRaw())) {
                event.getChannel().sendMessage(":x: ***__Your message needs to be completely numeric__***").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                event.getMessage().delete().queue();
                return false;
            }
            return true;
        }, event -> {
            int newCooldown = Integer.parseInt(event.getMessage().getContentRaw());
            exam.setFailureCooldown(newCooldown);
            manager.getChannel().sendMessage("Set failure cooldown to " + newCooldown + " seconds").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            event.getMessage().delete().queue();

            promptMessage.delete().queue();
            manager.setExpectingMessage(false);
            reset();
            setCooldown();
        }, 30, TimeUnit.SECONDS, () -> {
            promptMessage.delete().queue();
            manager.setExpectingMessage(false);
            reset();
            setCooldown();
        });
    }
    public void setDelete() {
        examinator.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class, event -> {
            return !event.getUser().isBot() && event.getMessageId().equals(message.getId()) && event.getReactionEmote().getName().equals(Emoji.X);
        }, event -> {
            message.addReaction(Emoji.WhiteCheckMark).queue();
            setDelete2();
        });
    }
    public void setDelete2() {
        examinator.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class, event -> {
            return !event.getUser().isBot() && event.getMessageId().equals(message.getId()) && event.getReactionEmote().getName().equals(Emoji.WhiteCheckMark);
        }, event2 -> {
            destroy();
            message.delete().queue();
        }, 5, TimeUnit.SECONDS, () -> {
            reset();
            setDelete();
        });
    }

    public void reset() {
        message.editMessage("**Exam:** " + exam.getName()).queue();
        message.clearReactions().queue(v -> emojis.stream().map(message::addReaction).forEach(RestAction::complete));
    }

    public Exam getExam() {
        return exam;
    }

    public static ExamMessage send(Examinator examinator, GuildConfigManager manager, Exam exam) {
        Message message = manager.getChannel().sendMessage("**Exam: **" + exam.getName()).complete();
        emojis.stream().map(message::addReaction).forEach(RestAction::queue);
        return new ExamMessage(examinator, message, manager, exam);
    }

}
