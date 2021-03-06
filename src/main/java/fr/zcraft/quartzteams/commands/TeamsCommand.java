/*
 * Copyright or © or Copr. Amaury Carrade (2014 - 2016)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package fr.zcraft.quartzteams.commands;

import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.commands.Commands;
import fr.zcraft.quartzlib.components.gui.Gui;
import fr.zcraft.quartzlib.components.gui.GuiUtils;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.core.QuartzLib;
import fr.zcraft.quartzteams.QuartzTeam;
import fr.zcraft.quartzteams.QuartzTeams;
import fr.zcraft.quartzteams.QuartzTeamsPermission;
import fr.zcraft.quartzteams.colors.TeamColor;
import fr.zcraft.quartzteams.guis.TeamsSelectorGUI;
import fr.zcraft.quartzteams.texts.TextUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


/**
 * TODO for teams with spaces, add a way to execute commands with the team missing and click on a team in the chat.
 */
@CommandInfo(name = "team", usageParameters = "<add|remove|join|leave|banner|banner-reset|list|spy|reset>")
public class TeamsCommand extends Command {
    protected String[] sargs;

    @Override
    protected void run() throws CommandException {
        if (args.length == 0) {
            throwInvalidArgument(I.t("Please specify a sub-command."));
        }

        sargs = TextUtils.extractArgsWithQuotes(args, 1);

        switch (args[0].toLowerCase()) {
            case "add":
                add();
                break;

            case "remove":
                remove();
                break;

            case "join":
                join();
                break;

            case "leave":
                leave();
                break;

            case "list":
                list();
                break;

            case "gui":
                gui();
                break;

            case "banner":
                banner();
                break;

            case "bannerreset":
            case "banner-reset":
                bannerreset();
                break;

            case "reset":
                reset();
                break;

            case "spy":
                spy();
                break;

            default:
                final String cmd = "/" + commandGroup.getUsualName() + " " + getName();

                help(I.t("{bold}{0} help for {1}", QuartzLib.getPlugin().getName(), cmd));
                help(I.t("{0} add <color> [name]: registers a new team.", cmd));
                help(I.t("{0} remove <teamName>: deletes a team.", cmd));
                help(I.t("{0} join <teamName> [playerName]: joins a team.", cmd));
                help(I.t(
                        "{0} leave [playerName]: leave the current team (for the current player or the specified one).",
                        cmd));
                help(I.t("{0} list: lists all teams and their players.", cmd));
                help(I.t("{0} gui: opens a GUI to create and manage teams.", cmd));
                help(I.t(
                        "{0} banner [teamName]: from the game, updates the team's banner (either the sender's team or the specified one). The banner must be in the sender's hand.",
                        cmd));
                help(I.t(
                        "{0} banner-reset [teamName]: resets the team's banner to the default (either the sender's team or the specified one).",
                        cmd));
                help(I.t("{0} reset: deletes all teams.", cmd));
                help(I.t("{0} spy [teamName]: toggle the spy of the given team's chat.", cmd));
                help(I.t(ChatColor.ITALIC + "To specify teams names with spaces in them, puts them in \"quotes\"."));
        }
    }

    private void help(final String line) {
        info(GuiUtils.generatePrefixedFixedLengthString(ChatColor.DARK_BLUE + "" + Commands.CHAT_PREFIX + " ",
                ChatColor.GOLD + line));
    }

    protected void add() throws CommandException {
        String name = null;
        TeamColor color = null;

        if (sargs.length == 0) {
            throwInvalidArgument(
                    I.t("You must specify a color and optionally a team name (in quotes if it includes spaces)."));
        }

        if (sargs.length >= 1) {
            color = TeamColor.fromString(sargs[0]);

            if (color == null) {
                throwInvalidArgument(I.t("This color is not a valid one. Tip: use autocompletion."));
            }
        }
        if (sargs.length >= 2) {
            name = sargs[1];
        }

        if (name == null) {
            name = color.toString().toLowerCase();
        }

        // If the team name is taken, generate a new one
        if (QuartzTeams.get().isTeamRegistered(name) && !QuartzTeams.settings().teamsOptionsAllowDuplicatedNames()) {
            final Random rand = new Random();
            final String originalName = name.trim() + " ";
            do {
                name = originalName + rand.nextInt(1000);
            } while (QuartzTeams.get().isTeamRegistered(name));
        }

        final QuartzTeam team = QuartzTeams.get().createTeam(name, color);

        try {
            QuartzTeams.get().registerTeam(team);
            success(I.t("{cs}Team {0}{cs} added.", team.getDisplayName()));
        }
        catch (IllegalArgumentException e) {
            error(I.t("{ce}This team already exists."));
        }
    }

