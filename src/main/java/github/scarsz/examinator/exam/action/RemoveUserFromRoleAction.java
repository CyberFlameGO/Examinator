package github.scarsz.examinator.exam.action;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import github.scarsz.examinator.exam.ExamResult;
import github.scarsz.examinator.exam.TestingSession;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class RemoveUserFromRoleAction extends Action {

    private String roleId;

    public RemoveUserFromRoleAction(Examinator examinator, Exam exam, String roleId) {
        super(examinator, exam);
        this.roleId = roleId;
    }

    @Override
    public void execute(TestingSession session) {
        //TODO differentiate action based on result
        if (session.getResult() == ExamResult.FAIL) session.getExam().getGuild().getController().removeRolesFromMember(session.getMember(), examinator.getJda().getRoleById(roleId)).queue();
    }

    @Override
    public JSONObject serialize() {
        return new JSONObject(new HashMap<String, Object>() {{
            put("type", Action.Type.REMOVE_USER_FROM_ROLE);
            put("roleId", roleId);
        }});
    }

}
