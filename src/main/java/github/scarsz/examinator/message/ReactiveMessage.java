package github.scarsz.examinator.message;

import github.scarsz.examinator.Examinator;
import github.scarsz.examinator.manager.GuildConfigManager;
import net.dv8tion.jda.core.entities.Message;

public abstract class ReactiveMessage {

    protected final Examinator examinator;
    protected final Message message;
    protected final GuildConfigManager manager;

    public ReactiveMessage(Examinator examinator, Message message, GuildConfigManager manager) {
        this.examinator = examinator;
        this.message = message;
        this.manager = manager;
    }

}
