package github.scarsz.examinator.exam.action;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import github.scarsz.examinator.exam.TestingSession;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class DecrementTriesRemainingAction extends Action {

    private String roleId;

    public DecrementTriesRemainingAction(Examinator examinator, Exam exam) {
        super(examinator, exam);
    }

    @Override
    public void execute(TestingSession session) {
        //TODO decrement tries
    }

    @Override
    public JSONObject serialize() {
        return new JSONObject(new HashMap<String, Object>() {{
            put("type", Action.Type.DECREMENT_TRIES_REMAINING);
        }});
    }

}
