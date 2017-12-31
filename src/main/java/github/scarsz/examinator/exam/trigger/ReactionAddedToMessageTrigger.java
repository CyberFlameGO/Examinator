package github.scarsz.examinator.exam.trigger;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class ReactionAddedToMessageTrigger extends Trigger {

    private String messageId;

    public ReactionAddedToMessageTrigger(Examinator examinator, Exam exam, String messageId) {
        super(examinator, exam);
        this.messageId = messageId.toLowerCase();
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (!exam.getGuild().equals(event.getGuild())) return;
        if (event.getUser().isBot()) return;
        if (!event.getMessageId().equals(messageId)) return;

        exam.take(this, event.getChannel(), event.getMember());
    }

    @Override
    public JSONObject serialize() {
        return new JSONObject(new HashMap<String, Object>() {{
            put("type", Type.REACTION_ADDED_TO_MESSAGE);
            put("messageId", messageId);
        }});
    }

}
