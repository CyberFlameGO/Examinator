package github.scarsz.examinator.exam.trigger;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.json.simple.JSONObject;

public abstract class Trigger extends ListenerAdapter {

    public class Type {
        public static final int MESSAGE_SENT_ANYWHERE = 1;
        public static final int MESSAGE_SENT_TO_CHANNEL = 2;
        public static final int REACTION_ADDED_TO_MESSAGE = 3;
        public static final int SPECIFIC_REACTION_ADDED_TO_MESSAGE = 4;
    }

    final Examinator examinator;
    final Exam exam;

    public Trigger(Examinator examinator, Exam exam) {
        this.examinator = examinator;
        this.exam = exam;

        examinator.getJda().addEventListener(this);
    }

    public void destroy() {
        examinator.getJda().removeEventListener(this);
    }
    public abstract JSONObject serialize();

}
