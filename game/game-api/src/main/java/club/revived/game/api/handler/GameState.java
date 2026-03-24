package club.revived.game.api.handler;

public enum GameState {

    WAITING,
    STARTING,
    IN_PROGRESS,
    ENDING,
    FINISHED;

    public boolean isPlayable() {
        return this == IN_PROGRESS;
    }

    public boolean isJoinable() {
        return this == WAITING || this == STARTING;
    }

    public boolean isTerminal() {
        return this == FINISHED;
    }
}
