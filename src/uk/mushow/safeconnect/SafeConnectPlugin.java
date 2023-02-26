package uk.mushow.safeconnect;

import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import uk.mushow.safeconnect.commands.SafeConnectLogin;
import uk.mushow.safeconnect.commands.SafeConnectRegister;
import uk.mushow.safeconnect.commands.SafeConnectReset;
import uk.mushow.safeconnect.database.LoginRegisterHandler;
import uk.mushow.safeconnect.database.SafeConnectDatabase;
import uk.mushow.safeconnect.listener.SafeConnectListener;

import java.sql.Connection;

public class SafeConnectPlugin extends JavaPlugin {

    private LoginRegisterHandler handler;
    private Location location;
    private Location redirectLocation;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        SafeConnectDatabase safeConnectDatabase = new SafeConnectDatabase(this);
        Connection connection = safeConnectDatabase.init();

        handler = new LoginRegisterHandler(connection, this);
        registerEvents();
        registerCommands();
        createWorld();
    }

    private void createWorld() {
        String worldName = getConfig("world.name");
        World spawnWorld = Bukkit.getWorld(worldName);

        createWorldIfNotExist(worldName, spawnWorld);

        setSpawnLocation(spawnWorld);
        setRedirectLocation();
    }

    private void createWorldIfNotExist(String worldName, World spawnWorld) {
        if(spawnWorld != null) return;

        WorldCreator wc = new WorldCreator(worldName);
        wc.type(WorldType.FLAT);
        wc.generatorSettings("2;0;1;");
        wc.createWorld();
    }

    private void setSpawnLocation(World spawnWorld) {
        this.location = new Location(spawnWorld, 0, 30, 0, 90f, 10f);
        spawnWorld.setTime(13800);
        this.location.clone().add(0, -1, 0).getBlock().setType(Material.BARRIER);
    }

    private void setRedirectLocation() {
        World world = Bukkit.getWorld(getConfig("redirect.world"));
        int x = Integer.parseInt(getConfig("redirect.x"));
        int y = Integer.parseInt(getConfig("redirect.y"));
        int z = Integer.parseInt(getConfig("redirect.z"));
        this.redirectLocation = new Location(world, x, y, z);
    }

    @Override
    public void onDisable() {}

    public String getConfig(String path) {
        return getConfig().getString(path);
    }

    public String applyColorCodes(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new SafeConnectListener(handler, this), this);
    }

    private void registerCommands() {
        getServer().getPluginCommand("register").setExecutor(new SafeConnectRegister(handler, this));
        getServer().getPluginCommand("login").setExecutor(new SafeConnectLogin(handler, this));
        getServer().getPluginCommand("reset").setExecutor(new SafeConnectReset(handler.getConnection(), this));
    }

    public Location getLocation() {
        return this.location;
    }

    public Location getRedirectLocation() {
        return redirectLocation;
    }

}
