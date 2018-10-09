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
package fr.zcraft.zteams.guis.editor;

import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.gui.GuiAction;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.components.gui.PromptGui;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zteams.ZTeam;
import fr.zcraft.zteams.ZTeamsPermission;
import fr.zcraft.zteams.colors.ColorsUtils;
import fr.zcraft.zteams.texts.TextUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;


public class TeamEditGUI extends TeamActionGUI
{
    public TeamEditGUI(ZTeam team)
    {
        super(team);
    }


    @Override
    protected void onUpdate()
    {
        /// The title of the edit team GUI. {0} = team display name.
        setTitle(I.t("Teams » {black}{0}", team.getDisplayName()));
        setSize(36);

        if (!exists())
        {
            action("", 13, getDeletedItem());
            return;
        }


        /* *** Banner *** */

        final ItemStackBuilder bannerButton = new ItemStackBuilder(team.getBanner())
                .title(team.getDisplayName())
                /// Members count in the banner description, in the team edit GUI.
                .longLore(I.tn("{white}{0} {gray}member", "{white}{0} {gray}members", team.getSize(), team.getSize()))
                .lore(" ").hideAttributes();

        if (ZTeamsPermission.UPDATE_TEAM_BANNER.grantedTo(getPlayer()))
            bannerButton.longLore(I.t("{white}Click with a banner {gray}to update this team's banner"));
        else
            bannerButton.longLore(I.t("{gray}You're not allowed to update this team's banner."));

        action("banner", 9, bannerButton);


        /* *** Color *** */

        final ItemStackBuilder colorButton = new ItemStackBuilder(Material.WOOL)
                .data(ColorsUtils.chat2Dye(team.getColorOrWhite().toChatColor()).getWoolData())
                /// Update team color button in edit GUI.
                .title(I.t("{green}Update the color"))
                .longLore(I.tc(
                    /// Current team color in edit GUI. {0} = formatted color name.
                    "current_team_color", "{gray}Current: {white}{0}",
                    team.getColorOrWhite().toChatColor() + TextUtils.friendlyEnumName(team.getColorOrWhite())
                ));

        if (!ZTeamsPermission.UPDATE_TEAM_COLOR.grantedTo(getPlayer()))
            colorButton.lore(" ").longLore(I.t("{gray}You're not allowed to update this team's color."));

        action("color", 11, colorButton);


        /* *** Name *** */

        final ItemStackBuilder nameButton = new ItemStackBuilder(Material.BOOK_AND_QUILL)
                /// Rename team button in edit GUI.
                .title(I.t("{green}Rename the team"))
                /// Current team name in edit GUI. {0} = raw team name.
                .longLore(I.tc("current_team_name", "{gray}Current: {white}{0}", team.getName()));

        if (!ZTeamsPermission.UPDATE_TEAM_NAME.grantedTo(getPlayer()))
            nameButton.lore(" ").longLore(I.t("{gray}You're not allowed to update this team's name."));

        action("name", 13, nameButton);


        /* *** Members *** */

        final ItemStackBuilder membersButton = new ItemStackBuilder(Material.SKULL_ITEM).data((short) 3);

        for (OfflinePlayer player : team.getPlayers())
        {
            if (player.isOnline())
                membersButton.lore(I.t("{green} • ") + ChatColor.RESET + player.getName());
            else
                membersButton.lore(I.t("{red} • ") + ChatColor.RESET + player.getName());
        }

        if (ZTeamsPermission.UPDATE_TEAMS_PLAYERS_LIST.grantedTo(getPlayer()))
        {
            membersButton
                /// Update team members button in edit GUI.
                .title(I.t("{green}Add or remove players"))
                .lore(" ").longLore(I.t("{white}Click {gray}to add or remove players"));
        }
        else
        {
            membersButton
                .title(I.t("{green}Players list"))
                .lore(" ").longLore(I.t("{gray}You're not allowed to add or remove players."));
        }

        action("members", 15, membersButton);


        /* *** Delete *** */

        final ItemStackBuilder deleteButton = new ItemStackBuilder(Material.BARRIER)
                /// Delete team button in edit GUI.
                .title(I.t("{red}Delete this team"));

        if (ZTeamsPermission.DELETE_TEAM.grantedTo(getPlayer()))
            deleteButton.longLore(I.t("{gray}Cannot be undone"));
        else
            deleteButton.longLore(I.t("{gray}You're not allowed to delete this team."));


        // Delete
        action("delete", 17, GuiUtils.makeItem(
                Material.BARRIER,
                /// Delete team button in edit GUI.
                I.t("{red}Delete this team"),
                /// Warning under the "delete team" button title.
                GuiUtils.generateLore(I.t("{gray}Cannot be undone"))
        ));

        // Exit
        action("exit", getSize() - 5, GuiUtils.makeItem(
                Material.EMERALD,
                /// Go back button in GUIs.
                I.t("{green}« Go back")
        ));
    }


    @GuiAction ("banner")
    protected void banner(InventoryClickEvent ev)
    {
        if (!ZTeamsPermission.UPDATE_TEAM_BANNER.grantedTo(getPlayer())) return;

        if (ev.getCursor() != null && ev.getCursor().getType() == Material.BANNER)
        {
            team.setBanner(ev.getCursor());
            update();
        }
    }

    @GuiAction ("color")
    protected void color()
    {
        if (!ZTeamsPermission.UPDATE_TEAM_COLOR.grantedTo(getPlayer())) return;
        Gui.open(getPlayer(), new TeamEditColorGUI(team), this);
    }

    @GuiAction ("name")
    protected void name()
    {
        if (!ZTeamsPermission.UPDATE_TEAM_NAME.grantedTo(getPlayer())) return;
        Gui.open(getPlayer(), new PromptGui(name -> { if (!name.trim().isEmpty()) team.setName(name); }, team.getName()), this);
    }

    @GuiAction ("members")
    protected void members()
    {
        if (!ZTeamsPermission.UPDATE_TEAMS_PLAYERS_LIST.grantedTo(getPlayer())) return;
        Gui.open(getPlayer(), new TeamEditMembersGUI(team), this);
    }

    @GuiAction ("delete")
    protected void delete()
    {
        if (!ZTeamsPermission.DELETE_TEAM.grantedTo(getPlayer())) return;
        Gui.open(getPlayer(), new TeamEditDeleteGUI(team), this);
    }

    @GuiAction ("exit")
    protected void exit()
    {
        close();
    }
}
