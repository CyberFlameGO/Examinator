package github.scarsz.examinator.exam.question;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class TrueFalseQuestion extends Question {

    private boolean answer;

    public TrueFalseQuestion(Examinator examinator, Exam exam, String prompt, boolean answer) {
        super(examinator, exam, prompt);
        this.answer = answer;
    }

    public boolean grade(boolean answer) {
        return answer == this.answer;
    }

    @Override
    public Message send(TextChannel channel, int questionNumber) {
        Message message = super.send(channel, questionNumber);
        message.addReaction("✅").queue();
        message.addReaction("❌").queue();
        return message;
    }

    @Override
    public JSONObject serialize() {
        return new JSONObject(new HashMap<String, Object>() {{
            put("type", Question.Type.TRUE_FALSE);
            put("prompt", prompt);
            put("answer", answer);
        }});
    }

}
