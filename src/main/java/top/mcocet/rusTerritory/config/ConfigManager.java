package top.mcocet.rusTerritory.config;

import org.bukkit.configuration.file.FileConfiguration;
import top.mcocet.rusTerritory.RusTerritory;

public class ConfigManager {
    private final RusTerritory plugin;
    private FileConfiguration config;

    // Protection switches
    private boolean protectPvp;
    private boolean protectBuild;
    private boolean protectDestroy;
    private boolean protectInteract;
    private boolean protectEnter;

    // Territory settings
    private int maxSize;
    private int maxPerPlayer;

    // Selection settings
    private String selectionTool;
    private boolean selectionRequireSneak;

    // Player permissions
    private boolean playerCanCreate;
    private boolean playerCanDelete;

    // Messages
    private String msgEnter;
    private String msgLeave;
    private String msgDenyPvp;
    private String msgDenyBuild;
    private String msgDenyDestroy;
    private String msgDenyInteract;
    private String msgDenyEnter;

    public ConfigManager(RusTerritory plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        load();
    }

    public void load() {
        plugin.reloadConfig();
        config = plugin.getConfig();

        protectPvp = config.getBoolean("protection.pvp", true);
        protectBuild = config.getBoolean("protection.build", true);
        protectDestroy = config.getBoolean("protection.destroy", true);
        protectInteract = config.getBoolean("protection.interact", true);
        protectEnter = config.getBoolean("protection.enter", true);

        maxSize = config.getInt("territory.max-size", 256);
        maxPerPlayer = config.getInt("territory.max-per-player", 5);

        selectionTool = config.getString("selection.tool", "WOODEN_SWORD");
        selectionRequireSneak = config.getBoolean("selection.require-sneak", true);

        playerCanCreate = config.getBoolean("player.can-create", false);
        playerCanDelete = config.getBoolean("player.can-delete", false);

        msgEnter = config.getString("messages.enter-actionbar", "§a你进入了 §f{owner} §a的领地 §f{territory}");
        msgLeave = config.getString("messages.leave-actionbar", "§e你离开了 §f{owner} §a的领地 §f{territory}");
        msgDenyPvp = config.getString("messages.deny-pvp", "§c此领地禁止PVP");
        msgDenyBuild = config.getString("messages.deny-build", "§c此领地禁止放置方块");
        msgDenyDestroy = config.getString("messages.deny-destroy", "§c此领地禁止破坏方块");
        msgDenyInteract = config.getString("messages.deny-interact", "§c此领地禁止交互");
        msgDenyEnter = config.getString("messages.deny-enter", "§c此领地禁止进入");
    }

    public void save() {
        config.set("protection.pvp", protectPvp);
        config.set("protection.build", protectBuild);
        config.set("protection.destroy", protectDestroy);
        config.set("protection.interact", protectInteract);
        config.set("protection.enter", protectEnter);
        config.set("territory.max-size", maxSize);
        config.set("territory.max-per-player", maxPerPlayer);
        config.set("selection.tool", selectionTool);
        config.set("selection.require-sneak", selectionRequireSneak);
        config.set("player.can-create", playerCanCreate);
        config.set("player.can-delete", playerCanDelete);
        config.set("messages.enter-actionbar", msgEnter);
        config.set("messages.leave-actionbar", msgLeave);
        config.set("messages.deny-pvp", msgDenyPvp);
        config.set("messages.deny-build", msgDenyBuild);
        config.set("messages.deny-destroy", msgDenyDestroy);
        config.set("messages.deny-interact", msgDenyInteract);
        config.set("messages.deny-enter", msgDenyEnter);
        plugin.saveConfig();
    }

    public void reload() {
        load();
    }

    // Getters
    public boolean isProtectPvp() { return protectPvp; }
    public boolean isProtectBuild() { return protectBuild; }
    public boolean isProtectDestroy() { return protectDestroy; }
    public boolean isProtectInteract() { return protectInteract; }
    public boolean isProtectEnter() { return protectEnter; }
    public int getMaxSize() { return maxSize; }
    public int getMaxPerPlayer() { return maxPerPlayer; }
    public String getSelectionTool() { return selectionTool; }
    public boolean isSelectionRequireSneak() { return selectionRequireSneak; }
    public boolean isPlayerCanCreate() { return playerCanCreate; }
    public boolean isPlayerCanDelete() { return playerCanDelete; }
    public String getMsgEnter() { return msgEnter; }
    public String getMsgLeave() { return msgLeave; }
    public String getMsgDenyPvp() { return msgDenyPvp; }
    public String getMsgDenyBuild() { return msgDenyBuild; }
    public String getMsgDenyDestroy() { return msgDenyDestroy; }
    public String getMsgDenyInteract() { return msgDenyInteract; }
    public String getMsgDenyEnter() { return msgDenyEnter; }

    // Setters
    public void setProtectPvp(boolean value) { protectPvp = value; }
    public void setProtectBuild(boolean value) { protectBuild = value; }
    public void setProtectDestroy(boolean value) { protectDestroy = value; }
    public void setProtectInteract(boolean value) { protectInteract = value; }
    public void setProtectEnter(boolean value) { protectEnter = value; }
    public void setMaxSize(int value) { maxSize = value; }
    public void setMaxPerPlayer(int value) { maxPerPlayer = value; }
    public void setSelectionTool(String value) { selectionTool = value; }
    public void setSelectionRequireSneak(boolean value) { selectionRequireSneak = value; }
    public void setPlayerCanCreate(boolean value) { playerCanCreate = value; }
    public void setPlayerCanDelete(boolean value) { playerCanDelete = value; }
    public void setMsgEnter(String value) { msgEnter = value; }
    public void setMsgLeave(String value) { msgLeave = value; }
    public void setMsgDenyPvp(String value) { msgDenyPvp = value; }
    public void setMsgDenyBuild(String value) { msgDenyBuild = value; }
    public void setMsgDenyDestroy(String value) { msgDenyDestroy = value; }
    public void setMsgDenyInteract(String value) { msgDenyInteract = value; }
    public void setMsgDenyEnter(String value) { msgDenyEnter = value; }
}
