package top.mcocet.rusTerritory.models;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;
import java.util.UUID;

public class Territory {
    private String id;
    private String name;
    private String worldName;
    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;
    private UUID owner;
    private boolean pvp;
    private boolean build;
    private boolean destroy;
    private boolean interact;
    private boolean enter;
    private String enterMessage;
    private String leaveMessage;

    public Territory(String name, String worldName, int minX, int minY, int minZ,
                     int maxX, int maxY, int maxZ, UUID owner) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.worldName = worldName;
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
        this.owner = owner;
        // 默认保护设置: 禁止PVP/建造/破坏/交互，允许进入
        this.pvp = false;
        this.build = false;
        this.destroy = false;
        this.interact = false;
        this.enter = true;
        this.enterMessage = null;
        this.leaveMessage = null;
    }

    public Territory() {}

    public boolean contains(Location location) {
        if (location.getWorld() == null || !location.getWorld().getName().equals(worldName)) return false;
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public Location getCenter(World world) {
        double cx = (minX + maxX) / 2.0 + 0.5;
        double cy = minY;
        double cz = (minZ + maxZ) / 2.0 + 0.5;
        return new Location(world, cx, cy, cz);
    }

    public long getVolume() {
        return (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }
    public int getMinX() { return minX; }
    public void setMinX(int minX) { this.minX = minX; }
    public int getMinY() { return minY; }
    public void setMinY(int minY) { this.minY = minY; }
    public int getMinZ() { return minZ; }
    public void setMinZ(int minZ) { this.minZ = minZ; }
    public int getMaxX() { return maxX; }
    public void setMaxX(int maxX) { this.maxX = maxX; }
    public int getMaxY() { return maxY; }
    public void setMaxY(int maxY) { this.maxY = maxY; }
    public int getMaxZ() { return maxZ; }
    public void setMaxZ(int maxZ) { this.maxZ = maxZ; }
    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }
    public boolean isPvp() { return pvp; }
    public void setPvp(boolean pvp) { this.pvp = pvp; }
    public boolean isBuild() { return build; }
    public void setBuild(boolean build) { this.build = build; }
    public boolean isDestroy() { return destroy; }
    public void setDestroy(boolean destroy) { this.destroy = destroy; }
    public boolean isInteract() { return interact; }
    public void setInteract(boolean interact) { this.interact = interact; }
    public boolean isEnter() { return enter; }
    public void setEnter(boolean enter) { this.enter = enter; }
    public String getEnterMessage() { return enterMessage; }
    public void setEnterMessage(String enterMessage) { this.enterMessage = enterMessage; }
    public String getLeaveMessage() { return leaveMessage; }
    public void setLeaveMessage(String leaveMessage) { this.leaveMessage = leaveMessage; }

    public static class Serializer implements JsonSerializer<Territory>, JsonDeserializer<Territory> {
        @Override
        public JsonElement serialize(Territory src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", src.id);
            obj.addProperty("name", src.name);
            obj.addProperty("world", src.worldName);
            obj.addProperty("minX", src.minX);
            obj.addProperty("minY", src.minY);
            obj.addProperty("minZ", src.minZ);
            obj.addProperty("maxX", src.maxX);
            obj.addProperty("maxY", src.maxY);
            obj.addProperty("maxZ", src.maxZ);
            obj.addProperty("owner", src.owner != null ? src.owner.toString() : null);
            obj.addProperty("pvp", src.pvp);
            obj.addProperty("build", src.build);
            obj.addProperty("destroy", src.destroy);
            obj.addProperty("interact", src.interact);
            obj.addProperty("enter", src.enter);
            obj.addProperty("enterMessage", src.enterMessage);
            obj.addProperty("leaveMessage", src.leaveMessage);
            return obj;
        }

        @Override
        public Territory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            Territory t = new Territory();
            t.id = obj.get("id").getAsString();
            t.name = obj.get("name").getAsString();
            t.worldName = obj.get("world").getAsString();
            t.minX = obj.get("minX").getAsInt();
            t.minY = obj.get("minY").getAsInt();
            t.minZ = obj.get("minZ").getAsInt();
            t.maxX = obj.get("maxX").getAsInt();
            t.maxY = obj.get("maxY").getAsInt();
            t.maxZ = obj.get("maxZ").getAsInt();
            String ownerStr = obj.has("owner") && !obj.get("owner").isJsonNull() ? obj.get("owner").getAsString() : null;
            t.owner = ownerStr != null ? UUID.fromString(ownerStr) : null;
            t.pvp = obj.has("pvp") ? obj.get("pvp").getAsBoolean() : false;
            t.build = obj.has("build") ? obj.get("build").getAsBoolean() : false;
            t.destroy = obj.has("destroy") ? obj.get("destroy").getAsBoolean() : false;
            t.interact = obj.has("interact") ? obj.get("interact").getAsBoolean() : false;
            t.enter = !obj.has("enter") || obj.get("enter").getAsBoolean();
            t.enterMessage = obj.has("enterMessage") && !obj.get("enterMessage").isJsonNull() ? obj.get("enterMessage").getAsString() : null;
            t.leaveMessage = obj.has("leaveMessage") && !obj.get("leaveMessage").isJsonNull() ? obj.get("leaveMessage").getAsString() : null;
            return t;
        }
    }
}
