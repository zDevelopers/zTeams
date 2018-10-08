/*
 * Plugin UHCReloaded : Alliances
 *
 * Copyright ou © ou Copr. Amaury Carrade (2016)
 * Idées et réflexions : Alexandre Prokopowicz, Amaury Carrade, "Vayan".
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package fr.zcraft.zteams;

import fr.zcraft.zlib.core.ZLib;
import fr.zcraft.zlib.core.ZLibComponent;
import fr.zcraft.zteams.colors.TeamColor;
import fr.zcraft.zteams.events.TeamChangedEvent;
import fr.zcraft.zteams.events.TeamRegisteredEvent;
import fr.zcraft.zteams.events.TeamUnregisteredEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;


/**
 * This component must be registered inside your project to use teams.
 *
 * {@code T} is your own {@link ZTeam} subclass.
 */
public class ZTeams<T extends ZTeam> extends ZLibComponent implements Listener
{
    private static ZTeams instance;

    private Scoreboard scoreboard = null;

    private boolean bannerShapeWriteLetter = true;
    private boolean bannerShapeAddBorder = false;

    private boolean teamsOptionsSeeFriendlyInvisibles = true;
    private boolean teamsOptionsFriendlyFire = true;
    private boolean teamsOptionsColorizeChat = true;

    private int maxPlayersPerTeam = 0;

    private Set<T> teams = new HashSet<>();

    @Override
    protected void onEnable()
    {
        instance = this;
    }


    /**
     * @return This component's instance.
     */
    public static ZTeams get()
    {
        return instance;
    }


    /* *** Teams getters *** */

    /**
     * Finds a team by name (ignoring case and extra spaces around).
     *
     * @param name The team name.
     * @return The {@link ZTeam}, or {@code null} if not found.
     */
    public T getTeamByName(final String name)
    {
        return teams.stream().filter(team -> team.getName().trim().equalsIgnoreCase(name)).findFirst().orElse(null);
    }


    /**
     * Finds the team of the given player, if it exists.
     *
     * @param player The player.
     * @return The team, or {@code null} if this player is not in a team.
     */
    public T getTeamForPlayer(final OfflinePlayer player)
    {
        return getTeamForPlayer(player.getUniqueId());
    }

    /**
     * Finds the team of the given player, if it exists.
     *
     * @param playerID The player's UUID.
     * @return The team, or {@code null} if this player is not in a team.
     */
    public T getTeamForPlayer(final UUID playerID)
    {
        return teams.stream().filter(team -> team.containsPlayer(playerID)).findFirst().orElse(null);
    }


    /**
     * Removes a player form its team, if he had any.
     *
     * @param player The player.
     * @return {@code true} if the player was actually removed from a team.
     */
    public boolean removePlayerFromTeam(final OfflinePlayer player)
    {
        return removePlayerFromTeam(player, false);
    }

    /**
     * Removes a player form its team, if he had any.
     *
     * @param playerID The player's UUID.
     * @return {@code true} if the player was actually removed from a team.
     */
    public boolean removePlayerFromTeam(final UUID playerID)
    {
        return removePlayerFromTeam(playerID, false);
    }

    /**
     * Removes a player form its team, if he had any.
     *
     * @param player The player.
     * @param becauseJoin {@code true} if the player is removed to go into another team.
     * @return {@code true} if the player was actually removed from a team.
     */
    boolean removePlayerFromTeam(final OfflinePlayer player, boolean becauseJoin)
    {
        return removePlayerFromTeam(player.getUniqueId(), becauseJoin);
    }

    /**
     * Removes a player form its team, if he had any.
     *
     * @param playerID The player's UUID.
     * @param becauseJoin {@code true} if the player is removed to go into another team.
     * @return {@code true} if the player was actually removed from a team.
     */
    boolean removePlayerFromTeam(final UUID playerID, boolean becauseJoin)
    {
        T team = getTeamForPlayer(playerID);

        if (team != null)
        {
            team.removePlayer(playerID, becauseJoin);
            return true;
        }

        else return false;
    }



    /* *** Teams registration *** */


    /**
     * Registers a new team.
     *
     * @param team The team.
     */
    public void registerTeam(T team)
    {
        teams.add(team);
        fireEvent(new TeamRegisteredEvent(team));
    }

    /**
     * Unregisters a team.
     *
     * @param team The team.
     * @return {@code true} if the team was actually unregistered.
     */
    public boolean unregisterTeam(T team)
    {
        final boolean actuallyUnregistered = teams.remove(team);
        fireEvent(new TeamUnregisteredEvent(team));

        return actuallyUnregistered;
    }

    /**
     * Unregisters all the teams.
     */
    public void unregisterAll()
    {
        new HashSet<>(teams).forEach(ZTeam::deleteTeam);
    }



    /* *** ZTeams internal management methods *** */


    void colorizePlayer(final OfflinePlayer offlinePlayer)
    {
        if (!teamsOptionsColorizeChat)
        {
            return;
        }

        if (!offlinePlayer.isOnline())
        {
            return;
        }

        final Player player = (Player) offlinePlayer;
        final T team = getTeamForPlayer(player);

        if (team == null)
        {
            player.setDisplayName(player.getName());
        }
        else
        {
            player.setDisplayName(team.getColorOrWhite().toChatColor() + player.getName() + ChatColor.RESET);
        }
    }

