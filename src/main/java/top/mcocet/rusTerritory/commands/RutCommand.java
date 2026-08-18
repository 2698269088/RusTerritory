package top.mcocet.rusTerritory.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mcocet.rusTerritory.RusTerritory;
import top.mcocet.rusTerritory.listeners.SelectionListener;
import top.mcocet.rusTerritory.models.Territory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RutCommand implements CommandExecutor, TabCompleter {
    private final RusTerritory plugin;
    private final Map<UUID, String> deleteConfirm = new ConcurrentHashMap<>();

    public RutCommand(RusTerritory plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c此命令只能由玩家执行");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "ade" -> handleAde(player);
            case "create" -> handleCreate(player, args);
            case "delete" -> handleDelete(player, args);
            case "confirm" -> handleConfirm(player);
            case "list" -> handleList(player);
            case "tp" -> handleTp(player, args);
            case "info" -> handleInfo(player, args);
            case "reload" -> handleReload(player);
            case "set" -> handleSet(player, args);
            case "rename" -> handleRename(player, args);
            case "transfer" -> handleTransfer(player, args);
            case "config" -> handleConfig(player, args);
            case "help" -> sendHelp(player);
            default -> player.sendMessage("§c未知子命令，使用 /rut help 查看帮助");
        }
        return true;
    }

    private SelectionListener getSelectionListener() {
        return plugin.getSelectionListener();
    }

    private boolean isValidName(String name) {
        if (name == null || name.isEmpty() || name.length() > 32) return false;
        return name.matches("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$");
    }

    private void handleAde(Player player) {
        SelectionListener listener = getSelectionListener();
        if (listener.isInSelectionMode(player)) {
            listener.setSelectionMode(player, false);
        } else {
            listener.setSelectionMode(player, true);
        }
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c用法: /rut create <领地名称> [玩家]");
            return;
        }

        String name = args[1];
        if (!isValidName(name)) {
            player.sendMessage("§c领地名称不合法 (只能包含字母、数字、下划线和中文，最长32字符)");
            return;
        }

        SelectionListener listener = getSelectionListener();
        Location pos1 = listener.getPos1(player);
        Location pos2 = listener.getPos2(player);

        if (pos1 == null || pos2 == null) {
            player.sendMessage("§c请先使用 /rut ade 进入选区模式，手持选区工具蹲下右键点击两个方块来设定领地范围");
            return;
        }
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            player.sendMessage("§c两个点必须在同一世界");
            return;
        }
        if (plugin.getStorage().nameExists(name)) {
            player.sendMessage("§c领地名称已存在");
            return;
        }

        UUID owner = player.getUniqueId();
        if (args.length >= 3) {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                player.sendMessage("§c目标玩家不在线");
                return;
            }
            owner = target.getUniqueId();
        }

        Territory territory = new Territory(
                name,
                pos1.getWorld().getName(),
                pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ(),
                owner
        );

        if (!player.hasPermission("rusterry.admin") && plugin.getStorage().isOverlapping(territory)) {
            player.sendMessage("§c该区域与其他领地重叠，无法创建");
            return;
        }

        plugin.getStorage().addTerritory(territory);
        listener.clearSelection(player);
        player.sendMessage("§a成功创建领地 §f" + name + " §a(ID: §f" + territory.getId() + "§a)");
        player.sendMessage("§a体积: §f" + territory.getVolume() + " §a方块");
        player.sendMessage("§a拥有者: §f" + Bukkit.getOfflinePlayer(owner).getName());
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c用法: /rut delete <领地名称>");
            return;
        }
        Territory territory = plugin.getStorage().getTerritoryByName(args[1]);
        if (territory == null) {
            player.sendMessage("§c未找到该领地");
            return;
        }
        deleteConfirm.put(player.getUniqueId(), territory.getId());
        player.sendMessage("§e你确定要删除领地 §f" + territory.getName() + " §e吗？");
        player.sendMessage("§e输入 §f/rut confirm §e确认删除");
    }

    private void handleConfirm(Player player) {
        String territoryId = deleteConfirm.remove(player.getUniqueId());
        if (territoryId == null) {
            player.sendMessage("§c没有待确认的删除操作");
            return;
        }
        Territory territory = plugin.getStorage().getTerritory(territoryId);
        if (territory == null) {
            player.sendMessage("§c领地不存在");
            return;
        }
        plugin.getStorage().removeTerritory(territoryId);
        player.sendMessage("§a已删除领地 §f" + territory.getName());
    }

    private void handleList(Player player) {
        Collection<Territory> list = plugin.getStorage().getAllTerritories();
        if (list.isEmpty()) {
            player.sendMessage("§e当前没有任何领地");
            return;
        }
        player.sendMessage("§6========== 全部领地列表 ==========");
        int i = 1;
        for (Territory t : list) {
            String ownerName = Bukkit.getOfflinePlayer(t.getOwner()).getName();
            player.sendMessage("§a" + (i++) + ". §f" + t.getName() + " §7(拥有者: " + ownerName + ", 世界: " + t.getWorldName() + ")");
        }
        player.sendMessage("§6=================================");
    }

    private void handleTp(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c用法: /rut tp <领地名称>");
            return;
        }
        Territory territory = plugin.getStorage().getTerritoryByName(args[1]);
        if (territory == null) {
            player.sendMessage("§c未找到该领地");
            return;
        }
        World world = Bukkit.getWorld(territory.getWorldName());
        if (world == null) {
            player.sendMessage("§c目标世界不存在");
            return;
        }
        Location center = territory.getCenter(world);
        player.sendMessage("§a正在传送至领地 §f" + territory.getName() + "§a...");
        player.teleportAsync(center).thenAccept(success -> {
            if (success) {
                player.sendMessage("§a已传送至领地 §f" + territory.getName());
            } else {
                player.sendMessage("§c传送失败");
            }
        });
    }

    private void handleInfo(Player player, String[] args) {
        Territory territory;
        if (args.length >= 2) {
            territory = plugin.getStorage().getTerritoryByName(args[1]);
        } else {
            territory = plugin.getStorage().getTerritoryAt(player.getLocation());
        }
        if (territory == null) {
            player.sendMessage(args.length >= 2 ? "§c未找到该领地" : "§c你当前不在任何领地内");
            return;
        }
        String ownerName = Bukkit.getOfflinePlayer(territory.getOwner()).getName();
        player.sendMessage("§6========== 领地信息 ==========");
        player.sendMessage("§a名称: §f" + territory.getName());
        player.sendMessage("§aID: §f" + territory.getId());
        player.sendMessage("§a拥有者: §f" + ownerName);
        player.sendMessage("§a世界: §f" + territory.getWorldName());
        player.sendMessage("§a范围: §f(" + territory.getMinX() + ", " + territory.getMinY() + ", " + territory.getMinZ() + ") §a到 §f(" + territory.getMaxX() + ", " + territory.getMaxY() + ", " + territory.getMaxZ() + ")");
        player.sendMessage("§a体积: §f" + territory.getVolume() + " §a方块");
        player.sendMessage("§aPVP: §f" + (territory.isPvp() ? "§c允许" : "§a禁止"));
        player.sendMessage("§a建造: §f" + (territory.isBuild() ? "§a允许" : "§c禁止"));
        player.sendMessage("§a破坏: §f" + (territory.isDestroy() ? "§a允许" : "§c禁止"));
        player.sendMessage("§a交互: §f" + (territory.isInteract() ? "§a允许" : "§c禁止"));
        player.sendMessage("§a进入: §f" + (territory.isEnter() ? "§a允许" : "§c禁止"));
        player.sendMessage("§6==============================");
    }

    private void handleReload(Player player) {
        plugin.getConfigManager().reload();
        plugin.getStorage().loadAll();
        player.sendMessage("§a配置文件和领地数据已重载");
    }

    private void handleSet(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§c用法: /rut set <领地名称> <pvp|build|destroy|interact|enter> <true|false>");
            return;
        }
        Territory territory = plugin.getStorage().getTerritoryByName(args[1]);
        if (territory == null) {
            player.sendMessage("§c未找到该领地");
            return;
        }
        String property = args[2].toLowerCase();
        boolean value = Boolean.parseBoolean(args[3]);
        switch (property) {
            case "pvp" -> territory.setPvp(value);
            case "build" -> territory.setBuild(value);
            case "destroy" -> territory.setDestroy(value);
            case "interact" -> territory.setInteract(value);
            case "enter" -> territory.setEnter(value);
            default -> {
                player.sendMessage("§c未知属性，可用: pvp, build, destroy, interact, enter");
                return;
            }
        }
        plugin.getStorage().addTerritory(territory);
        player.sendMessage("§a已设置 §f" + territory.getName() + " §a的 §f" + property + " §a为 §f" + value);
    }

    private void handleRename(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§c用法: /rut rename <旧名称> <新名称>");
            return;
        }
        Territory territory = plugin.getStorage().getTerritoryByName(args[1]);
        if (territory == null) {
            player.sendMessage("§c未找到该领地");
            return;
        }
        String newName = args[2];
        if (!isValidName(newName)) {
            player.sendMessage("§c领地名称不合法 (只能包含字母、数字、下划线和中文，最长32字符)");
            return;
        }
        if (plugin.getStorage().nameExists(newName)) {
            player.sendMessage("§c该名称已被使用");
            return;
        }
        String oldName = territory.getName();
        territory.setName(newName);
        plugin.getStorage().addTerritory(territory);
        player.sendMessage("§a已将领地 §f" + oldName + " §a重命名为 §f" + newName);
    }

    private void handleTransfer(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§c用法: /rut transfer <领地名称> <玩家>");
            return;
        }
        Territory territory = plugin.getStorage().getTerritoryByName(args[1]);
        if (territory == null) {
            player.sendMessage("§c未找到该领地");
            return;
        }
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            player.sendMessage("§c目标玩家不在线");
            return;
        }
        UUID oldOwner = territory.getOwner();
        territory.setOwner(target.getUniqueId());
        plugin.getStorage().addTerritory(territory);
        player.sendMessage("§a已将领地 §f" + territory.getName() + " §a转让给 §f" + target.getName());
        target.sendMessage("§a你已获得领地 §f" + territory.getName() + " §a的所有权");
    }

    private void handleConfig(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§c用法: /rut config <key> <value>");
            player.sendMessage("§7可用配置项:");
            player.sendMessage("§7  protection.pvp / protection.build / protection.destroy / protection.interact / protection.enter");
            player.sendMessage("§7  territory.max-size / territory.max-per-player");
            player.sendMessage("§7  selection.tool / selection.require-sneak");
            player.sendMessage("§7  player.can-create / player.can-delete");
            player.sendMessage("§7  messages.enter-actionbar / messages.leave-actionbar");
            player.sendMessage("§7  messages.deny-pvp / messages.deny-build / messages.deny-destroy");
            player.sendMessage("§7  messages.deny-interact / messages.deny-enter");
            return;
        }
        String key = args[1];
        String value = args[2];
        try {
            switch (key) {
                case "protection.pvp" -> plugin.getConfigManager().setProtectPvp(Boolean.parseBoolean(value));
                case "protection.build" -> plugin.getConfigManager().setProtectBuild(Boolean.parseBoolean(value));
                case "protection.destroy" -> plugin.getConfigManager().setProtectDestroy(Boolean.parseBoolean(value));
                case "protection.interact" -> plugin.getConfigManager().setProtectInteract(Boolean.parseBoolean(value));
                case "protection.enter" -> plugin.getConfigManager().setProtectEnter(Boolean.parseBoolean(value));
                case "territory.max-size" -> plugin.getConfigManager().setMaxSize(Integer.parseInt(value));
                case "territory.max-per-player" -> plugin.getConfigManager().setMaxPerPlayer(Integer.parseInt(value));
                case "selection.tool" -> plugin.getConfigManager().setSelectionTool(value.toUpperCase());
                case "selection.require-sneak" -> plugin.getConfigManager().setSelectionRequireSneak(Boolean.parseBoolean(value));
                case "player.can-create" -> plugin.getConfigManager().setPlayerCanCreate(Boolean.parseBoolean(value));
                case "player.can-delete" -> plugin.getConfigManager().setPlayerCanDelete(Boolean.parseBoolean(value));
                case "messages.enter-actionbar" -> plugin.getConfigManager().setMsgEnter(value.replace("_", " "));
                case "messages.leave-actionbar" -> plugin.getConfigManager().setMsgLeave(value.replace("_", " "));
                case "messages.deny-pvp" -> plugin.getConfigManager().setMsgDenyPvp(value.replace("_", " "));
                case "messages.deny-build" -> plugin.getConfigManager().setMsgDenyBuild(value.replace("_", " "));
                case "messages.deny-destroy" -> plugin.getConfigManager().setMsgDenyDestroy(value.replace("_", " "));
                case "messages.deny-interact" -> plugin.getConfigManager().setMsgDenyInteract(value.replace("_", " "));
                case "messages.deny-enter" -> plugin.getConfigManager().setMsgDenyEnter(value.replace("_", " "));
                default -> {
                    player.sendMessage("§c未知配置项: " + key);
                    return;
                }
            }
            plugin.getConfigManager().save();
            player.sendMessage("§a已设置 §f" + key + " §a为 §f" + value);
        } catch (Exception e) {
            player.sendMessage("§c设置失败: " + e.getMessage());
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6========== RusTerritory 管理员帮助 ==========");
        player.sendMessage("§a/rut ade §7- 进入/退出选区模式");
        player.sendMessage("§a/rut create <名称> [玩家] §7- 使用选区创建领地(可指定拥有者)");
        player.sendMessage("§a/rut delete <名称> §7- 删除任意领地");
        player.sendMessage("§a/rut confirm §7- 确认删除领地");
        player.sendMessage("§a/rut list §7- 列出所有领地");
        player.sendMessage("§a/rut tp <名称> §7- 传送至领地中心");
        player.sendMessage("§a/rut info [名称] §7- 查看领地信息");
        player.sendMessage("§a/rut set <名称> <属性> <true|false> §7- 设置领地属性");
        player.sendMessage("§a/rut rename <旧名称> <新名称> §7- 重命名领地");
        player.sendMessage("§a/rut transfer <名称> <玩家> §7- 转让领地");
        player.sendMessage("§a/rut config <key> <value> §7- 修改配置文件");
        player.sendMessage("§a/rut reload §7- 重载配置和数据");
        player.sendMessage("§a/rut help §7- 显示此帮助");
        player.sendMessage("§6============================================");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("ade", "create", "delete", "confirm", "list", "tp", "info", "set", "rename", "transfer", "config", "reload", "help")
                    .stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && List.of("delete", "tp", "info", "set", "rename", "transfer").contains(args[0].toLowerCase())) {
            return plugin.getStorage().getAllTerritories().stream()
                    .map(Territory::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("config")) {
            return List.of("protection.pvp", "protection.build", "protection.destroy", "protection.interact", "protection.enter",
                    "territory.max-size", "territory.max-per-player",
                    "selection.tool", "selection.require-sneak",
                    "player.can-create", "player.can-delete",
                    "messages.enter-actionbar", "messages.leave-actionbar",
                    "messages.deny-pvp", "messages.deny-build", "messages.deny-destroy",
                    "messages.deny-interact", "messages.deny-enter")
                    .stream().filter(s -> s.startsWith(args[1].toLowerCase())).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return List.of("pvp", "build", "destroy", "interact", "enter")
                    .stream().filter(s -> s.startsWith(args[2].toLowerCase())).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("transfer")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase()))
                    .toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("config")) {
            if (args[1].startsWith("protection.") || args[1].startsWith("player.") || args[1].startsWith("selection.require-sneak")) {
                return List.of("true", "false").stream().filter(s -> s.startsWith(args[2].toLowerCase())).toList();
            }
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            return List.of("true", "false")
                    .stream().filter(s -> s.startsWith(args[3].toLowerCase())).toList();
        }
        return List.of();
    }
}
