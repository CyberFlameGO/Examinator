package github.scarsz.examinator.manager;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.exam.Exam;
import github.scarsz.examinator.message.ExamMessage;
import github.scarsz.examinator.message.HeaderMessage;
import github.scarsz.examinator.message.ReactiveMessage;
import github.scarsz.examinator.util.DiscordUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdatePermissionsEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GuildConfigManager extends ListenerAdapter {

    private final Examinator examinator;
    private final ConfigurationManager manager;
    private final TextChannel channel;
    private final Guild guild;
    private final Member owner;
    private final List<ReactiveMessage> messages = new LinkedList<>();

    private boolean expectingMessage;

    public GuildConfigManager(Examinator examinator, ConfigurationManager manager, TextChannel channel) {
        this.examinator = examinator;
        this.manager = manager;
        this.channel = channel;
        this.guild = examinator.getJda().getGuildById(channel.getName());
        this.owner = manager.getConfigGuild().getMember(guild.getOwner().getUser());

        examinator.getJda().addEventListener(this);
        init();
        tick();
    }

    public void destroy() {
        examinator.getJda().removeEventListener(this);
    }

    public void init() {
        DiscordUtil.clear(channel);
    }

    public void tick() {
        tickMessages();
        tickPermissions();
        tickTopic();
    }
    public void tickMessages() {
        synchronized (messages) {
            if (messages.size() == 0 || !(messages.get(0) instanceof HeaderMessage)) {
                messages.add(0, HeaderMessage.send(examinator, this, channel));
            }

            for (Exam exam : examinator.getExams(guild)) {
                boolean exists = messages.stream()
                        .filter(m -> m instanceof ExamMessage).map(ExamMessage.class::cast)
                        .anyMatch(m -> m.getExam().equals(exam));
                if (!exists) {
                    messages.add(ExamMessage.send(examinator, this, exam));
                }
            }
        }
    }
    public void tickPermissions() {
        PermissionOverride override = channel.getPermissionOverride(owner);
        if (override == null) override = channel.createPermissionOverride(owner).complete();

        List<Permission> permissions = Arrays.asList(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MANAGE_PERMISSIONS);
        if (examinator.getJda().getGuilds().stream().anyMatch(g -> g.getId().equals(channel.getName()))) {
            override.getManager().grant(permissions).queue();
        } else {
            override.getManager().deny(permissions).queue();
        }
    }
    public void tickTopic() {
        if (channel.getTopic() == null || !channel.getTopic().equals(guild.getName())) {
            channel.getManager().setTopic(guild.getName()).queue();
        }
    }

    @Override
    public void onGuildUpdateName(GuildUpdateNameEvent event) {
        if (event.getGuild().equals(guild)) tick();
    }
    @Override
    public void onTextChannelUpdatePermissions(TextChannelUpdatePermissionsEvent event) {
        if (event.getChannel().equals(channel)) tick();
    }
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getChannel().equals(channel) && !event.getAuthor().isBot() && !expectingMessage) event.getMessage().delete().queue();
    }
    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        if (event.getChannel().equals(channel)) tick();
    }

    public TextChannel getChannel() {
        return channel;
    }
    public Guild getGuild() {
        return guild;
    }
    public Member getOwner() {
        return owner;
    }
    public boolean isExpectingMessage() {
        return expectingMessage;
    }
    public void setExpectingMessage(boolean expectingMessage) {
        this.expectingMessage = expectingMessage;
    }

}