    protected void remove() throws CommandException {
        final QuartzTeam team = getTeamParameter(0);

        QuartzTeams.get().unregisterTeam(team);
        success(I.t("{cs}Team {0} deleted.", team.getDisplayName()));
    }

    protected void join() throws CommandException {
        final QuartzTeam team = getTeamParameter(0);
        final OfflinePlayer player;

        if (sargs.length >= 2 && QuartzTeamsPermission.UPDATE_TEAMS_PLAYERS_LIST.grantedTo(sender)) {
            player = getOfflinePlayerParameter(1);
        } else if (QuartzTeamsPermission.JOIN_TEAM.grantedTo(playerSender())) {
            player = playerSender();
        } else {
            throwNotAuthorized();
            return;
        }

        team.addPlayer(player);
    }

    protected void leave() throws CommandException {
        final OfflinePlayer player;

        if (sargs.length >= 1 && QuartzTeamsPermission.UPDATE_TEAMS_PLAYERS_LIST.grantedTo(sender)) {
            player = getOfflinePlayerParameter(0);
        } else if (QuartzTeamsPermission.LEAVE_TEAM.grantedTo(playerSender())) {
            player = playerSender();
        } else {
            throwNotAuthorized();
            return;
        }

        QuartzTeam team = QuartzTeams.get().getTeamForPlayer(player);

        if (team != null) {
            team.removePlayer(player);
        } else {
            error(I.t("This player was not in a team."));
        }
    }

    protected void list() throws CommandException {
        if (!QuartzTeamsPermission.LIST_TEAMS.grantedTo(sender)) {
            throwNotAuthorized();
        }

        final Set<QuartzTeam> teams = QuartzTeams.get().getTeams();

        if (teams.size() == 0) {
            error(I.t("{ce}There isn't any team to show."));
        }

        for (final QuartzTeam team : teams) {
            info(I.tn("{0} ({1} player)", "{0} ({1} players)", team.size(), team.getDisplayName(), team.size()));

            for (final OfflinePlayer player : team.getPlayers()) {
                final String bullet;
                if (player.isOnline()) {
                    /// Online dot in /uh team list
                    bullet = I.t("{green} • ");
                } else {
                    /// Offline dot in /uh team list
                    bullet = I.t("{red} • ");
                }

                /// Player name after the online status dot in /uh teams list
                info(bullet + I.tc("teams_list", "{0}", player.getName()));
            }
        }
    }

    protected void gui() throws CommandException {
        Gui.open(playerSender(), new TeamsSelectorGUI());
    }

    protected void banner() throws CommandException {
        final QuartzTeam team;

        if (sargs.length >= 1 && QuartzTeamsPermission.UPDATE_OTHER_TEAM_BANNER.grantedTo(playerSender())) {
            team = getTeamParameter(0);
        } else if (QuartzTeamsPermission.UPDATE_TEAM_BANNER.grantedTo(playerSender())) {
            team = QuartzTeams.get().getTeamForPlayer(playerSender());
        } else {
            throwNotAuthorized();
            return;
        }

        if (team == null) {
            error(I.t("{ce}Either this team does not exists, or you are not in a team."));
        } else if (!playerSender().getItemOnCursor().getType().name().endsWith("_BANNER")) {
            error(I.t("{ce}You must run this command with a banner in your main hand."));
        } else {
            team.setBanner(playerSender().getItemOnCursor());
            success(I.t("{cs}The banner of the team {0}{cs} was successfully updated.", team.getDisplayName()));
        }
    }

