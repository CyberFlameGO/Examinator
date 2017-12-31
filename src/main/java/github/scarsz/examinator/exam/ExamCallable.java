package github.scarsz.examinator.exam;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.question.MultipleChoiceQuestion;
import github.scarsz.examinator.exam.question.Question;
import github.scarsz.examinator.exam.question.TrueFalseQuestion;
import github.scarsz.examinator.exam.trigger.Trigger;
import github.scarsz.examinator.util.DiscordUtil;
import github.scarsz.examinator.util.TimeUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ExamCallable implements Callable<Object> {

    private final Examinator examinator;
    private final Exam exam;
    private final Trigger trigger;
    private final TextChannel sourceChannel;
    private final Member member;

    private Message examChannelMessage;
    private TestingSession session;
    private TextChannel targetChannel;

    public ExamCallable(Examinator examinator, Exam exam, Trigger trigger, TextChannel sourceChannel, Member member) {
        this.examinator = examinator;
        this.exam = exam;
        this.trigger = trigger;
        this.sourceChannel = sourceChannel;
        this.member = member;

        session = new TestingSession(examinator, exam, member);
        examinator.getExamPool().getActiveExams().add(this);
    }

    @Override
    public Object call() {
        try {
            List<Category> possibleCategories = exam.getGuild().getCategoriesByName("Exams", true);
            if (possibleCategories.size() == 0) possibleCategories.add((Category) exam.getGuild().getController().createCategory("Exams").complete());
            if (possibleCategories.size() == 0) return null;
            Category targetCategory = possibleCategories.get(0);

            targetChannel = (TextChannel) exam.getGuild().getController().createTextChannel(member.getUser().getId() + "-" + System.currentTimeMillis())
                    .addPermissionOverride(exam.getGuild().getPublicRole(), Collections.emptyList(), Collections.singleton(Permission.MESSAGE_READ))
                    .addPermissionOverride(member, Arrays.asList(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION), null)
                    .setParent(exam.getGuild().getCategoriesByName("Exams", true).get(0)) //TODO: customize category name
                    .complete();
            examChannelMessage = sourceChannel.sendMessage(member.getAsMention() + ", your exam channel is " + targetChannel.getAsMention() + ".").complete();
            examChannelMessage.delete().queueAfter(1, TimeUnit.MINUTES);
        } catch (Exception e) {
            exam.getGuild().getOwner().getUser().openPrivateChannel().queue(pm -> pm.sendMessage("I failed to create a text channel in your server `" + exam.getGuild().getName() + "`: `" + e.getMessage() + "`").queue());
            return null;
        }

        if (StringUtils.isNotBlank(exam.getIntroductionMessage())) targetChannel.sendMessage(exam.getIntroductionMessage()).queue();

        int questionNumber = 0;
        for (Question question : exam.getQuestions()) {
            questionNumber++;

            boolean correct = false;
            Message message = question.send(targetChannel, questionNumber);
            if (question instanceof MultipleChoiceQuestion) correct = ((MultipleChoiceQuestion) question).grade(DiscordUtil.pullLetter(examinator, member.getUser(), message));
            if (question instanceof TrueFalseQuestion) correct = ((TrueFalseQuestion) question).grade(DiscordUtil.pullYesNo(examinator, member.getUser(), message));
            if (correct) session.correct().incrementAndGet();
        }
        session.setFinished();

        targetChannel.delete().reason("Exam channel [" + exam.getName() + " | " + member.getUser().getId() + " | " + session.getResult().toString() + " | " + TimeUtil.timestamp() + "]").queue();
        exam.getActions().forEach(action -> action.execute(session));
        destroy();
        return null;
    }

    public void destroy() {
        examChannelMessage.delete().complete();
        session.destroy();
        targetChannel.delete().complete();
        examinator.getExamPool().getActiveExams().remove(this);
    }

}
