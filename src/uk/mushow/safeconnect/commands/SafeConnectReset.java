package uk.mushow.safeconnect.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import uk.mushow.safeconnect.SafeConnectPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SafeConnectReset implements CommandExecutor {

    private final Connection connection;
    private final SafeConnectPlugin plugin;

    public SafeConnectReset(Connection connection, SafeConnectPlugin safeConnectPlugin) {
        this.connection = connection;
        this.plugin = safeConnectPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(plugin.getConfig("messages.reset_usage"));
            return false;
        }

        resetPassword(args[0], sender);
        return false;
    }

    private void deletePlayerFromDatabase(String playerName, String tableName) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE username = ?");
            statement.setString(1, playerName);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void resetPassword(String playerName, CommandSender sender) {
        try {
            String tableName = plugin.getConfig("database.table");
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE username = ?");
            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                deletePlayerFromDatabase(playerName, tableName);
                success(playerName, sender);
            } else {
                error(playerName, sender);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void error(String playerName, CommandSender sender) {
        sender.sendMessage(plugin.applyColorCodes(plugin.getConfig("messages.player_not_found") + playerName));

    }

    private void success(String playerName, CommandSender sender) {
        sender.sendMessage(plugin.applyColorCodes(plugin.getConfig("messages.reset") + playerName));
    }

}
