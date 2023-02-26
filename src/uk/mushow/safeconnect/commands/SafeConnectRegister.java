package uk.mushow.safeconnect.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.mushow.safeconnect.SafeConnectPlugin;
import uk.mushow.safeconnect.commands.interfaces.LoginRegisterInteraction;
import uk.mushow.safeconnect.database.LoginRegisterHandler;
import uk.mushow.safeconnect.utils.ArgonUtil;

public class SafeConnectRegister implements CommandExecutor, LoginRegisterInteraction {

    private final LoginRegisterHandler handler;
    private final Connection connection;
    private final SafeConnectPlugin plugin;

    public SafeConnectRegister(LoginRegisterHandler loginRegisterHandler, SafeConnectPlugin plugin) {
        this.handler = loginRegisterHandler;
        this.connection = handler.getConnection();
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        if (!handler.getUuidBukkitTaskMap().containsKey(player.getUniqueId())) {
            return true;
        }

        if (handler.registrationMessage(player, command.getName())) {
            return true;
        }

        if (args.length != 2) {
            return false;
        }

        databaseOperation(player, args[0], args[1]);
        return false;
    }

    @Override
    public void databaseOperation(Player player, String... passwords) {
        if(!isPasswordValid(player, passwords)) return;

        insertInDatabase(player, passwords[0]);
    }

    private boolean isPasswordValid(Player player, String[] passwords) {
        return passwordsMatch(passwords[0], passwords[1], player) && isValidPassword(passwords[0], player);
    }

    private boolean passwordsMatch(String password1, String password2, Player player) {
        if (!password1.equals(password2)) {
            player.sendMessage(plugin.applyColorCodes(plugin.getConfig("messages.register_failed")));
            return false;
        }
        return true;
    }

    private boolean isValidPassword(String password, Player player) {
        if (!handler.isValidPassword(password)) {
            player.sendMessage(plugin.applyColorCodes(plugin.getConfig("messages.password_requirements")));
            return false;
        }
        return true;
    }

    private void insertInDatabase(Player player, String password) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + plugin.getConfig("database.table") + " (username, password) VALUES (?, ?)")) {
            statement.setString(1, player.getName());
            statement.setString(2, ArgonUtil.hashPassword(password));
            statement.executeUpdate();
            handleSuccessfulConnection(player);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleSuccessfulConnection(Player player) {
        player.sendMessage(plugin.applyColorCodes(plugin.getConfig("messages.register_success")));
        handler.success(player);
    }

}
