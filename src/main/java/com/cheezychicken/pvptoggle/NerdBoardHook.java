package com.cheezychicken.pvptoggle;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import nu.nerd.nerdboard.NerdBoard;

// ----------------------------------------------------------------------------
/**
 * A class which encapsulates all of this plugin's hooked functionality
 * dependent on the NerdBoard plugin.
 */
public class NerdBoardHook {
    // ------------------------------------------------------------------------
    /**
     * The NerdBoard instance.
     */
    private static NerdBoard _nerdBoard;

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param nerdBoard the NerdBoard plugin.
     */
    NerdBoardHook(NerdBoard nerdBoard) {
        _nerdBoard = nerdBoard;
        _scoreboard = nerdBoard.getScoreboard();

        _pvpTeam = configureTeam("PvP Enabled", ChatColor.RED);
        _defaultTeam = configureTeam("Default", null);
    }


    // ------------------------------------------------------------------------
    /**
     * Configures a {@link Team} with the given name, color, and collision
     * status.
     *
     * @param name the name of the team.
     * @param color (nullable) the team's color (i.e. player name color).
     * 
     * @return a {@link Team} with the given properties.
     */
    private Team configureTeam(String name, ChatColor color) {
        Team team = getOrCreateTeam(name);
        if (color != null) {
            team.setColor(color);
        }
        return team;
    }

    // ------------------------------------------------------------------------
    /**
     * Returns the NerdBoard plugin if found, otherwise null.
     *
     * @return the NerdBoard plugin if found, otherwise null.
     */
    static NerdBoard getNerdBoard() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("NerdBoard");
        if (plugin instanceof NerdBoard) {
            return (NerdBoard) plugin;
        }
        return null;
    }

    // ------------------------------------------------------------------------
    /**
     * Look up a Team by name in the Scoreboard, or create a new one with the
     * specified name if not found.
     *
     * @param name the Team name.
     * @return the Team with that name.
     */
    private Team getOrCreateTeam(String name) {
        Team team = _scoreboard.getTeam(name);
        if (team == null) {
            team = _nerdBoard.addTeam(name);
        }
        return team;
    }

    // ------------------------------------------------------------------------
    /**
     * Assign the player to the Team that corresponds to its pvp
     * state, and then update their scoreboard if necessary.
     *
     * The Team controls the name tag prefix (colour)
     *
     * @param player the player.
     */
    public static void checkPvPstate(Player player) {
        boolean inPvPmode = PvPToggle.isActive(player);
        Team team = inPvPmode ? _pvpTeam
                                            : _defaultTeam;
        _nerdBoard.addPlayerToTeam(team, player);
        if (player.getScoreboard() != _scoreboard) {
            player.setScoreboard(_scoreboard);
        }
    }

    /**
     * Translates a boolean to an
     * {@link org.bukkit.scoreboard.Team.OptionStatus} with the mapping: true ->
     * OptionStatus.ALWAYS false -> OptionStatus.NEVER
     *
     * @param bool the boolean.
     * @return the translated OptionStatus.
     *
    private static Team.OptionStatus boolToStatus(boolean bool) {
        return bool ? Team.OptionStatus.ALWAYS : Team.OptionStatus.NEVER;
    }
	*/
    /**
     * Scoreboard API stuff for colored name tags
     */
    private static Scoreboard _scoreboard;

    private static Team _pvpTeam;

    /**
     * Players who are not in pvp are in this team
     */
    private static Team _defaultTeam;

} // NerdBoardHook