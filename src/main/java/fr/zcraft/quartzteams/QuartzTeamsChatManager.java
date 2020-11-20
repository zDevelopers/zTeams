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

import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.core.QuartzComponent;
import fr.zcraft.quartzlib.tools.text.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class QuartzTeamsChatManager extends QuartzComponent implements Listener
{
    private final Set<UUID> teamChatLocked = new HashSet<>();
    private final Map<UUID, QuartzTeam> otherTeamChatLocked = new HashMap<>();
    private final Set<UUID> globalSpies = new HashSet<>();

    /**
     * Sends a team-message from the given sender.
     *
     * @param sender The sender.
     * @param message The message to send.
     */
    public void sendTeamMessage(final Player sender, final String message)
    {
        sendTeamMessage(sender, message, null);
    }

    /**
     * Sends a team-message from the given sender.
     *
     * @param sender The sender.
     * @param message The message to send.
     * @param team If not null, this message will be considered as an external message from another player to this team.
     */
    public void sendTeamMessage(final Player sender, final String message, final QuartzTeam team)
    {
        // Permission check
        if (team == null && !sender.hasPermission("uh.teamchat.self"))
        {
            sender.sendMessage(I.t("{ce}You are not allowed to send a private message to your team."));
            return;
        }
        if (team != null && !sender.hasPermission("uh.teamchat.others"))
        {
            sender.sendMessage(I.t("{ce}You are not allowed to enter in the private chat of another team."));
            return;
        }

        final String rawMessage;
        final QuartzTeam recipient;

        if (team == null)
        {
            /// Format of a private team message from a team member. {0} = sender display name, {1} = message.
            rawMessage = I.t("{gold}[{0}{gold} -> his team] {reset}{1}", sender.getDisplayName(), message);
            recipient = QuartzTeams.get().getTeamForPlayer(sender);

            if (recipient == null)
            {
                /// Error message if someone try to send a team private message out of any team
                sender.sendMessage(I.t("{ce}You are not in a team!"));
                return;
            }
        }
        else
        {
            /// Format of a private team message from a non-team-member. {0} = sender display name, {1} = team display name, {2} = message.
            rawMessage = I.t("{gold}[{0}{gold} -> team {1}{gold}] {reset}{2}", sender.getDisplayName(), team.getDisplayName(), message);
            recipient = team;
        }

        sendRawTeamMessage(rawMessage, recipient);
    }

    /**
     * Sends a raw team-message from the given player.
     *
     * @param rawMessage The raw message to be sent.
     * @param team The recipient of this message.
     */
    private void sendRawTeamMessage(final String rawMessage, final QuartzTeam team)
    {
        // The message is sent to the players of the team...
        team.getOnlinePlayers().forEach(player -> MessageSender.sendChatMessage(player, rawMessage));

        // ... to the spies ...
        if (otherTeamChatLocked.containsValue(team))
        {
            // The message is only sent to the spies not in the team, to avoid double messages
            otherTeamChatLocked.keySet().stream()
                    .filter(playerId -> otherTeamChatLocked.containsKey(playerId) && otherTeamChatLocked.get(playerId).equals(team))
                    .filter(playerId -> !team.containsPlayer(playerId))
                    .forEach(playerId -> MessageSender.sendChatMessage(Bukkit.getPlayer(playerId), rawMessage));
        }

        // ... to the global spies ...
        globalSpies.stream()
                .filter(playerId -> !otherTeamChatLocked.containsKey(playerId) || !otherTeamChatLocked.get(playerId).equals(team))
                .filter(playerId -> !team.containsPlayer(playerId))
                .forEach(playerId -> MessageSender.sendChatMessage(Bukkit.getPlayer(playerId), rawMessage));

        // ... and to the console.
        if (QuartzTeams.settings().teamChatLogInConsole())
        {
            Bukkit.getConsoleSender().sendMessage(rawMessage);
        }
    }

    /**
     * Sends a global message from the given player.
     *
     * @param sender The sender of this message.
     * @param message The message to be sent.
     */
    public void sendGlobalMessage(final Player sender, final String message)
    {
        // This message will be sent synchronously.
        // The players' messages are sent asynchronously.
        // That's how we differentiates the messages sent through /g and the messages sent using
        // the normal chat.
        sender.chat(message);
    }


    /**
     * Toggles the chat between the global chat and the team chat.
     *
     * @param playerID The chat of this player will be toggled.
     * @return {@code true} if the chat is now the team chat; false else.
     */
    public boolean toggleChatForPlayer(final UUID playerID)
    {
        return toggleChatForPlayer(playerID, null);
    }

    /**
     * Toggles the chat between the global chat and the team chat.
     *
     * @param playerID The chat of this player will be toggled.
     * @param team The team to chat with. If null, the player's team will be used.
     * @return {@code true} if the chat is now the team chat; false else.
     */
    public boolean toggleChatForPlayer(final UUID playerID, final QuartzTeam team)
    {
        // If the team is not null, we will always go to the team chat
        // Else, normal toggle

        if (team != null)
        {
            // if the player was in another team chat before, we removes it.
            teamChatLocked.remove(playerID);
            otherTeamChatLocked.put(playerID, team);

            return true;
        }

        else
        {
            if (isAnyTeamChatEnabled(playerID))
            {
                teamChatLocked.remove(playerID);
                otherTeamChatLocked.remove(playerID);

                return false;
            }
            else
            {
                teamChatLocked.add(playerID);

                return true;
            }
        }
    }

    /**
     * Toggles the chat between the global chat and the team chat.
     *
     * @param player The chat of this player will be toggled.
     * @return {@code true} if the chat is now the team chat; false else.
     */
    public boolean toggleChatForPlayer(final Player player)
    {
        return toggleChatForPlayer(player.getUniqueId(), null);
    }

    /**
     * Toggles the chat between the global chat and the team chat.
     *
     * @param player The chat of this player will be toggled.
     * @param team The team to chat with. If null, the player's team will be used.
     * @return {@code true} if the chat is now the team chat; false else.
     */
    public boolean toggleChatForPlayer(final Player player, final QuartzTeam team)
    {
        return toggleChatForPlayer(player.getUniqueId(), team);
    }

    /**
     * Returns true if the team chat is enabled for the given player.
     *
     * @param playerID The player's UUID.
     * @param team If non-null, this will check if the given player is spying the current team.
     * @return {@code true} if the team chat is enabled for the given player.
     */
    public boolean isTeamChatEnabled(final UUID playerID, final QuartzTeam team)
    {
        if (team == null)
        {
            return teamChatLocked.contains(playerID);
        }
        else
        {
            final QuartzTeam lockedTeam = this.otherTeamChatLocked.get(playerID);
            final QuartzTeam playerTeam = QuartzTeams.get().getTeamForPlayer(playerID);

            return (lockedTeam != null && lockedTeam.equals(team)) || (playerTeam != null && playerTeam.equals(team));
        }
    }

    /**
     * Returns true if the team chat is enabled for the given player.
     *
     * @param playerID The player's UUID.
     * @return {@code true} if the team chat is enabled for the given player.
     */
    public boolean isTeamChatEnabled(final UUID playerID)
    {
        return this.isTeamChatEnabled(playerID, null);
    }

    /**
     * Returns true if the team chat is enabled for the given player.
     *
     * @param player The player.
     * @param team If non-null, this will check if the given player is spying the current team.
     * @return {@code true} if the team chat is enabled for the given player.
     */
    public boolean isTeamChatEnabled(final Player player, final QuartzTeam team)
    {
        return isTeamChatEnabled(player.getUniqueId(), team);
    }

    /**
     * Returns true if the team chat is enabled for the given player.
     *
     * @param player The player.
     * @return {@code true} if the team chat is enabled for the given player.
     */
    public boolean isTeamChatEnabled(final Player player)
    {
        return isTeamChatEnabled(player.getUniqueId(), null);
    }

    /**
     * Returns true if the given player is in the team chat of another team.
     *
     * @param playerID The player's UUID.
     * @return {@code true} if the given player is in the team chat of another team.
     */
    public boolean isOtherTeamChatEnabled(final UUID playerID)
    {
        return otherTeamChatLocked.containsKey(playerID);
    }

    /**
     * Returns true if the given player is in the team chat of another team.
     *
     * @param player The player.
     * @return {@code true} if the given player is in the team chat of another team.
     */
    public boolean isOtherTeamChatEnabled(final Player player)
    {
        return isOtherTeamChatEnabled(player.getUniqueId());
    }

    /**
     * Returns true if a team chat is enabled for the given player.
     *
     * @param playerID The player's UUID.
     * @return {@code true} if a team chat is enabled for the given player.
     */
    public boolean isAnyTeamChatEnabled(final UUID playerID)
    {
        return (teamChatLocked.contains(playerID) || otherTeamChatLocked.containsKey(playerID));
    }

    /**
     * Returns true if a team chat is enabled for the given player.
     *
     * @param player The player.
     * @return {@code true} if a team chat is enabled for the given player.
     */
    public boolean isAnyTeamChatEnabled(final Player player)
    {
        return isAnyTeamChatEnabled(player.getUniqueId());
    }

    /**
     * Returns the other team viewed by the given player, or null if the player is not in
     * the chat of another team.
     *
     * @param player The player.
     * @return The other team viewed by the given player.
     */
    public QuartzTeam getOtherTeamEnabled(final Player player)
    {
        return otherTeamChatLocked.get(player.getUniqueId());
    }


    /**
     * Registers a player receiving ALL the teams chats.
     *
     * @param playerID The spy's UUID.
     */
    public void addGlobalSpy(final UUID playerID)
    {
        globalSpies.add(playerID);
    }

    /**
     * Stops a player from receiving ALL the teams chats.
     *
     * @param playerID The spy's UUID.
     */
    public void removeGlobalSpy(final UUID playerID)
    {
        globalSpies.remove(playerID);
    }

    /**
     * Checks if the given player receives all the teams chats.
     *
     * @param playerID The spy's UUID.
     * @return {@code true} if spying.
     */
    public boolean isGlobalSpy(final UUID playerID)
    {
        return globalSpies.contains(playerID);
    }



    /**
     * Used to send the chat to the team-chat if this team-chat is enabled.
     */
    // Priority LOWEST to be able to cancel the event before all other plugins
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent ev)
    {
        // If the event is asynchronous, the message was sent by a "real" player.
        // Else, the message was sent by a plugin (like our /g command, or another plugin), and
        // the event is ignored.
        if (ev.isAsynchronous())
        {
            if (isTeamChatEnabled(ev.getPlayer()))
            {
                ev.setCancelled(true);
                sendTeamMessage(ev.getPlayer(), ev.getMessage());
            }
            else if (isOtherTeamChatEnabled(ev.getPlayer()) && QuartzTeamsPermission.TALK_IN_OTHER_TEAM_CHAT.grantedTo(ev.getPlayer()))
            {
                ev.setCancelled(true);
                sendTeamMessage(ev.getPlayer(), ev.getMessage(), getOtherTeamEnabled(ev.getPlayer()));
            }
        }
    }
}
