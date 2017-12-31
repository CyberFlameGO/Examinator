package github.scarsz.examinator.exam.question;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import github.scarsz.examinator.util.ReactionUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MultipleChoiceQuestion extends Question {

    private final Map<Character, String> choices;
    private final char answer;

    public MultipleChoiceQuestion(Examinator examinator, Exam exam, String prompt, Map<Character, String> choices, char answer) {
        super(examinator, exam, prompt);
        this.choices = choices;
        this.answer = answer;

        for (Map.Entry<Character, String> entry : choices.entrySet()) {
            int value = entry.getKey();
            if (97 > value || value > 122) {
                throw new IllegalArgumentException("Only lowercase letters are accepted keys for choices");
            }
        }
    }

    public boolean grade(char answer) {
        answer = String.valueOf(answer).toLowerCase().toCharArray()[0];
        return Character.valueOf(answer).equals(this.answer);
    }

    @Override
    public Message send(TextChannel channel, int questionNumber) {
        Message message = super.send(channel, questionNumber);

        List<String> messageLines = new LinkedList<>();
        for (Map.Entry<Character, String> entry : choices.entrySet()) {
            messageLines.add(":regional_indicator_" + entry.getKey() + ": " + entry.getValue());
            message.addReaction(ReactionUtil.getEmojiFromLetter(entry.getKey())).queue();
        }
        message.editMessage(message.getRawContent() + "\n\n" + String.join("\n", messageLines)).queue();

        return message;
    }

    @Override
    public JSONObject serialize() {
        return new JSONObject(new HashMap<String, Object>() {{
            put("type", Question.Type.MULTIPLE_CHOICE);
            put("prompt", prompt);
            put("answer", String.valueOf(answer));
            put("options", choices);
        }});
    }

}
