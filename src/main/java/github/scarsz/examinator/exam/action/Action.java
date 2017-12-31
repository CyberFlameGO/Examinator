package github.scarsz.examinator.exam.action;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import github.scarsz.examinator.exam.TestingSession;
import org.json.simple.JSONObject;

public abstract class Action {

    public class Type {
        public static final int ADD_USER_TO_ROLE = 1;
        public static final int REMOVE_USER_FROM_ROLE = 2;
        public static final int DECREMENT_TRIES_REMAINING = 3;
        public static final int LOG_RESULT_TO_CHANNEL = 4;
        public static final int PM_CUSTOM_MESSAGE = 5;
        public static final int PM_RESULT = 6;
        public static final int SEND_MESSAGE_TO_CHANNEL = 7;
    }

    final Examinator examinator;
    final Exam exam;

    public Action(Examinator examinator, Exam exam) {
        this.examinator = examinator;
        this.exam = exam;
    }

    public abstract void execute(TestingSession session);
    public abstract JSONObject serialize();

}