    /**
     * Generates a color from the given color.
     * <p>
     * If the color is neither {@link TeamColor#RANDOM} nor {@code null}, returns the given color.<br />
     * Else, generates a random unused (if possible) color.
     *
     * @param color The color to be generated.
     * @return A non-random color.
     */
    TeamColor generateColor(TeamColor color)
    {
        if (color != null && color != TeamColor.RANDOM)
        {
            return color;
        }

        // A list of the currently used colors.
        final Set<TeamColor> availableColors = new HashSet<>(Arrays.asList(TeamColor.values()));
        availableColors.remove(TeamColor.RANDOM);

        teams.stream().map(T::getColorOrWhite).forEach(availableColors::remove);

        if (availableColors.size() != 0)
        {
            return (TeamColor) availableColors.toArray()[(new Random()).nextInt(availableColors.size())];
        }
        else
        {
            // length - 1 so the RANDOM option is never selected.
            return TeamColor.values()[(new Random()).nextInt(TeamColor.values().length - 1)];
        }
    }



    /* *** Events *** */

    @EventHandler
    public void onTeamChanged(final TeamChangedEvent ev)
    {
        // TODO updateGUIs();
    }



    /* *** ZTeams configuration methods *** */


    /**
     * @return The Minecraft scoreboard the teams will use.
     */
    public Scoreboard getScoreboard()
    {
        return scoreboard != null ? scoreboard : ZLib.getPlugin().getServer().getScoreboardManager().getMainScoreboard();
    }

    /**
     * Sets the scoreboard to be used by the teams to be displayed and registered into Bukkit.
     *
     * If this is never set, or reset by setting the scoreboard to {@code null}, the main Bukkit scoreboard
     * will be used.
     *
     * @param scoreboard The scoreboard to use.
     */
    public static void setScoreboard(final Scoreboard scoreboard)
    {
        get().scoreboard = scoreboard;
    }

    /**
     * @return {@code true} if the first meaningful letter of the team name should be written
     * on the default team banners.
     */
    boolean bannerShapeWriteLetter()
    {
        return bannerShapeWriteLetter;
    }

    /**
     * @return {@code true} if a border should be added to the default team banners.
     */
    boolean bannerShapeAddBorder()
    {
        return bannerShapeAddBorder;
    }

    /**
     * @return {@code true} if the players should see their invisible teammates.
     */
    boolean teamsOptionsSeeFriendlyInvisibles()
    {
        return teamsOptionsSeeFriendlyInvisibles;
    }

    /**
     * @return {@code true} if the PvP should be enabled between teammates.
     */
    boolean teamsOptionsFriendlyFire()
    {
        return teamsOptionsFriendlyFire;
    }

    boolean teamsOptionsColorizeChat()
    {
        return teamsOptionsColorizeChat;
    }

    /**
     * @return The maximal number of players per team. {@code 0} means “no limit”.
     */
    int maxPlayersPerTeam()
    {
        return maxPlayersPerTeam;
    }

    /**
     * Updates the settings followed to generate the default teams banners.
     *
     * All default banners will be regenerated when this method is called, according to the new settings.
     *
     * @param bannerShapeWriteLetter {@code true} if the first meaningful letter of the team name
     *                               should be written on the default team banners.
     * @param bannerShapeAddBorder {@code true} if a border should be added to the default team banners.
     */
    public static void setBannerOptions(boolean bannerShapeWriteLetter, final boolean bannerShapeAddBorder)
    {
        get().bannerShapeWriteLetter = bannerShapeWriteLetter;
        get().bannerShapeAddBorder = bannerShapeAddBorder;

        get().updateDefaultBanners();
    }

    /**
     * Updates the teams options for all teams.
     *
     * All teams settings will be updated when this method is called.
     *
     * @param teamsOptionsSeeFriendlyInvisibles {@code true} if the players should see their invisible teammates.
     * @param teamsOptionsFriendlyFire {@code true} if the PvP should be enabled between teammates.
     * @param teamsOptionsColorizeChat {@code true} to colorize the players name in the chat (by setting their display
     *                                 name including their team color).
     */
    public static void setTeamsOptions(boolean teamsOptionsSeeFriendlyInvisibles, boolean teamsOptionsFriendlyFire, boolean teamsOptionsColorizeChat)
    {
        get().teamsOptionsSeeFriendlyInvisibles = teamsOptionsSeeFriendlyInvisibles;
        get().teamsOptionsFriendlyFire = teamsOptionsFriendlyFire;
        get().teamsOptionsColorizeChat = teamsOptionsColorizeChat;

        get().updateTeamsOptions();
    }

    /**
     * Updates the maximal amount of players per team. If some teams overflow the new limits, players will not be kicked
     * but new one will not be able to join.
     *
     * @param maxPlayersPerTeam The maximal number of players per team. {@code 0} to remove the limit (default value).
     */
    public static void setMaxPlayersPerTeam(int maxPlayersPerTeam)
    {
        get().maxPlayersPerTeam = maxPlayersPerTeam;
    }

    private void updateTeamsOptions()
    {
        teams.forEach(ZTeam::updateTeamOptions);
    }

    private void updateDefaultBanners()
    {
        teams.forEach(ZTeam::updateDefaultBanner);
    }



    /* *** Utilities *** */


    /**
     * Fires an event.
     *
     * @param event The event.
     */
    static void fireEvent(Event event)
    {
        Bukkit.getPluginManager().callEvent(event);
    }
}
