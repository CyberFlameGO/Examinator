package github.scarsz.examinator.manager;

import github.scarsz.examinator.Examinator;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigurationManager extends ListenerAdapter {

    private final Examinator examinator;
    private final Guild configGuild;
    private final Role proctorRole;
    private final Category configCategory;
    private final Set<GuildConfigManager> managers = new HashSet<>();

    public ConfigurationManager(Examinator examinator) {
        this.examinator = examinator;
        configGuild = examinator.getJda().getGuildById("385963777364983808");
        configCategory = configGuild.getCategoryById("385963920441081857");
        proctorRole = configGuild.getRolesByName("Proctor", true).get(0);

        examinator.getJda().addEventListener(this);

        // create GuildConfigManager for guilds with channels, create channels if necessary
        for (Member member : configGuild.getMembers()) {
            List<Guild> matchingGuilds = examinator.getJda().getGuilds().stream().filter(guild -> guild.getOwner().getUser().equals(member.getUser())).collect(Collectors.toList());
            matchingGuilds.forEach(guild -> {
                if (configGuild.getTextChannelsByName(guild.getId(), true).size() == 0) {
                    configCategory.createTextChannel(guild.getId()).complete();
                }
            });
        }
        for (TextChannel channel : configCategory.getTextChannels()) {
            Guild targetGuild; try { targetGuild = examinator.getJda().getGuildById(channel.getName()); } catch (Exception e) { continue; } if (targetGuild == null) return;
            if (configGuild.getMemberById(targetGuild.getOwner().getUser().getId()) == null) return;
            managers.add(new GuildConfigManager(examinator, this, channel));
        }

        // set proctor roles
        List<String> proctors = examinator.getJda().getGuilds().stream().map(Guild::getOwner).map(Member::getUser).map(User::getId).collect(Collectors.toList());
        System.out.println(proctors);
        configGuild.getMembers().forEach(member -> {
            if (proctors.contains(member.getUser().getId())) {
                configGuild.getController().addRolesToMember(member, proctorRole).queue();
            } else {
                configGuild.getController().removeRolesFromMember(member, proctorRole).queue();
            }
        });
    }

    public void destroy() {
        examinator.getJda().removeEventListener(this);
        managers.forEach(GuildConfigManager::destroy);
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (!event.getGuild().equals(configGuild)) return;

        // GuildConfigManager creation
        List<Guild> matchingGuilds = examinator.getJda().getGuilds().stream().filter(guild -> guild.getOwner().getUser().equals(event.getUser())).collect(Collectors.toList());
        matchingGuilds.forEach(guild -> {
            List<TextChannel> channels = configGuild.getTextChannelsByName(guild.getName(), true);
            if (channels.size() == 0) channels.add((TextChannel) configCategory.createTextChannel(guild.getId()).complete());
            channels.forEach(channel -> managers.add(new GuildConfigManager(examinator, this, channel)));
        });

        // Proctor role
        if (examinator.getJda().getGuilds().stream().map(Guild::getOwner).anyMatch(member -> member.equals(event.getMember()))) configGuild.getController().addRolesToMember(event.getMember(), proctorRole).queue();
    }
    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        List<GuildConfigManager> managersToDestroy = managers.stream().filter(manager -> manager.getOwner().getUser().equals(event.getUser())).collect(Collectors.toList());
        managersToDestroy.forEach(GuildConfigManager::destroy);
        managers.removeAll(managersToDestroy);
    }
    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        System.out.println("Joined " + event.getGuild());

        // GuildConfigManager creation
        List<TextChannel> channels = configGuild.getTextChannelsByName(event.getGuild().getName(), true);
        if (channels.size() == 0) channels.add((TextChannel) configCategory.createTextChannel(event.getGuild().getId()).complete());
        channels.forEach(channel -> managers.add(new GuildConfigManager(examinator, this, channel)));
    }

    public Guild getConfigGuild() {
        return configGuild;
    }
    public Category getConfigCategory() {
        return configCategory;
    }
    public Set<GuildConfigManager> getManagers() {
        return managers;
    }

}