    protected void bannerreset() throws CommandException {
        final QuartzTeam team;

        if (sargs.length >= 1 && QuartzTeamsPermission.UPDATE_OTHER_TEAM_BANNER.grantedTo(playerSender())) {
            team = getTeamParameter(0);
        } else if (QuartzTeamsPermission.UPDATE_TEAM_BANNER.grantedTo(playerSender())) {
            team = QuartzTeams.get().getTeamForPlayer(playerSender());
        } else {
            throwNotAuthorized();
            return;
        }

        if (team == null) {
            error(I.t("{ce}Either this team does not exists, or you are not in a team."));
        } else {
            team.setBanner((ItemStack) null);
            success(I.t("{cs}The banner of the team {0}{cs} was successfully updated.", team.getDisplayName()));
        }
    }

    protected void reset() throws CommandException {
        if (!QuartzTeamsPermission.RESET_TEAMS.grantedTo(sender)) {
            throwNotAuthorized();
        }

        QuartzTeams.get().unregisterAll();
        success(I.t("{cs}All teams where removed."));
    }

    protected void spy() throws CommandException {
        final Player target;

        if (sargs.length == 0) {
            if (!QuartzTeamsPermission.SPY_ALL_TEAMS.grantedTo(playerSender())) {
                throwNotAuthorized();
            }

            target = playerSender();
        } else {
            if (!QuartzTeamsPermission.MAKE_ANOTHER_SPY_ALL_TEAMS.grantedTo(sender)) {
                throwNotAuthorized();
            }

            target = getPlayerParameter(0);
        }

        final String message;

        if (QuartzTeams.chatManager().isGlobalSpy(target.getUniqueId())) {
            QuartzTeams.chatManager().removeGlobalSpy(target.getUniqueId());
            message = I.t("{cs}Spy mode {darkred}disabled{cs} for {0}.", target.getDisplayName() + ChatColor.GREEN);
        } else {
            QuartzTeams.chatManager().addGlobalSpy(target.getUniqueId());
            message = I.t("{cs}Spy mode {darkgreen}enabled{cs} for {0}.", target.getDisplayName() + ChatColor.GREEN);
        }

        target.sendMessage(message);

        if (!sender.equals(target)) {
            success(message);
        }
    }

    /**
     * Returns the team from the arg or displays an error and exits.
     *
     * @param argIndex The argument index where the team should be.
     * @return The team.
     * @throws CommandException if arguments are invalid.
     */
    private QuartzTeam getTeamParameter(final int argIndex) throws CommandException {
        if (sargs.length <= argIndex) {
            throwInvalidArgument(
                    I.t("You must specify a team name (in quotes if there are spaces in the name) as the #{0} argument.",
                            argIndex + 1));
        }

        final QuartzTeam team = QuartzTeams.get().getTeamByName(sargs[argIndex].trim());

        if (team == null) {
            throwInvalidArgument(I.t("{ce}This team does not exists."));
        }

        return team;
    }

    /**
     * Returns an OfflinePlayer object at the given argument, or displays an error and exits.
     *
     * @param argIndex The argument index where the player should be.
     * @return The player.
     * @throws CommandException if arguments are invalid.
     */
    private OfflinePlayer getOfflinePlayerParameter(int argIndex) throws CommandException {
        if (sargs.length <= argIndex) {
            throwInvalidArgument(I.t("You must specify a player name as the #{0} argument.", argIndex + 1));
        }

        final OfflinePlayer player = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(p -> p.getName().equalsIgnoreCase(sargs[argIndex]))
                .findFirst().orElse(null);

        if (player == null) {
            throwInvalidArgument(I.t("The player {0} cannot be found."));
        }

        return player;
    }



    /* *** Autocompletion *** */


