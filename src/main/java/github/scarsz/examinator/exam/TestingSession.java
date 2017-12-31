package github.scarsz.examinator.exam;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.util.TimeUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TestingSession {

    private final Examinator examinator;
    private final Exam exam;
    private final Member member;
    private final long startTime;
    private long finishTime = 0;
    private AtomicInteger correct = new AtomicInteger();

    public TestingSession(Examinator examinator, Exam exam, Member member) {
        this.examinator = examinator;
        this.exam = exam;
        this.member = member;
        this.startTime = System.currentTimeMillis();
        examinator.getSessions().add(this);
    }

    public MessageEmbed asEmbed() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(getResult() == ExamResult.PASS ? Color.GREEN : Color.RED);
        builder.setAuthor(member.getEffectiveName(), null, member.getUser().getEffectiveAvatarUrl());
        builder.setTitle("Exam: " + exam.getName());
        builder.setDescription("Taken at " + exam.getGuild().getName());
        builder.setThumbnail(exam.getGuild().getIconUrl());
        builder.addField("Questions correct", correct.get() + "/" + exam.getQuestions().size(), true);
        builder.addField("Correct amount to pass", String.valueOf(exam.getCorrectQuestionsToPass()), true);
        builder.addField("Time taken", TimeUtil.formatDuration(getDuration()), true);
        builder.addField("Result", getResult().toEmoji(), true);
        builder.setFooter("Test started at " + TimeUtil.timestamp(startTime), "https://image.flaticon.com/icons/png/512/59/59252.png");
        return builder.build();
    }

    /**
     * Get the result of this testing session
     * @return {@link ExamResult#PASS} if questions correct >= amount required to pass<br>{@link ExamResult#IN_PROGRESS} if exam still in progress<br>{@link ExamResult#FAIL} otherwise
     */
    public ExamResult getResult() {
        if (finishTime == 0) return ExamResult.IN_PROGRESS;
        if (correct.get() >= exam.getCorrectQuestionsToPass()) return ExamResult.PASS;
        return ExamResult.FAIL;
    }

    public Exam getExam() {
        return exam;
    }

    public Member getMember() {
        return member;
    }

    public AtomicInteger correct() {
        return correct;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setFinished() {
        if (finishTime != 0) throw new RuntimeException("Session already set as finished");
        finishTime = System.currentTimeMillis();
    }
    public long getFinishTime() {
        return finishTime;
    }

    public long getDuration() {
        long duration = finishTime - startTime;
        return duration > 0 ? duration : 0;
    }

    public void destroy() {
        examinator.getSessions().remove(this);
    }

}
