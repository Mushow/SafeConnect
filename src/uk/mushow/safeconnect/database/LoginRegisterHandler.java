package uk.mushow.safeconnect.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import uk.mushow.safeconnect.SafeConnectPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginRegisterHandler {

    private final Connection connection;
    private final SafeConnectPlugin plugin;
    private final Map<UUID, BukkitTask> uuidBukkitTaskMap = new HashMap<>();

    public LoginRegisterHandler(Connection connection, SafeConnectPlugin plugin) {
        this.plugin = plugin;
        this.connection = connection;
    }

    public Map<UUID, BukkitTask> getUuidBukkitTaskMap() {
        return this.uuidBukkitTaskMap;
    }

    public boolean registrationMessage(Player player, String commandName) {
        return sendConfigTryMessages(player, commandName);
    }

    private boolean sendConfigTryMessages(Player player, String commandName) {
        String playerName = player.getName();
        String messageKey;
        boolean isRegistered;

        if(commandName.equalsIgnoreCase("login")) {
            messageKey = "messages.login_try";
            isRegistered = isRegistered(playerName);
        } else if (commandName.equalsIgnoreCase("register")) {
            messageKey = "messages.register_try";
            isRegistered = !isRegistered(playerName);
        } else {
            return false;
        }

        if (!isRegistered) {
            player.sendMessage(this.plugin.applyColorCodes(this.plugin.getConfig(messageKey)));
            return true;
        }

        return false;
    }

    public boolean isRegistered(String playerName) {
        try {
            String tableName = this.plugin.getConfig("database.table");
            String query = "SELECT * FROM " + tableName + " WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[@#$%^&+=].*");
    }

    public Connection getConnection() {
        return connection;
    }

    public void success(Player player) {
        getUuidBukkitTaskMap().get(player.getUniqueId()).cancel();
        getUuidBukkitTaskMap().remove(player.getUniqueId());
        redirectPlayer(player);
    }

    private void showPlayers(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            player.showPlayer(p);
        }
    }

    private void redirectPlayer(Player player) {
        Location location = this.plugin.getRedirectLocation();
        showPlayers(player);
        unsetupPlayer(player);
        player.teleport(location);
    }

    private void unsetupPlayer(Player player) {
        player.setWalkSpeed(0.2f);
    }

}