    @Override
    public List<String> complete() {
        sargs = TextUtils.extractArgsWithQuotes(args, 1);

        switch (args[0].toLowerCase()) {
            case "add":
                if (!QuartzTeamsPermission.CREATE_TEAM.grantedTo(sender)) {
                    return null;
                }
                if (sargs.length == 1) {
                    return getMatchingSubset(sargs[0], "aqua", "black", "blue", "darkaqua",
                            "darkblue", "darkgray", "darkgreen", "darkpurple", "darkred",
                            "gold", "gray", "green", "lightpurple", "red", "white", "yellow", "?");
                } else {
                    return null;
                }

            case "remove":
                if (!QuartzTeamsPermission.DELETE_TEAM.grantedTo(sender)) {
                    return null;
                }
                if (sargs.length == 1) {
                    return getMatchingTeams(sargs[0]);
                } else {
                    return null;
                }

            case "join":
                if (!QuartzTeamsPermission.JOIN_TEAM.grantedTo(sender)) {
                    return null;
                }
                if (sargs.length == 1 && QuartzTeamsPermission.UPDATE_TEAMS_PLAYERS_LIST.grantedTo(sender)) {
                    return getMatchingTeams(sargs[0]);
                } else {
                    return null;
                }

            case "leave":
                if (sargs.length == 1 && QuartzTeamsPermission.UPDATE_TEAMS_PLAYERS_LIST.grantedTo(sender)) {
                    return getMatchingOfflinePlayerNames(sargs[0]);
                } else {
                    return null;
                }

            case "list":
                return null;

            case "gui":
                return null;

            case "banner":
            case "bannerreset":
            case "banner-reset":
                if (sargs.length == 1 && QuartzTeamsPermission.UPDATE_OTHER_TEAM_BANNER.grantedTo(sender)) {
                    return getMatchingTeams(sargs[0]);
                } else {
                    return null;
                }

            case "reset":
                return null;

            case "spy":
                if (sargs.length == 1 && QuartzTeamsPermission.MAKE_ANOTHER_SPY_ALL_TEAMS.grantedTo(sender)) {
                    return getMatchingPlayerNames(sargs[0]);
                } else {
                    return null;
                }

            default:
                final List<String> allowedCommands = new ArrayList<>();

                if (QuartzTeamsPermission.CREATE_TEAM.grantedTo(sender)) {
                    allowedCommands.add("add");
                }
                if (QuartzTeamsPermission.DELETE_TEAM.grantedTo(sender)) {
                    allowedCommands.add("remove");
                }
                if (QuartzTeamsPermission.JOIN_TEAM.grantedTo(sender)) {
                    allowedCommands.add("join");
                }
                if (QuartzTeamsPermission.LEAVE_TEAM.grantedTo(sender)) {
                    allowedCommands.add("leave");
                }
                if (QuartzTeamsPermission.LIST_TEAMS.grantedTo(sender)) {
                    allowedCommands.add("list");
                }

                allowedCommands.add("gui");

                if (QuartzTeamsPermission.UPDATE_TEAM_BANNER.grantedTo(sender) ||
                        QuartzTeamsPermission.UPDATE_OTHER_TEAM_BANNER.grantedTo(sender)) {
                    allowedCommands.add("banner");
                    allowedCommands.add("banner-reset");
                }

                if (QuartzTeamsPermission.RESET_TEAMS.grantedTo(sender)) {
                    allowedCommands.add("reset");
                }
                if (QuartzTeamsPermission.SPY_TEAM_CHAT.grantedTo(sender)) {
                    allowedCommands.add("spy");
                }


                return getMatchingSubset(allowedCommands, args[0]);
        }
    }

    private List<String> getMatchingTeams(final String prefix) {
        // Avoids leaks through auto-completion.
        if (!QuartzTeamsPermission.LIST_TEAMS.grantedTo(sender)) {
            return null;
        }

        final String lowerPrefix = prefix.toLowerCase();
        final List<String> suggestions = new ArrayList<>();

        for (final Object t : QuartzTeams.get().getTeams()) {
            final QuartzTeam team = (QuartzTeam) t;
            final String name = team.getName().trim();

            if (!name.contains(" ") && name.toLowerCase().startsWith(lowerPrefix)) {
                suggestions.add(name);
            }
        }

        return suggestions;
    }

    private List<String> getMatchingOfflinePlayerNames(final String prefix) {
        final String lowerPrefix = prefix.toLowerCase();

        return Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(name -> name.toLowerCase().startsWith(lowerPrefix))
                .collect(Collectors.toList());
    }
}
