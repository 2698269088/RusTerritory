package top.mcocet.rusTerritory.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Location;
import top.mcocet.rusTerritory.RusTerritory;
import top.mcocet.rusTerritory.models.Territory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TerritoryStorage {
    private final RusTerritory plugin;
    private final File dataFolder;
    private final Gson gson;
    private final Map<String, Territory> territories = new ConcurrentHashMap<>();
    private final Map<UUID, List<Territory>> ownerCache = new ConcurrentHashMap<>();

    public TerritoryStorage(RusTerritory plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Territory.class, new Territory.Serializer())
                .create();
        loadAll();
    }

    public void loadAll() {
        territories.clear();
        ownerCache.clear();
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;
        for (File file : files) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                List<Territory> list = gson.fromJson(reader, new TypeToken<List<Territory>>(){}.getType());
                if (list != null) {
                    for (Territory t : list) {
                        territories.put(t.getId(), t);
                        ownerCache.computeIfAbsent(t.getOwner(), k -> new ArrayList<>()).add(t);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("读取领地数据失败: " + file.getName() + " - " + e.getMessage());
            }
        }
        plugin.getLogger().info("已加载 " + territories.size() + " 个领地");
    }

    public void saveAll() {
        Map<UUID, List<Territory>> byOwner = new HashMap<>();
        for (Territory t : territories.values()) {
            if (t.getOwner() != null) {
                byOwner.computeIfAbsent(t.getOwner(), k -> new ArrayList<>()).add(t);
            }
        }
        for (Map.Entry<UUID, List<Territory>> entry : byOwner.entrySet()) {
            savePlayerTerritories(entry.getKey(), entry.getValue());
        }
    }

    public void savePlayerTerritories(UUID owner, List<Territory> list) {
        File file = new File(dataFolder, owner.toString() + ".json");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(list, writer);
            writer.flush();
        } catch (IOException e) {
            plugin.getLogger().severe("保存领地数据失败: " + file.getName() + " - " + e.getMessage());
        }
    }

    public void addTerritory(Territory territory) {
        territories.put(territory.getId(), territory);
        ownerCache.computeIfAbsent(territory.getOwner(), k -> new ArrayList<>()).add(territory);
        saveOwner(territory.getOwner());
        plugin.getLogger().info("已保存领地: " + territory.getName() + " (拥有者: " + territory.getOwner() + ")");
    }

    public void removeTerritory(String id) {
        Territory t = territories.remove(id);
        if (t != null && t.getOwner() != null) {
            List<Territory> list = ownerCache.get(t.getOwner());
            if (list != null) {
                list.remove(t);
            }
            saveOwner(t.getOwner());
        }
    }

    private void saveOwner(UUID owner) {
        if (owner == null) return;
        List<Territory> list = ownerCache.getOrDefault(owner, new ArrayList<>());
        savePlayerTerritories(owner, list);
    }

    public Territory getTerritory(String id) {
        return territories.get(id);
    }

    public Territory getTerritoryByName(String name) {
        for (Territory t : territories.values()) {
            if (t.getName().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return null;
    }

    public Collection<Territory> getAllTerritories() {
        return Collections.unmodifiableCollection(territories.values());
    }

    public List<Territory> getTerritoriesByOwner(UUID owner) {
        return new ArrayList<>(ownerCache.getOrDefault(owner, new ArrayList<>()));
    }

    public int getTerritoryCount(UUID owner) {
        return ownerCache.getOrDefault(owner, new ArrayList<>()).size();
    }

    public Territory getTerritoryAt(Location location) {
        for (Territory t : territories.values()) {
            if (t.contains(location)) {
                return t;
            }
        }
        return null;
    }

    public boolean nameExists(String name) {
        return getTerritoryByName(name) != null;
    }

    public boolean isOverlapping(Territory newTerritory) {
        for (Territory t : territories.values()) {
            if (t.getWorldName().equals(newTerritory.getWorldName())) {
                if (isOverlapping(t, newTerritory)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOverlapping(Territory a, Territory b) {
        return a.getMinX() <= b.getMaxX() && a.getMaxX() >= b.getMinX()
                && a.getMinY() <= b.getMaxY() && a.getMaxY() >= b.getMinY()
                && a.getMinZ() <= b.getMaxZ() && a.getMaxZ() >= b.getMinZ();
    }
}
