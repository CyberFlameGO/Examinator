package github.scarsz.examinator.util;

import net.dv8tion.jda.core.entities.User;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Timeout {

    private final Set<TimeoutEntry> timeouts = new HashSet<>();

    public void add(User user, UUID test, long msAtExpiration) {
        timeouts.add(new TimeoutEntry(user, test, msAtExpiration));
    }

    public boolean isOnTimeout(User user, UUID test) {
        // remove old timeouts
        timeouts.removeIf(timeout -> timeout.msAtExpiration < System.currentTimeMillis());

        return getDuration(user, test) > 0;
    }

    public long getDuration(User user, UUID test) {
        TimeoutEntry entry = timeouts.stream()
                .filter(timeout -> timeout.user.equals(user))
                .filter(timeout -> timeout.test.equals(test))
                .findFirst().orElse(null);
        if (entry == null) return -1;
        return entry.msAtExpiration - System.currentTimeMillis();
    }

    private class TimeoutEntry {

        private final User user;
        private final UUID test;
        private final long msAtExpiration;

        public TimeoutEntry(User user, UUID test, long msAtExpiration) {
            this.user = user;
            this.test = test;
            this.msAtExpiration = msAtExpiration;
        }

        public User getUser() {
            return user;
        }

        public UUID getTest() {
            return test;
        }

        public long getMsAtExpiration() {
            return msAtExpiration;
        }

    }

}
