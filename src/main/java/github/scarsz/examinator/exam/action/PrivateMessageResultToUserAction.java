package github.scarsz.examinator.exam.action;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import github.scarsz.examinator.exam.TestingSession;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class PrivateMessageResultToUserAction extends Action {

    public PrivateMessageResultToUserAction(Examinator examinator, Exam exam) {
        super(examinator, exam);
    }

    @Override
    public void execute(TestingSession session) {
        session.getMember().getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(session.asEmbed()).queue());
    }

    @Override
    public JSONObject serialize() {
        return new JSONObject(new HashMap<String, Object>() {{
            put("type", Action.Type.PM_RESULT);
        }});
    }

}
