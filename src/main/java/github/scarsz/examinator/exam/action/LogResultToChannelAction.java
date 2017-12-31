package github.scarsz.examinator.exam.action;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import github.scarsz.examinator.exam.TestingSession;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class LogResultToChannelAction extends Action {

    private String channelId;

    public LogResultToChannelAction(Examinator examinator, Exam exam, String channelId) {
        super(examinator, exam);
        this.channelId = channelId;
    }

    @Override
    public void execute(TestingSession session) {
        examinator.getJda().getTextChannelById(channelId).sendMessage(session.asEmbed()).queue();
    }

    @Override
    public JSONObject serialize() {
        return new JSONObject(new HashMap<String, Object>() {{
            put("type", Action.Type.LOG_RESULT_TO_CHANNEL);
            put("channelId", channelId);
        }});
    }

}
