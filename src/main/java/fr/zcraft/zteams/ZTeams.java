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

import fr.zcraft.zlib.core.ZLibComponent;
import fr.zcraft.zteams.colors.TeamColor;
import fr.zcraft.zteams.creator.DefaultZTeamCreator;
import fr.zcraft.zteams.creator.ZTeamCreator;
import fr.zcraft.zteams.events.TeamChangedEvent;
import fr.zcraft.zteams.events.TeamRegisteredEvent;
import fr.zcraft.zteams.events.TeamUnregisteredEvent;
import fr.zcraft.zteams.permissions.OpBasedPermissionsChecker;
import fr.zcraft.zteams.permissions.PermissionsChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.Collections;
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

    private final ZTeamsSettings settings = new ZTeamsSettings();

    private PermissionsChecker permissionsChecker = new OpBasedPermissionsChecker();
    private ZTeamCreator teamsCreator = new DefaultZTeamCreator();

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

    /**
     * @return The zTeams settings.
     */
    public static ZTeamsSettings settings()
    {
        return get().settings;
    }



    /* *** Teams getters *** */

    /**
     * @return All the registered teams.
     */
    public Set<T> getTeams()
    {
        return Collections.unmodifiableSet(teams);
    }

    /**
     * @return All the registered teams, as an array.
     */
    public T[] getTeamsArray()
    {
        return (T[]) teams.toArray();
    }

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
     * @return The amount of registered teams.
     */
    public int countTeams()
    {
        return teams.size();
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
        final T team = getTeamForPlayer(playerID);

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
     * @throws IllegalArgumentException if duplicated teams are disabled and there is another team with the same name.
     */
    public void registerTeam(T team) throws IllegalArgumentException
    {
        if (!settings.teamsOptionsAllowDuplicatedNames() && isTeamRegistered(team.getName()))
            throw new IllegalArgumentException("There is already a team registered with this name, and it's disallowed by the ZTeams configuration.");

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

    /**
     * Checks if the given team is registered.
     *
     * @param team The team.
     * @return {@code true} if registered.
     */
    public boolean isTeamRegistered(T team)
    {
        return teams.contains(team);
    }

    /**
     * Checks if there is a team registered with this name (case-insensitive).
     *
     * @param teamName The team's name.
     * @return {@code true} if registered.
     */
    public boolean isTeamRegistered(String teamName)
    {
        return teams.stream().anyMatch(team -> team.getName().trim().equalsIgnoreCase(teamName.trim()));
    }



    /* *** ZTeams internal management methods *** */


    void colorizePlayer(final OfflinePlayer offlinePlayer)
    {
        if (!settings.teamsOptionsColorizeChat())
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
        updateGUIs();
    }



    /* *** ZTeams permissions methods *** */


    /**
     * @return The internal permissions checker.
     */
    public PermissionsChecker permissionsChecker()
    {
        return permissionsChecker;
    }

    /**
     * Sets the permissions checker used to grant access to commands or GUIs actions to players.
     *
     * If not set, a default {@linkplain OpBasedPermissionsChecker op-based permissions checker} is used,
     * granting permissions to operators or to any player for non-administrative actions.
     *
     * @param permissionsChecker The permissions checker to use.
     */
    public static void setPermissionsChecker(PermissionsChecker permissionsChecker)
    {
        get().permissionsChecker = permissionsChecker;
    }



    /* *** ZTeams creation methods *** */


    /**
     * @return The internal teams creator.
     */
    public ZTeamCreator<T> teamsCreator()
    {
        return teamsCreator;
    }

    /**
     * Sets the teams creator used by the provided GUIs and commands to create teams.
     *
     * Override this if you want to provide your own subclassed team class with your custom
     * attributes.
     *
     * @param teamsCreator The teams creator to use.
     */
    public static void setTeamsCreator(ZTeamCreator<? extends ZTeam> teamsCreator)
    {
        get().teamsCreator = teamsCreator;
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


    void updateTeamsOptions()
    {
        teams.forEach(ZTeam::updateTeamOptions);
    }

    void updateDefaultBanners()
    {
        teams.forEach(ZTeam::updateDefaultBanner);
    }

    public void updateGUIs()
    {
        // TODO
    }
}
