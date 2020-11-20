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
package fr.zcraft.quartzteams.commands;

import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.components.rawtext.RawText;
import fr.zcraft.quartzteams.QuartzTeam;
import fr.zcraft.quartzteams.QuartzTeams;
import fr.zcraft.quartzteams.QuartzTeamsPermission;
import fr.zcraft.quartzteams.texts.TextUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandInfo (name = "toggle-chat", usageParameters = "[* | \"<team name>\"]", aliases = "togglechat")
public class ToggleChatCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        // /togglechat
        if (args.length == 0)
        {
            if (!QuartzTeamsPermission.ENTER_PRIVATE_CHAT.grantedTo(playerSender()))
            {
                throwNotAuthorized();
            }

            if (QuartzTeams.chatManager().toggleChatForPlayer(playerSender()))
            {
                success(I.t("{cs}You are now chatting with your team only."));
                info(I.t("To exit, execute {0}", build()));
            }
            else
            {
                success(I.t("{cs}You are now chatting with everyone."));
            }
        }

        // /togglechat <another team>
        else
        {
            if (!QuartzTeamsPermission.SPY_TEAM_CHAT.grantedTo(playerSender()))
            {
                throwNotAuthorized();
            }

            // Teams selector
            if (args.length == 1 && args[0].equals("*"))
            {
                info("");
                info(I.t("{green}{bold}Click on a team below to enter their private chat"));

                final RawText buttons = new RawText();
                int i = 0;

                for (final QuartzTeam team : QuartzTeams.get().getTeams())
                {
                    final RawText tooltip = new RawText(team.getName()).color(team.getColorOrWhite().toChatColor());
                    team.getPlayers().forEach(player -> tooltip.then("\n- ").color(ChatColor.GRAY).then(player.getName()).color(ChatColor.WHITE));
                    tooltip.then("\n\n» ").color(ChatColor.DARK_GRAY).then(I.t("{white}Click here {gray}to enter this team's private chat"));

                    buttons
                        .then("[  " + team.getName() + "  ]")
                            .color(team.getColorOrWhite().toChatColor())
                            .command(ToggleChatCommand.class, "\"" + team.getName() + "\"")
                            .hover(tooltip);

                    if (i % 3 != 2)
                    {
                        buttons.then("    ");
                    }
                    else
                    {
                        buttons.then("\n");
                    }

                    i++;
                }

                send(buttons);
                return;
            }

            final QuartzTeam team = QuartzTeams.get().getTeamByName(TextUtils.extractArgsWithQuotes(args, 0)[0]);

            if (team != null)
            {
                if (QuartzTeams.chatManager().toggleChatForPlayer((Player) sender, team))
                {
                    success(I.t("{cs}You are now chatting with the team {0}{cs}.", team.getDisplayName()));
                    info(I.t("To exit, execute {0}", build()));

                    if (!QuartzTeamsPermission.TALK_IN_OTHER_TEAM_CHAT.grantedTo(sender))
                    {
                        info(I.t("{gray}{bold}Warning:{gray} you are not allowed to talk to the team. Even if you're spying the team's private chat, your messages will go to the global chat."));
                    }
                }
            }
            else
            {
                warning(I.t("{ce}This team does not exists."));
                send(
                    new RawText(I.t("Execute {0} or click here to select the team chat to enter by clicking on it.", build("*")))
                        .color(ChatColor.GRAY)
                        .command(ToggleChatCommand.class, "*")
                );
            }
        }
    }
}
