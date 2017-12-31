package github.scarsz.examinator.exam.action;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import github.scarsz.examinator.exam.TestingSession;
import net.dv8tion.jda.core.entities.TextChannel;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class SendMessageToChannelAction extends Action {

    private final TextChannel channel;
    private final String message;

    public SendMessageToChannelAction(Examinator examinator, Exam exam, String channelId, String message) {
        super(examinator, exam);
        this.channel = examinator.getJda().getTextChannelById(channelId);
        this.message = message;
    }

    @Override
    public void execute(TestingSession session) {
        channel.sendMessage(message).queue();
    }

    @Override
    public JSONObject serialize() {
        return new JSONObject(new HashMap<String, Object>() {{
            put("type", Action.Type.SEND_MESSAGE_TO_CHANNEL);
            put("channelId", channel.getId());
            put("message", message);
        }});
    }

}
