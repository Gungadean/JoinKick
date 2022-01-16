package sr.will.joinkick;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.logging.Logger;

@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, description = PluginInfo.DESCRIPTION)
public class JoinKick {
    private final ProxyServer proxy;
    private final Logger logger;

    private HashMap<String, RegisteredServer> pendingConnections = new HashMap<>();

    @Inject
    public JoinKick(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        logger.info("JoinKick Loaded!");
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        for (Player player : proxy.getAllPlayers()) {
            // Checks if the user logging in is currently connected to the proxy
            if (player.getUsername().equals(event.getUsername())) {
                // Put the server the player was connected to in the pendingconnections list so that we can reconnect them to that server
                if (player.getCurrentServer().isPresent()) {
                    pendingConnections.put(player.getUsername(), player.getCurrentServer().get().getServer());
                }

                // Disconnect old player
                player.disconnect(Component.text("You have logged in from another location", NamedTextColor.RED));
                return;
            }
        }
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        // If the user is in the pending connections list, connect them to the server there instead of the default server
        if (pendingConnections.containsKey(event.getPlayer().getUsername())) {
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(pendingConnections.get(event.getPlayer().getUsername())));
            // Remove the player from the pending connections list
            pendingConnections.remove(event.getPlayer().getUsername());
        }
    }
}
