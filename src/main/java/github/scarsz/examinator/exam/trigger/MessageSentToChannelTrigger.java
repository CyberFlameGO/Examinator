package github.scarsz.examinator.exam.trigger;

import github.scarsz.examinator.exam.Exam;
import github.scarsz.examinator.Examinator;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class MessageSentToChannelTrigger extends Trigger {

    private String channelId;
    private String message;

    public MessageSentToChannelTrigger(Examinator examinator, Exam exam, String channelId, String message) {
        super(examinator, exam);
        this.channelId = channelId;
        this.message = message.toLowerCase();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (!exam.getGuild().equals(event.getGuild())) return;
        if (event.getAuthor().isBot()) return;
        if (!event.getChannel().getId().equals(channelId)) return;

        if (event.getMessage().getContentRaw().toLowerCase().startsWith(message)) {
            exam.take(this, event.getChannel(), event.getMember());
        }
    }

    @Override
    public JSONObject serialize() {
        return new JSONObject(new HashMap<String, Object>() {{
            put("type", Type.MESSAGE_SENT_TO_CHANNEL);
            put("channelId", channelId);
            put("message", message);
        }});
    }

}
