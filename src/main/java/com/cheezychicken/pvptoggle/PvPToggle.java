package com.cheezychicken.pvptoggle;
import com.sk89q.worldguard.bukkit.protection.events.DisallowedPVPEvent;

import nu.nerd.nerdboard.NerdBoard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

// ----------------------------------------------------------------------------------------------------------
/**
 * The main plugin class.
 */
public class PvPToggle extends JavaPlugin implements Listener {

    // ------------------------------------------------------------------------------------------------------
    /**
     * Stores the UUIDs of the players currently pvping.
     */
    private static final Set<UUID> ENABLED_PLAYERS = new HashSet<>();

    // ------------------------------------------------------------------------------------------------------

    // ------------------------------------------------------------------------------------------------------
    /**
     * Stores the UUIDs of the players with PVP persisting.
     */
    private static final Set<UUID> PERSISTED_PLAYERS = new HashSet<>();
    
    // ------------------------------------------------------------------------------------------------------
    
    
    /**
     * @see JavaPlugin#onEnable().
     */
    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
      
        NerdBoard nerdBoard = NerdBoardHook.getNerdBoard();
        if (nerdBoard != null) {
            new NerdBoardHook(nerdBoard);
        } else {
            //log("NerdBoard is required. http://github.com/nerdnu/NerdBoard");
            getPluginLoader().disablePlugin(this);
            return;
        }

    /**    for (String uuidString : getConfig().getStringList("players")) {
    *        try {
    *            UUID uuid = UUID.fromString(uuidString);
    *            ENABLED_PLAYERS.add(uuid);
    *            log("Loaded serialized player: " + uuidString);
    *        } catch (IllegalArgumentException e) {y/pvp
    *           log("Invalid UUID found in config: " + uuidString);
    *        }
    *    }
    */    
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * @see JavaPlugin#onDisable().
     *
    *@Override
    *public void onDisable() {
    *    List<String> serializePlayers = ENABLED_PLAYERS.stream()
    *                                                   .map(UUID::toString)
    *                                                  .collect(Collectors.toList());
    *    getConfig().set("players", serializePlayers);
    *    saveConfig();
    *}
	*/
    // ------------------------------------------------------------------------------------------------------
    /**
     * Handles commands.
     *
     * @see JavaPlugin#onCommand(CommandSender, Command, String, String[]).
     */
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {

        if (command.getName().equalsIgnoreCase("pvp")) {
            if (args.length == 0 || !sender.hasPermission("pvp.toggle")) {
                return false;
                
            } else {
           
            if (args[0].equalsIgnoreCase("on")) {
            	if (args.length == 1) {
                Player player = (Player) sender;
                pvpStatusOn(player, "Player");
                return true;
                
            	} else if (args[1].equalsIgnoreCase("persist")) {
            		Player player = (Player) sender;
					UUID uuid = player.getUniqueId();
            		PERSISTED_PLAYERS.add(uuid);
            		pvpStatusOn(player, "Player");
            		return true;
            	
                // Admins can change other people's
            	} else if (sender.hasPermission("pvp.others")) {
            		Player player = Bukkit.getPlayer(args[1]);
                    pvpStatusOn(player, "Admin");
                    return true;
                    
            	} else {
            		return false;
            	}
                
            } else if (args[0].equalsIgnoreCase("off")) {
            	if (args.length == 1) {
                    Player player = (Player) sender;
                    pvpStatusOff(player, "Player");
                    return true;
                    // Admins can change other people's
                	} else if (sender.hasPermission("pvp.others")) {
                		Player player = Bukkit.getPlayer(args[1]);
                        pvpStatusOff(player, "Admin");
                        return true;		
                	} else {
                		return false;
                	}
            } else if (args[0].equalsIgnoreCase("list")) {
                String playerList = "Players with PvP active: ";
                if (ENABLED_PLAYERS.isEmpty()) {
                    playerList += "None!";
                } else {
                    playerList += ENABLED_PLAYERS.stream()
                                                 .map(getServer()::getPlayer)
                                                 .map(Player::getName)
                                                 .collect(Collectors.joining(", "));
                }
                sender.sendMessage(playerList);
                return true;
            }
            
        		
            }
            return false;
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * Prevent WorldGuard from disabling PvP for two players with pvp on.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    private void onPVPDamage(DisallowedPVPEvent event) {
        Player defender = event.getDefender();
        Player attacker = event.getAttacker();
        if (isActive(defender) && isActive(attacker)) {
            event.setCancelled(true);
        }
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * Turn off PvP for a player when they die.
     * @param player the player
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (isActive(player) && !isPersisted(player)) {
            pvpStatusOff(player, "Death");
        }
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * Turn off PvP for a player when they log out.
     * @param player the player
     */
    
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent player) {
    	Player leaver = player.getPlayer();
        if (isActive(leaver)) {
            pvpStatusOff(leaver, "Leave");
        }
    }
    
    
    // ------------------------------------------------------------------------------------------------------
    /**
     * Turn off PvP for a player when they are kicked.
     * @param player the player
     */
    
    @EventHandler
    public void onPlayerKick(PlayerKickEvent player) {
    	Player leaver = player.getPlayer();
        if (isActive(leaver)) {
            pvpStatusOff(leaver, "Leave");
        }
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * Turns the player's hunted status on
     * @param player the player
     * @param reason Reason for change
     */
    private void pvpStatusOn(Player player, String reason) {
        UUID uuid = player.getUniqueId();
        String msg = "";
        if (isActive(player)) {
        	if(reason == "Player") {
        	player.sendMessage("PvP is already enabled for you!");
        	}
        } else {
            if(reason == "Player") {
            	msg = ChatColor.RED + player.getName() + " has turned their PvP on." + ChatColor.RESET;
            } else if (reason == "Admin") {
            	msg = ChatColor.RED + player.getName() + " has had their PvP turned on." + ChatColor.RESET;
            }
            ENABLED_PLAYERS.add(uuid);
            if (msg != "") {
            	getServer().broadcastMessage(msg);
            }
            NerdBoardHook.checkPvPstate(player);
        }

    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * Turns the player's hunted status off
     * @param player the player
     * @param reason Reason for change
     */
    
    private void pvpStatusOff(Player player, String reason) {
        UUID uuid = player.getUniqueId();
        String msg = "";
        if (isActive(player)) {
        	if(reason == "Player") {
            	msg = ChatColor.RED + player.getName() + " has turned their PvP off." + ChatColor.RESET;
            } else if (reason == "Admin") {
            	msg = ChatColor.RED + player.getName() + " has had their PvP turned off." + ChatColor.RESET;
            }
        	if(isPersisted(player)) {
        		PERSISTED_PLAYERS.remove(uuid);
        	}
            ENABLED_PLAYERS.remove(uuid);
            if (msg != "") {
            	getServer().broadcastMessage(msg);
            }
            NerdBoardHook.checkPvPstate(player);
        } else {
        	if(reason == "Player") {
        	player.sendMessage("PvP is already disabled for you!");
        	}
        }
      
    }

    // ------------------------------------------------------------------------------------------------------
    /**
     * @param player the player.
     * @return true if the player has pvp on
     */
    public static boolean isActive(Player player) {
        return ENABLED_PLAYERS.contains(player.getUniqueId());
    }
    // ------------------------------------------------------------------------------------------------------
    /**
     * @param player the player.
     * @return true if the player has pvp persisted
     */
    public static boolean isPersisted(Player player) {
        return PERSISTED_PLAYERS.contains(player.getUniqueId());
    }
    
}
