package uk.mushow.safeconnect.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import uk.mushow.safeconnect.SafeConnectPlugin;
import uk.mushow.safeconnect.database.LoginRegisterHandler;

import java.util.Map;
import java.util.UUID;

public class SafeConnectListener implements Listener {

    private final LoginRegisterHandler handler;
    private final SafeConnectPlugin plugin;

    public SafeConnectListener(LoginRegisterHandler loginRegisterHandler, SafeConnectPlugin plugin) {
        this.handler = loginRegisterHandler;
        this.plugin = plugin;
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.teleport(this.plugin.getLocation());

        sendLoginOrRegister(player);
        setupPlayer(player);
        event.setJoinMessage(null);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        Map<UUID, BukkitTask> taskMap = handler.getUuidBukkitTaskMap();
        taskMap.remove(playerUUID);
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (shouldPreventMovement(player)) {
            Location from = event.getFrom();
            player.teleport(from);
        }
    }

    private boolean shouldPreventMovement(Player player) {
        return handler.getUuidBukkitTaskMap().containsKey(player.getUniqueId());
    }

    @EventHandler
    private void onWeatherChange(WeatherChangeEvent event) {
        if(event.getWorld().getName().equalsIgnoreCase(this.plugin.getConfig("world.name"))) {
            event.setCancelled(true);
        }
    }

    private void setupPlayer(Player player) {
        player.setWalkSpeed(0f);
        player.setVelocity(new Vector(0, 0, 0));
        player.setFlySpeed(0f);
        hidePlayers(player);
    }

    private void hidePlayers(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            player.hidePlayer(p);
        }
    }


    private void sendLoginOrRegister(Player player) {
        if (!handler.isRegistered(player.getName())) {
            sendSafeConnect(player, plugin.getConfig("messages.register"));
        } else {
            sendSafeConnect(player, plugin.getConfig("messages.login"));
        }
    }

    private void sendSafeConnect(Player player, String message) {
        int interval = Integer.parseInt(plugin.getConfig("messages.interval"));
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin,
                () -> player.sendMessage(plugin.applyColorCodes(message)),
                0L, interval * 20L);
        UUID playerUUID = player.getUniqueId();
        handler.getUuidBukkitTaskMap().put(playerUUID, bukkitTask);
    }

}
