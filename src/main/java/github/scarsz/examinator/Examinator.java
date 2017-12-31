package github.scarsz.examinator;

import com.jagrosh.jdautilities.waiter.EventWaiter;
import github.scarsz.examinator.exam.Exam;
import github.scarsz.examinator.exam.TestingSession;
import github.scarsz.examinator.manager.ConfigurationManager;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Examinator {

    private final ExamPool examPool;
    private final JDA jda;
    private final EventWaiter waiter = new EventWaiter();
    private final Set<Exam> exams = new HashSet<>();
    private final Set<TestingSession> sessions = new HashSet<>();
    private final ConfigurationManager configurationManager;

    private static final File examsFile = new File("exams.json");

    public Examinator(String botToken) throws Exception {
        jda = new JDABuilder(AccountType.BOT)
                .addEventListener(waiter)
                .setAudioEnabled(false)
                .setBulkDeleteSplittingEnabled(false)
                .setGame(Game.listening("Scarsz swear"))
                .setToken(botToken)
                .buildBlocking();
        examPool = new ExamPool(this);
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "Examinator - Shutdown thread"));
        Exam.loadExamsFromFile(this, examsFile);
        configurationManager = new ConfigurationManager(this);
        System.out.println();

        System.out.println("Guilds:");
        for (Guild guild : jda.getGuilds()) System.out.println(guild.getName() + ": " + guild.getRoles().stream().map(role -> role.getName() + " [" + role.getId() + "]").collect(Collectors.joining(", ")));
        System.out.println();

        System.out.println("Exams:");
        exams.forEach(System.out::println);
        System.out.println();

        System.out.println("Loaded " + exams.size() + " exams in " + jda.getGuilds().size() + " guilds\n");
    }

    public void shutdown() {
        try {
            JSONArray examsJson = new JSONArray();
            exams.stream().map(Exam::serialize).forEach(examsJson::add);
            FileUtils.writeStringToFile(examsFile, examsJson.toString(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        new ArrayList<>(exams).forEach(Exam::destroy);

        configurationManager.destroy();
    }

    public ExamPool getExamPool() {
        return examPool;
    }
    public JDA getJda() {
        return jda;
    }
    public EventWaiter getWaiter() {
        return waiter;
    }
    public Set<Exam> getExams() {
        return exams;
    }
    public Set<Exam> getExams(Guild guild) {
        return exams.stream().filter(exam -> exam.getGuild().equals(guild)).collect(Collectors.toSet());
    }
    public Set<TestingSession> getSessions() {
        return sessions;
    }

}
