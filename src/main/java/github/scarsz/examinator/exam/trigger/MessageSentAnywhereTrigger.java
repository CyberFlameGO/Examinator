package github.scarsz.examinator.exam.trigger;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class MessageSentAnywhereTrigger extends Trigger {

    private String message;

    public MessageSentAnywhereTrigger(Examinator examinator, Exam exam, String message) {
        super(examinator, exam);
        this.message = message.toLowerCase();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (!exam.getGuild().equals(event.getGuild())) return;
        if (event.getAuthor().isBot()) return;

        if (event.getMessage().getContentRaw().toLowerCase().startsWith(message)) {
            exam.take(this, event.getChannel(), event.getMember());
        }
    }

    @Override
    public JSONObject serialize() {
        return new JSONObject(new HashMap<String, Object>() {{
            put("type", Trigger.Type.MESSAGE_SENT_ANYWHERE);
            put("message", message);
        }});
    }

}
