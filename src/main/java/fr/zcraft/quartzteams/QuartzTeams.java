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

package fr.zcraft.quartzteams;

import fr.zcraft.quartzlib.components.gui.Gui;
import fr.zcraft.quartzlib.core.QuartzComponent;
import fr.zcraft.quartzlib.core.QuartzLib;
import fr.zcraft.quartzteams.colors.TeamColor;
import fr.zcraft.quartzteams.events.TeamChangedEvent;
import fr.zcraft.quartzteams.events.TeamRegisteredEvent;
import fr.zcraft.quartzteams.events.TeamUnregisteredEvent;
import fr.zcraft.quartzteams.guis.TeamsSelectorGUI;
import fr.zcraft.quartzteams.guis.builder.TeamBuilderBaseGUI;
import fr.zcraft.quartzteams.guis.editor.TeamActionGUI;
import fr.zcraft.quartzteams.guis.editor.TeamEditMembersGUI;
import fr.zcraft.quartzteams.permissions.OpBasedPermissionsChecker;
import fr.zcraft.quartzteams.permissions.PermissionsChecker;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


/**
 * This component must be registered inside your project to use teams.
 * <p>
 * {@code T} is your own {@link QuartzTeam} subclass.
 */
public class QuartzTeams extends QuartzComponent implements Listener {
    private static QuartzTeams instance;

    private final QuartzTeamsSettings settings = new QuartzTeamsSettings();
    private final Set<QuartzTeam> teams = new HashSet<>();
    private PermissionsChecker permissionsChecker = new OpBasedPermissionsChecker();
    private QuartzTeamsChatManager chatManager;

    /**
     * @return This component's instance.
     */
    public static QuartzTeams get() {
        return instance;
    }

    /**
     * @return The zTeams settings.
     */
    public static QuartzTeamsSettings settings() {
        return get().settings;
    }

    /**
     * @return The zTeams chat manager (to handle private teams chats).
     */
    public static QuartzTeamsChatManager chatManager() {
        return get().chatManager;
    }

    /**
     * Sets the permissions checker used to grant access to commands or GUIs actions to players.
     * <p>
     * If not set, a default {@linkplain OpBasedPermissionsChecker op-based permissions checker} is used,
     * granting permissions to operators or to any player for non-administrative actions.
     *
     * @param permissionsChecker The permissions checker to use.
     */
    public static void setPermissionsChecker(PermissionsChecker permissionsChecker) {
        get().permissionsChecker = permissionsChecker;
    }



    /* *** Teams getters *** */

