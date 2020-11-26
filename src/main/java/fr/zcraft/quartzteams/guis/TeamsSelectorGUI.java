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

package fr.zcraft.quartzteams.guis;

import fr.zcraft.quartzlib.components.gui.ExplorerGui;
import fr.zcraft.quartzlib.components.gui.Gui;
import fr.zcraft.quartzlib.components.gui.GuiAction;
import fr.zcraft.quartzlib.components.gui.GuiUtils;
import fr.zcraft.quartzlib.components.gui.PromptGui;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.tools.items.ItemStackBuilder;
import fr.zcraft.quartzlib.tools.items.ItemUtils;
import fr.zcraft.quartzteams.QuartzTeam;
import fr.zcraft.quartzteams.QuartzTeams;
import fr.zcraft.quartzteams.QuartzTeamsPermission;
import fr.zcraft.quartzteams.guis.builder.TeamBuilderStepColorGUI;
import fr.zcraft.quartzteams.guis.editor.TeamEditGUI;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public class TeamsSelectorGUI extends ExplorerGui<QuartzTeam> {
    @Override
    protected void onUpdate() {
        if (QuartzTeamsPermission.LIST_TEAMS.grantedTo(getPlayer())) {
            /// The title of the teams selector GUI. {0} = teams count.
            setTitle(I.t("{black}Select a team {reset}({0})", QuartzTeams.get().countTeams()));
            setData(QuartzTeams.get().getTeamsArray());
        } else {
            setTitle(I.t("{darkred}Unauthorized to list teams"));
        }

        setMode(Mode.READONLY);
        setKeepHorizontalScrollingSpace(true);

        if (QuartzTeamsPermission.UPDATE_TEAM_NAME.grantedTo(getPlayer())) {
            int renameSlot = QuartzTeamsPermission.CREATE_TEAM.grantedTo(getPlayer()) ? getSize() - 6 : getSize() - 5;

            action("rename", renameSlot, GuiUtils.makeItem(
                    /// The title of a button to rename our team, in the selector GUI.
                    Material.WRITABLE_BOOK, I.t("{white}Rename your team"),
                    /// Warning displayed in the "Rename your team" button, if the player is not in a team
                    QuartzTeams.get().getTeamForPlayer(getPlayer()) == null ?
                            GuiUtils.generateLore(I.t("{gray}You have to be in a team")) : null
            ));
        }

        if (QuartzTeamsPermission.CREATE_TEAM.grantedTo(getPlayer())) {
            int newTeamSlot =
                    QuartzTeamsPermission.UPDATE_TEAM_NAME.grantedTo(getPlayer()) ? getSize() - 4 : getSize() - 5;

            /// The title of a button to create a new team, in the selector GUI.
            action("new", newTeamSlot, GuiUtils.makeItem(Material.EMERALD, I.t("{white}New team")));
        }

        if (!QuartzTeamsPermission.LIST_TEAMS.grantedTo(getPlayer()) && hasManagementPermission()) {
            final QuartzTeam team = QuartzTeams.get().getTeamForPlayer(getPlayer());
            if (team != null) {
                // Yes, that's the same slot as the rename button. As we can rename from
                // the details GUI, the GUI is cleaner this way.
                int ownTeamSlot =
                        QuartzTeamsPermission.CREATE_TEAM.grantedTo(getPlayer()) ? getSize() - 6 : getSize() - 5;
                action("own_team", ownTeamSlot, getViewItem(team));
            }
        }
    }

    @Override
    protected ItemStack getViewItem(QuartzTeam team) {
        final boolean isPlayerInTeam = team.getPlayersUUID().contains(getPlayer().getUniqueId());


        // Lore
        final List<String> lore = new ArrayList<>();

        lore.add("");

        if (team.size() != 0) {
            /// The "Players" title in the selector GUI, on a team's tooltip
            lore.add(I.t("{blue}Players"));

            /// An item of the players list in the selector GUI, on a team's tooltip
            team.getPlayers().stream().map(player -> I.t("{darkgray}- {white}{0}", player.getName()))
                    .forEach(lore::add);

            lore.add("");
        }

        if (QuartzTeamsPermission.JOIN_TEAM.grantedTo(getPlayer()) && !isPlayerInTeam) {
            if (!team.isFull()) {
                lore.add(I.t("{darkgray}» {white}Click {gray}to join this team"));
            } else {
                lore.add(I.t("{darkgray}» {red}This team is full"));
            }
        } else if (QuartzTeamsPermission.LEAVE_TEAM.grantedTo(getPlayer()) && isPlayerInTeam) {
            lore.add(I.t("{darkgray}» {white}Click {gray}to leave this team"));
        }

        if (hasManagementPermission()) {
            lore.add(I.t("{darkgray}» {white}Right-click {gray}to manage this team"));
        }

        // Title
        final String title = QuartzTeams.settings().maxPlayersPerTeam() != 0
                /// Title of the team item in the teams selector GUI (with max). {0}: team display name. {1}: players count. {2}: max count.
                ? I.t("{white}Team {0} {gray}({1}/{2})", team.getDisplayName(), team.size(),
                QuartzTeams.settings().maxPlayersPerTeam())
                /// Title of the team item in the teams selector GUI (without max) {0}: team display name. {1}: players count.
                : I.tn("{white}Team {0} {gray}({1} player)", "{white}Team {0} {gray}({1} players)", team.size(),
                team.getDisplayName(), team.size());

        return new ItemStackBuilder(
                ItemUtils.colorize(QuartzTeams.settings().teamsGUIItemType(), team.getColorOrWhite().toChatColor()))
                .title(title)
                .lore(lore)
                .glow(QuartzTeams.settings().teamsGUIGlowOnCurrentTeam() && isPlayerInTeam)
                .hideAllAttributes()
                .item();
    }

    @Override
    protected ItemStack getEmptyViewItem() {
        if (QuartzTeamsPermission.LIST_TEAMS.grantedTo(getPlayer())) {
            return new ItemStackBuilder(Material.BARRIER)
                    .title(I.t("{red}No team created"))
                    .longLore(QuartzTeamsPermission.CREATE_TEAM.grantedTo(getPlayer())
                            /// Subtitle of the item displayed in the teams selector GUI if there isn't anything to display.
                            ? I.t("{gray}Click the emerald button below to create one.")
                            /// Subtitle of the item displayed in the teams selector GUI if there isn't anything to display and the player cannot create a team.
                            : I.t("{gray}Wait for an administrator to create one."))
                    .hideAllAttributes()
                    .item();
        } else {
            return new ItemStackBuilder(Material.BARRIER)
                    .title(I.t("{red}You are not allowed to list the teams."))
                    .longLore(QuartzTeams.get().getTeamForPlayer(getPlayer()) != null && hasManagementPermission()
                            /// Subtitle of the item displayed in the teams selector GUI if teams are not listable by the player, but it is into a team.
                            ? I.t("{gray}You can still click the item below to open your team's settings.")
                            /// Subtitle of the item displayed in the teams selector GUI if teams are not listable by the player, and it is not into a team.
                            : I.t("{gray}Sorry."))
                    .hideAllAttributes()
                    .item();
        }
    }

    @Override
    protected ItemStack getPickedUpItem(QuartzTeam team) {
        final boolean playerInTeam = team.getPlayersUUID().contains(getPlayer().getUniqueId());

        if (QuartzTeamsPermission.JOIN_TEAM.grantedTo(getPlayer()) && !playerInTeam) {
            try {
                team.addPlayer(getPlayer());
            }
            catch (RuntimeException ignored) {
            } // team full, does nothing
        } else if (QuartzTeamsPermission.LEAVE_TEAM.grantedTo(getPlayer()) && playerInTeam) {
            team.removePlayer(getPlayer());
        }

        update();
        return null;
    }

    @Override
    protected void onRightClick(QuartzTeam team) {
        if (hasManagementPermission()) {
            Gui.open(getPlayer(), new TeamEditGUI(team), this);
        } else {
            getPickedUpItem(team);
        }
    }

    @GuiAction("rename")
    public void rename() {
        final QuartzTeam team = QuartzTeams.get().getTeamForPlayer(getPlayer());
        if (team == null) {
            return;
        }

        Gui.open(getPlayer(), new PromptGui(team::setName, team.getName()), this);
    }

    @GuiAction("new")
    public void newTeam() {
        Gui.open(getPlayer(), new TeamBuilderStepColorGUI());
    }

    @GuiAction("own_team")
    public void ownTeam() {
        final QuartzTeam team = QuartzTeams.get().getTeamForPlayer(getPlayer());
        if (team == null) {
            return;
        }

        Gui.open(getPlayer(), new TeamEditGUI(team), this);
    }

    private boolean hasManagementPermission() {
        return QuartzTeamsPermission.UPDATE_TEAM_NAME.grantedTo(getPlayer())
                || QuartzTeamsPermission.UPDATE_TEAM_COLOR.grantedTo(getPlayer())
                || QuartzTeamsPermission.UPDATE_TEAM_BANNER.grantedTo(getPlayer());
    }
}
