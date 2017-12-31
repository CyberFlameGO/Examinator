package github.scarsz.examinator.message;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import github.scarsz.examinator.manager.GuildConfigManager;
import github.scarsz.examinator.util.Emoji;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class HeaderMessage extends ReactiveMessage {

    public HeaderMessage(Examinator examinator, Message message, GuildConfigManager manager) {
        super(examinator, message, manager);
    }

    public static HeaderMessage send(Examinator examinator, GuildConfigManager manager, TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        //builder.setColor()
        builder.setThumbnail(manager.getGuild().getIconUrl());
        builder.setAuthor(manager.getGuild().getName());
        builder.setTitle("Examinator Configuration Panel");
        builder.setDescription("Owner: " + manager.getGuild().getOwner().getAsMention());
        builder.addField("Stats", String.join("\n", Arrays.asList(
                "**Exams (Total):** 69",
                "**Exams (Taken):** 420"
        )), true);
        builder.addField("Help", String.join("\n", Arrays.asList(
                Emoji.Pencil + " Create new exam",
                Emoji.Label + " Rename exam",
                Emoji.GreyExclamation + " Add trigger",
                Emoji.Exclamation + " Edit existing trigger",
                Emoji.HeavyPlusSign + " Add question",
                Emoji.HeavyMinusSign + " Edit existing question",
                Emoji.Grinning + " Add action",
                Emoji.NeutralFace + " Edit existing action",
                Emoji.AlarmClock + " Change cooldown",
                Emoji.X + " Delete exam",
                Emoji.WhiteCheckMark + " Confirm"
        )), true);
        Message message = channel.sendMessage(builder.build()).complete();
        HeaderMessage headerMessage = new HeaderMessage(examinator, message, manager);
        headerMessage.setCreateButton();
        return headerMessage;
    }

    public void setCreateButton() {
        message.clearReactions().queue(v -> message.addReaction(Emoji.Pencil).queue());
        examinator.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class, event -> {
            return !event.getUser().isBot() && event.getMessageId().equals(message.getId()) && event.getReactionEmote().getName().equals(Emoji.Pencil);
        }, event -> {
            new Exam(examinator, UUID.randomUUID(), manager.getGuild(), "New exam", 1000, 1400, null);
            message.getTextChannel().sendMessage("Created new exam").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            setCreateButton();
        });
    }

}
