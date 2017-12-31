package github.scarsz.examinator.exam;

import github.scarsz.examinator.Examinator;
import net.dv8tion.jda.core.entities.Guild;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ExamBuilder {

    private final Examinator examinator;

    private int failureCooldown;
    private int correctToPass;
    private String guildId;
    private String introductionMessage;
    private String name;
    private UUID uuid;

    public ExamBuilder(Examinator examinator) {
        this.examinator = examinator;
    }

    public ExamBuilder failureCooldown(int cooldown) {
        this.failureCooldown = cooldown;
        return this;
    }
    public ExamBuilder failureCooldown(long amount, TimeUnit unit) {
        this.failureCooldown = (int) unit.toMillis(amount);
        return this;
    }

    public ExamBuilder correctToPass(int correctToPass) {
        this.correctToPass = correctToPass;
        return this;
    }

    public ExamBuilder guild(String guildId) {
        this.guildId = guildId;
        return this;
    }
    public ExamBuilder guild(Guild guild) {
        this.guildId = guild.getId();
        return this;
    }

    public ExamBuilder introduction(String introductionMessage) {
        if (StringUtils.isBlank(introductionMessage)) introductionMessage = null;
        this.introductionMessage = introductionMessage;
        return this;
    }

    public ExamBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ExamBuilder uuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public Exam build() {
        return new Exam(examinator, uuid, examinator.getJda().getGuildById(guildId), name, correctToPass, failureCooldown, introductionMessage);
    }

}
