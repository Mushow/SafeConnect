package uk.mushow.safeconnect.commands.interfaces;

import org.bukkit.entity.Player;

public interface LoginRegisterInteraction {

    void databaseOperation(Player player, String... passwords);

    void handleSuccessfulConnection(Player player);

}