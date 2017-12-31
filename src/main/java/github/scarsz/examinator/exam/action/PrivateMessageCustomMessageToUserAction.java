package github.scarsz.examinator.exam.action;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import github.scarsz.examinator.exam.TestingSession;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class PrivateMessageCustomMessageToUserAction extends Action {

    private final String message;

    public PrivateMessageCustomMessageToUserAction(Examinator examinator, Exam exam, String message) {
        super(examinator, exam);
        this.message = message;
    }

    @Override
    public void execute(TestingSession session) {
        session.getMember().getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
    }

    @Override
    public JSONObject serialize() {
        return new JSONObject(new HashMap<String, Object>() {{
            put("type", Action.Type.PM_CUSTOM_MESSAGE);
            put("message", message);
        }});
    }

}
