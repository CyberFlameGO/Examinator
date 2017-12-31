package github.scarsz.examinator.exam;

public enum ExamResult {

    PASS,
    IN_PROGRESS,
    FAIL;

    public String toEmoji() {
        switch (this) {
            case PASS:
                return "✅";
            case FAIL:
                return "❌";
            case IN_PROGRESS:
                return "\uD83D\uDCDD";
            default:
                return "\uD83E\uDD14";
        }
    }

    @Override
    public String toString() {
        return name().toUpperCase().substring(0, 1) + name().toLowerCase().substring(1);
    }

}
