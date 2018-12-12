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
package fr.zcraft.zteams.guis.builder;

import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.gui.GuiAction;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zteams.ZTeam;
import fr.zcraft.zteams.ZTeams;
import fr.zcraft.zteams.colors.TeamColor;
import fr.zcraft.zteams.guis.TeamsSelectorGUI;
import fr.zcraft.zteams.guis.utils.OfflinePlayersComparator;
import fr.zcraft.zteams.texts.TextUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;


public class TeamBuilderStepPlayersGUI extends TeamBuilderBaseGUI
{
    private final TeamColor color;
    private final String name;

    private final Set<UUID> teamMembers = new HashSet<>();


    public TeamBuilderStepPlayersGUI(final TeamColor color, final String name)
    {
        this.color = color;
        this.name = name;
    }

    @Override
    protected void onUpdate()
    {
        /// The title of the members selector GUI, in the create team GUIs
        setTitle(I.t("New team » {black}Members"));
        setSize(6 * 9);

        generateBreadcrumbs(BuildingStep.PLAYERS);


        /* *** Players *** */

        final Set<OfflinePlayer> players = new TreeSet<>(new OfflinePlayersComparator());
        Collections.addAll(players, Bukkit.getOfflinePlayers());

        int slot = 9;
        for (final OfflinePlayer player : players)
        {
            action(player.getUniqueId().toString(), slot, generatePlayerButton(player));

            if (slot < 44) slot++;
            else break;
        }


        /* *** Done button *** */

        final ItemStackBuilder doneButton = new ItemStackBuilder(Material.EMERALD)
                /// The title of the final « create the team » button of the create team GUIs
                .title(I.t("{green}Create the team"))
                .lore(" ")

                /// The summary title in the final « create the team » button of the create team GUIs
                .lore(I.t("{blue}{bold}Summary"))

                /// The team name in the final « create the team » button of the create team GUIs
                .longLore(I.t("{gray}Team name: {white}{0}", getName()))

                /// The team color in the final « create the team » button of the create team GUIs
                .longLore(I.t("{gray}Color: {0}", getColor() == TeamColor.RANDOM ? ChatColor.MAGIC + "Random" : getColor().toChatColor() + TextUtils.friendlyEnumName(getColor())))

                /// The team members count in the final « create the team » button of the create team GUIs
                .longLore(I.t("{gray}Members: {white}{0}", teamMembers.size()))

                .lore(" ");

        for (UUID teamMember : teamMembers)
        {
            OfflinePlayer player = Bukkit.getOfflinePlayer(teamMember);
            /// A member bullet in the final « create the team » button of the create team GUIs
            doneButton.lore(I.t("{darkgray}- {white}{0}", player != null ? player.getName() : teamMember));
        }

        action("done", getSize() - 5, doneButton);
    }

    private ItemStack generatePlayerButton(OfflinePlayer player)
    {
        final ItemStack button = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        final SkullMeta meta = (SkullMeta) button.getItemMeta();

        final String displayName = player instanceof Player ? ((Player) player).getDisplayName() : player.getName();
        final ZTeam team = ZTeams.get().getTeamForPlayer(player);

        meta.setOwner(player.getName());
        /// The title of a button to select a player (a skull button). {0} = player's display name.
        meta.setDisplayName(I.t("{reset}{0}", displayName));
        meta.setLore(Arrays.asList(
                player.isOnline() ? I.t("{gray}Online") : I.t("{gray}Offline"),
                team != null ? I.t("{gray}Current team: {0}", team.getDisplayName()) : I.t("{gray}Current team: none"),
                "",
                teamMembers.contains(player.getUniqueId()) ? I.t("{lightpurple}Selected!") : I.t("{darkgray}» {white}Click {gray}to add to the team")
        ));

        button.setItemMeta(meta);
        return button;
    }


    @Override
    protected void unknown_action(String name, int slot, ItemStack item)
    {
        UUID playerUUID;
        try { playerUUID = UUID.fromString(name); } catch(IllegalArgumentException e) { return; }

        if (teamMembers.contains(playerUUID))
            teamMembers.remove(playerUUID);
        else
            teamMembers.add(playerUUID);

        update();
    }

    @GuiAction ("done")
    protected void done()
    {
        try
        {
            final ZTeam team = ZTeams.get().createTeam(getName(), getColor());
            teamMembers.stream().map(Bukkit::getOfflinePlayer).filter(Objects::nonNull).forEach(team::addPlayer);

            getPlayer().sendMessage(I.t("{cs}Team created."));
        }
        catch (IllegalArgumentException e)
        {
            getPlayer().sendMessage(I.t("{ce}This team already exists."));
        }

        Gui.open(getPlayer(), new TeamsSelectorGUI());
    }


    @Override
    protected TeamColor getColor() { return color; }

    @Override
    protected String getName() { return name; }
}
