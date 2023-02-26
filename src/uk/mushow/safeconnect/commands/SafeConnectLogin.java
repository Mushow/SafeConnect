package uk.mushow.safeconnect.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.mushow.safeconnect.SafeConnectPlugin;
import uk.mushow.safeconnect.commands.interfaces.LoginRegisterInteraction;
import uk.mushow.safeconnect.database.LoginRegisterHandler;
import uk.mushow.safeconnect.utils.ArgonUtil;

public class SafeConnectLogin implements CommandExecutor, LoginRegisterInteraction {

    private final LoginRegisterHandler handler;
    private final Connection connection;
    private final SafeConnectPlugin plugin;
    private final String tableName;

    public SafeConnectLogin(LoginRegisterHandler handler, SafeConnectPlugin plugin) {
        this.handler = handler;
        this.connection = handler.getConnection();
        this.plugin = plugin;
        this.tableName = plugin.getConfig().getString("database.table");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (!handler.getUuidBukkitTaskMap().containsKey(player.getUniqueId()) || handler.registrationMessage(player, command.getName())) {
            return true;
        }

        if (args.length == 1 && verifyPasswordFromDatabase(player, args[0])) {
            handleSuccessfulConnection(player);
            return true;
        }

        player.sendMessage(plugin.applyColorCodes(plugin.getConfig().getString("messages.login_failed")));
        return false;
    }

    @Override
    public void databaseOperation(Player player, String... password) {
        if (verifyPasswordFromDatabase(player, password[0])) {
            handleSuccessfulConnection(player);
        }
    }

    @Override
    public void handleSuccessfulConnection(Player player) {
        player.sendMessage(plugin.applyColorCodes(plugin.getConfig().getString("messages.login_success")));
        handler.success(player);
    }

    private boolean verifyPasswordFromDatabase(Player player, String password) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT password FROM " + tableName + " WHERE username = ?")) {
            statement.setString(1, player.getName());
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return ArgonUtil.verifyPassword(rs.getString("password"), password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
