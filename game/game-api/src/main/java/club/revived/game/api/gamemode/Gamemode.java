package club.revived.game.api.gamemode;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public abstract class Gamemode {
    
    private final String id;
    private final String displayName;
    private final Plugin plugin;
    private boolean enabled;
    
    protected Gamemode(final @NotNull String id, final @NotNull String displayName, final @NotNull Plugin plugin) {
        this.id = id;
        this.displayName = displayName;
        this.plugin = plugin;
        this.enabled = false;
    }
    
    @NotNull
    public String getId() {
        return id;
    }
    
    @NotNull
    public String getDisplayName() {
        return displayName;
    }
    
    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public final void enable() {
        if (!enabled) {
            enabled = true;
            getLogger().info("Enabling gamemode: " + displayName);
            onEnable();
        }
    }
    
    public final void disable() {
        if (enabled) {
            enabled = false;
            getLogger().info("Disabling gamemode: " + displayName);
            onDisable();
        }
    }
    
    protected abstract void onEnable();
    
    protected abstract void onDisable();
    
    @NotNull
    protected Logger getLogger() {
        return plugin.getLogger();
    }
}
