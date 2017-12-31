package github.scarsz.examinator.exam.trigger;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class SpecificReactionAddedToMessageTrigger extends Trigger {

    private String reaction;
    private String messageId;

    public SpecificReactionAddedToMessageTrigger(Examinator examinator, Exam exam, String reaction, String messageId) {
        super(examinator, exam);
        this.reaction = reaction;
        this.messageId = messageId;
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (!exam.getGuild().equals(event.getGuild())) return;
        if (event.getUser().isBot()) return;

        if (event.getReactionEmote().getName().equals(reaction) && event.getMessageId().equals(messageId)) {
            event.getChannel().getMessageById(messageId).queue(message -> message.clearReactions().queue());
            exam.take(this, event.getChannel(), event.getMember());
        }
    }

    @Override
    public void onGuildMessageReactionRemoveAll(GuildMessageReactionRemoveAllEvent event) {
        if (!event.getMessageId().equals(messageId)) return;
        event.getChannel().getMessageById(messageId).queue(message -> message.addReaction(reaction).queue());
    }

    @Override
    public JSONObject serialize() {
        return new JSONObject(new HashMap<String, Object>() {{
            put("type", Type.SPECIFIC_REACTION_ADDED_TO_MESSAGE);
            put("reaction", reaction);
            put("messageId", messageId);
        }});
    }

}
