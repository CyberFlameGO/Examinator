package github.scarsz.examinator.exam;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.action.*;
import github.scarsz.examinator.exam.question.MultipleChoiceQuestion;
import github.scarsz.examinator.exam.question.Question;
import github.scarsz.examinator.exam.question.TrueFalseQuestion;
import github.scarsz.examinator.exam.trigger.*;
import github.scarsz.examinator.util.TimeUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Exam {

    private final Examinator examinator;
    private final UUID uuid;
    private final Guild guild;
    private String name;
    private int correctQuestionsToPass;
    private int failureCooldown;
    private String introductionMessage;
    private final HashSet<Action> actions = new HashSet<>();
    private final HashSet<Question> questions = new LinkedHashSet<>();
    private final HashSet<Trigger> triggers = new HashSet<>();

    public Exam(Examinator examinator, UUID uuid, Guild guild, String name, int correctQuestionsToPass, int failureCooldown, String introductionMessage) {
        this.examinator = examinator;
        this.uuid = uuid;
        this.guild = guild;
        this.name = name;
        this.introductionMessage = introductionMessage;
        this.correctQuestionsToPass = correctQuestionsToPass;
        this.failureCooldown = failureCooldown;
        examinator.getExams().add(this);
    }

    public void addActions(Action... actions) {
        this.actions.addAll(Arrays.asList(actions));
    }
    public void addQuestions(Question... questions) {
        this.questions.addAll(Arrays.asList(questions));
    }
    public void addTriggers(Trigger... triggers) {
        this.triggers.addAll(Arrays.asList(triggers));
    }

    public void destroy() {
        triggers.forEach(Trigger::destroy);
        examinator.getExams().remove(this);
    }
    public void take(Trigger trigger, TextChannel channel, Member member) {
        if (examinator.getTimeout().isOnTimeout(member.getUser(), uuid)) {
            channel.sendMessage(member.getAsMention() + ", you have already attempted this exam. You need to wait for `" + TimeUtil.formatDuration(examinator.getTimeout().getDuration(member.getUser(), uuid)) + "` before attempting it again.").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
            return;
        }

        for (TestingSession session : examinator.getSessions()) {
            if (session.getMember().equals(member)) {
                channel.sendMessage(member.getAsMention() + ", you are already taking an exam. Please finish that one before starting another.").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                return;
            }
        }

        examinator.getExamPool().timedCall(new ExamCallable(examinator, this, trigger, channel, member));
        examinator.getTimeout().add(member.getUser(), getUuid(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(failureCooldown));
        System.out.println("Exam \"" + name + "\" being taken by " + member.getEffectiveName());
    }

    public Guild getGuild() {
        return guild;
    }
    public int getCorrectQuestionsToPass() {
        return correctQuestionsToPass;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setFailureCooldown(int cooldown) {
        this.failureCooldown = cooldown;
    }
    public int getFailureCooldown() {
        return failureCooldown;
    }
    public String getIntroductionMessage() {
        return introductionMessage;
    }
    public Set<Action> getActions() {
        return actions;
    }
    public Set<Question> getQuestions() {
        return questions;
    }
    public Set<Trigger> getTriggers() {
        return triggers;
    }
    public UUID getUuid() {
        return uuid;
    }

    public JSONObject serialize() {
        JSONObject serializedExam = new JSONObject();

        serializedExam.put("guild", guild.getId());
        serializedExam.put("name", name);
        serializedExam.put("correctToPass", correctQuestionsToPass);
        serializedExam.put("cooldown", failureCooldown);
        serializedExam.put("intro", introductionMessage);
        serializedExam.put("uuid", uuid.toString());
        serializedExam.put("actions", actions.stream().map(Action::serialize).collect(Collectors.toList()));
        serializedExam.put("questions", questions.stream().map(Question::serialize).collect(Collectors.toList()));
        serializedExam.put("triggers", triggers.stream().map(Trigger::serialize).collect(Collectors.toList()));

        return serializedExam;
    }
    public static List<Exam> loadExamsFromFile(Examinator examinator, File file) {
        List<Exam> exams = new ArrayList<>();

        JSONArray examsJson;
        try {
            examsJson = (JSONArray) new JSONParser().parse(FileUtils.readFileToString(file, Charset.forName("UTF-8")));
        } catch (ParseException | IOException e) {
            System.out.println("Failed to load exams from file: " + e.getMessage());
            return Collections.emptyList();
        }

        for (Object examJson : examsJson) {
            Map<String, Object> examMap = (Map<String, Object>) examJson;

            String guildId = (String) examMap.getOrDefault("guild", null);
            String examName = (String) examMap.getOrDefault("name", "Unknown");
            int correctToPass = Math.toIntExact((Long) examMap.getOrDefault("correctToPass", -1));
            int failureCooldown = Math.toIntExact((Long) examMap.getOrDefault("cooldown", 0));
            String introduction = (String) examMap.getOrDefault("intro", null);
            UUID uuid = examMap.get("uuid") != null ? UUID.fromString((String) examMap.get("uuid")) : UUID.randomUUID();

            Exam exam = new ExamBuilder(examinator)
                    .guild(guildId)
                    .name(examName)
                    .correctToPass(correctToPass)
                    .failureCooldown(failureCooldown)
                    .introduction(introduction)
                    .uuid(uuid)
                    .build();

            for (Object actionJson : ((JSONArray) examMap.get("actions"))) {
                Action action;
                Map<String, Object> actionMap = (Map<String, Object>) actionJson;
                int type = Math.toIntExact((Long) actionMap.get("type"));
                switch (type) {
                    case Action.Type.ADD_USER_TO_ROLE:
                        action = new AddUserToRoleAction(examinator, exam, (String) actionMap.get("roleId"));
                        break;
                    case Action.Type.REMOVE_USER_FROM_ROLE:
                        action = new RemoveUserFromRoleAction(examinator, exam, (String) actionMap.get("roleId"));
                        break;
                    case Action.Type.DECREMENT_TRIES_REMAINING:
                        action = new DecrementTriesRemainingAction(examinator, exam);
                        break;
                    case Action.Type.LOG_RESULT_TO_CHANNEL:
                        action = new LogResultToChannelAction(examinator, exam, (String) actionMap.get("channelId"));
                        break;
                    case Action.Type.PM_CUSTOM_MESSAGE:
                        action = new PrivateMessageCustomMessageToUserAction(examinator, exam, (String) actionMap.get("message"));
                        break;
                    case Action.Type.PM_RESULT:
                        action = new PrivateMessageResultToUserAction(examinator, exam);
                        break;
                    case Action.Type.SEND_MESSAGE_TO_CHANNEL:
                        action = new SendMessageToChannelAction(examinator, exam, (String) actionMap.get("channelId"), (String) actionMap.get("message"));
                        break;
                    default:
                        action = null;
                        System.out.println("Scarsz you're an idiot");
                        break;
                }
                exam.addActions(action);
            }

            for (Object questionJson : ((JSONArray) examMap.get("questions"))) {
                Question question;
                Map<String, Object> questionMap = (Map<String, Object>) questionJson;
                int type = Math.toIntExact((Long) questionMap.get("type"));
                switch (type) {
                    case Question.Type.MULTIPLE_CHOICE:
                        Map<Character, String> options = new HashMap<Character, String>() {{
                            ((Map) questionMap.get("options")).forEach((k, v) -> put(((String) k).charAt(0), (String) v));
                        }};
                        question = new MultipleChoiceQuestion(examinator, exam, (String) questionMap.get("prompt"), options, ((String) questionMap.get("answer")).charAt(0));
                        break;
                    case Question.Type.TRUE_FALSE:
                        question = new TrueFalseQuestion(examinator, exam, (String) questionMap.get("prompt"), (Boolean) questionMap.get("answer"));
                        break;
                    default:
                        question = null;
                        System.out.println("Scarsz you're an idiot x2");
                        break;
                }
                exam.addQuestions(question);
            }

            for (Object triggerJson : ((JSONArray) examMap.get("triggers"))) {
                Trigger trigger;
                Map<String, Object> triggerMap = (Map<String, Object>) triggerJson;
                int type = Math.toIntExact((Long) triggerMap.get("type"));
                switch (type) {
                    case Trigger.Type.MESSAGE_SENT_ANYWHERE:
                        trigger = new MessageSentAnywhereTrigger(examinator, exam, (String) triggerMap.get("message"));
                        break;
                    case Trigger.Type.MESSAGE_SENT_TO_CHANNEL:
                        trigger = new MessageSentToChannelTrigger(examinator, exam, (String) triggerMap.get("channelId"), (String) triggerMap.get("message"));
                        break;
                    case Trigger.Type.REACTION_ADDED_TO_MESSAGE:
                        trigger = new ReactionAddedToMessageTrigger(examinator, exam, (String) triggerMap.get("messageId"));
                        break;
                    case Trigger.Type.SPECIFIC_REACTION_ADDED_TO_MESSAGE:
                        trigger = new SpecificReactionAddedToMessageTrigger(examinator, exam, (String) triggerMap.get("reaction"), (String) triggerMap.get("messageId"));
                        break;
                    default:
                        trigger = null;
                        System.out.println("Scarsz you're an idiot x3");
                        break;
                }
                exam.addTriggers(trigger);
            }
        }

        return exams;
    }

    @Override
    public String toString() {
        return "Exam{" +
                "guild=" + guild.getName() +
                ", name='" + name + "'" +
                ", actions=" + actions.size() +
                ", questions=" + questions.size() +
                ", triggers=" + triggers.size() +
                '}';
    }

}
