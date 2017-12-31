package github.scarsz.examinator.exam.question;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.json.simple.JSONObject;

public abstract class Question {

    public class Type {
        public static final int MULTIPLE_CHOICE = 1;
        public static final int TRUE_FALSE = 2;
    }

    final Examinator examinator;
    final Exam exam;
    String prompt;

    public Question(Examinator examinator, Exam exam, String prompt) {
        this.examinator = examinator;
        this.exam = exam;
        this.prompt = prompt;
    }

    /**
     * Grade the given answer with the correct one
     *
     * @return true if answer was correct, false otherwise
     */
    public boolean grade() {
        throw new AbstractMethodError();
    }

    /**
     * Send this question as a prompt to the given text channel with the given question number
     * @param channel {@link TextChannel} to send the message to
     * @param questionNumber Number in the exam this question is at
     * @return The sent {@link Message}
     */
    public Message send(TextChannel channel, int questionNumber) {
        return channel.sendMessage("**__Question " + questionNumber + "__**: " + prompt).complete();
    }

    public abstract JSONObject serialize();

}