    /**
     * Fires an event.
     *
     * @param event The event.
     */
    static void fireEvent(final Event event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    protected void onEnable() {
        instance = this;

        chatManager = QuartzLib.loadComponent(QuartzTeamsChatManager.class);
    }

    /**
     * @return All the registered teams.
     */
    public Set<QuartzTeam> getTeams() {
        return Collections.unmodifiableSet(teams);
    }

    /**
     * @return All the registered teams, as an array.
     */
    public QuartzTeam[] getTeamsArray() {
        return teams.toArray(new QuartzTeam[0]);
    }

    /**
     * Finds a team by name (ignoring case and extra spaces around).
     *
     * @param name The team name.
     * @return The {@link QuartzTeam}, or {@code null} if not found.
     */
    public QuartzTeam getTeamByName(final String name) {
        return teams.stream().filter(team -> team.getName().trim().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * @return The amount of registered teams.
     */
    public int countTeams() {
        return teams.size();
    }

    /**
     * Finds the team of the given player, if it exists.
     *
     * @param player The player.
     * @return The team, or {@code null} if this player is not in a team.
     */
    public QuartzTeam getTeamForPlayer(final OfflinePlayer player) {
        return getTeamForPlayer(player.getUniqueId());
    }

    /**
     * Finds the team of the given player, if it exists.
     *
     * @param playerID The player's UUID.
     * @return The team, or {@code null} if this player is not in a team.
     */
    public QuartzTeam getTeamForPlayer(final UUID playerID) {
        return teams.stream().filter(team -> team.containsPlayer(playerID)).findFirst().orElse(null);
    }

    /**
     * Removes a player form its team, if he had any.
     *
     * @param player The player.
     * @return {@code true} if the player was actually removed from a team.
     */
    public boolean removePlayerFromTeam(final OfflinePlayer player) {
        return removePlayerFromTeam(player, false);
    }

    /**
     * Removes a player form its team, if he had any.
     *
     * @param playerID The player's UUID.
     * @return {@code true} if the player was actually removed from a team.
     */
    public boolean removePlayerFromTeam(final UUID playerID) {
        return removePlayerFromTeam(playerID, false);
    }



    /* *** Teams registration *** */

    /**
     * Removes a player form its team, if he had any.
     *
     * @param player      The player.
     * @param becauseJoin {@code true} if the player is removed to go into another team.
     * @return {@code true} if the player was actually removed from a team.
     */
    boolean removePlayerFromTeam(final OfflinePlayer player, boolean becauseJoin) {
        return removePlayerFromTeam(player.getUniqueId(), becauseJoin);
    }

    /**
     * Removes a player form its team, if he had any.
     *
     * @param playerID    The player's UUID.
     * @param becauseJoin {@code true} if the player is removed to go into another team.
     * @return {@code true} if the player was actually removed from a team.
     */
    boolean removePlayerFromTeam(final UUID playerID, boolean becauseJoin) {
        final QuartzTeam team = getTeamForPlayer(playerID);

        if (team != null) {
            team.removePlayer(playerID, becauseJoin);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Registers a new team.
     *
     * @param team The team.
     * @throws IllegalArgumentException if duplicated teams are disabled and there is another team with the same name.
     */
    public void registerTeam(final QuartzTeam team) throws IllegalArgumentException {
        if (!settings.teamsOptionsAllowDuplicatedNames() && isTeamRegistered(team.getName())) {
            throw new IllegalArgumentException(
                    "There is already a team registered with this name, and it's disallowed by the ZTeams configuration.");
        }

        teams.add(team);
        fireEvent(new TeamRegisteredEvent(team));
    }

    /**
     * Unregisters a team.
     *
     * @param team The team.
     * @return {@code true} if the team was actually unregistered.
     */
    public boolean unregisterTeam(final QuartzTeam team) {
        final boolean actuallyUnregistered = teams.remove(team);
        fireEvent(new TeamUnregisteredEvent(team));

        return actuallyUnregistered;
    }

    /**
     * Unregisters all the teams.
     */
    public void unregisterAll() {
        new HashSet<>(teams).forEach(QuartzTeam::deleteTeam);
    }



    /* *** QuartzTeams internal management methods *** */

    /**
     * Checks if the given team is registered.
     *
     * @param team The team.
     * @return {@code true} if registered.
     */
    public boolean isTeamRegistered(final QuartzTeam team) {
        return teams.contains(team);
    }

    /**
     * Checks if there is a team registered with this name (case-insensitive).
     *
     * @param teamName The team's name.
     * @return {@code true} if registered.
     */
    public boolean isTeamRegistered(final String teamName) {
        return teams.stream().anyMatch(team -> team.getName().trim().equalsIgnoreCase(teamName.trim()));
    }



    /* *** Events *** */

    void colorizePlayer(final OfflinePlayer offlinePlayer) {
        if (!settings.teamsOptionsColorizeChat()) {
            return;
        }

        if (!offlinePlayer.isOnline()) {
            return;
        }

        final Player player = (Player) offlinePlayer;
        final QuartzTeam team = getTeamForPlayer(player);

        if (team == null) {
            player.setDisplayName(player.getName());
        } else {
            player.setDisplayName(team.getColorOrWhite().toChatColor() + player.getName() + ChatColor.RESET);
        }
    }



    /* *** ZTeams permissions methods *** */

    /**
     * Generates a color from the given color.
     * <p>
     * If the color is neither {@link TeamColor#RANDOM} nor {@code null}, returns the given color.<br />
     * Else, generates a random unused (if possible) color.
     *
     * @param color The color to be generated.
     * @return A non-random color.
     */
    TeamColor generateColor(final TeamColor color) {
        if (color != null && color != TeamColor.RANDOM) {
            return color;
        }

        // A list of the currently used colors.
        final Set<TeamColor> availableColors = new HashSet<>(Arrays.asList(TeamColor.values()));
        availableColors.remove(TeamColor.RANDOM);

        teams.stream().map(QuartzTeam::getColorOrWhite).forEach(availableColors::remove);

        if (availableColors.size() != 0) {
            return (TeamColor) availableColors.toArray()[(new Random()).nextInt(availableColors.size())];
        } else {
            // length - 1 so the RANDOM option is never selected.
            return TeamColor.values()[(new Random()).nextInt(TeamColor.values().length - 1)];
        }
    }

    @EventHandler
    public void onTeamChanged(final TeamChangedEvent ev) {
        updateGUIs();
    }



    /* *** ZTeams creation methods *** */

    /**
     * @return The internal permissions checker.
     */
    public PermissionsChecker permissionsChecker() {
        return permissionsChecker;
    }


    /* *** Utilities *** */

    /**
     * Creates a team with name, color and member(s), registers and returns it.
     *
     * @param name    The team's name.
     * @param color   The team's color.
     * @param players Initial team members.
     * @return The (already registered) team.
     */
    public QuartzTeam createTeam(final String name, final TeamColor color, final OfflinePlayer... players) {
        final QuartzTeam team = new QuartzTeam(name, color);

        for (OfflinePlayer player : players) {
            team.addPlayer(player);
        }

        registerTeam(team);

        return team;
    }

    void updateTeamsOptions() {
        teams.forEach(QuartzTeam::updateTeamOptions);
    }

    void updateDefaultBanners() {
        teams.forEach(QuartzTeam::updateDefaultBanner);
    }

    public void updateGUIs() {
        Gui.update(TeamsSelectorGUI.class);
        Gui.update(TeamBuilderBaseGUI.class);
        Gui.update(TeamActionGUI.class);
        Gui.update(TeamEditMembersGUI.class);
    }
}
