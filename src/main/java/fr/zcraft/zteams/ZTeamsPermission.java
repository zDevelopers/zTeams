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

import org.bukkit.permissions.Permissible;


public enum ZTeamsPermission
{
    CREATE_TEAM("Allows an user to create a team", false),
    DELETE_TEAM("Allows an user to delete a team", true),
    RESET_TEAMS("Allows an user to delete all teams", true),

    JOIN_TEAM("Allows an user to join a team", false),
    LEAVE_TEAM("Allows an user to leave a team", false),

    UPDATE_TEAMS_PLAYERS_LIST("Allows an user to add or remove players from teams", true),

    UPDATE_TEAM_NAME("Allows an user to update its own team name", false),
    UPDATE_TEAM_COLOR("Allows an user to update its own team color", false),
    UPDATE_TEAM_BANNER("Allows an user to update its own team banner", false),

    UPDATE_OTHER_TEAM_NAME("Allows an user to update another team name", true),
    UPDATE_OTHER_TEAM_COLOR("Allows an user to update another team color", true),
    UPDATE_OTHER_TEAM_BANNER("Allows an user to update another team banner", true),

    ENTER_PRIVATE_CHAT("Allows an user to enter its team's private chat", false),
    SPY_TEAM_CHAT("Allows an user to enter the private team chat of another team", true),
    TALK_IN_OTHER_TEAM_CHAT("Allows an user to talk inside the private team chat of another team", true),
    SPY_ALL_TEAMS("Allows an user to see the private team chat of all other teams", true),
    MAKE_ANOTHER_SPY_ALL_TEAMS("Allows an user to enable spy mode for another player", true)
    ;


    private final String description;
    private final boolean administrative;

    ZTeamsPermission(final String description, final boolean administrative)
    {
        this.description = description;
        this.administrative = administrative;
    }

    /**
     * @return A description of the permission in English.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @return {@code true} if the permission is considered as administrative.
     */
    public boolean isAdministrative()
    {
        return administrative;
    }

    /**
     * Checks if the permission is granted to the given permissible using the registered
     * {@linkplain fr.zcraft.zteams.permissions.PermissionsChecker permissions checker}.
     *
     * @param permissible The permissible.
     * @return {@code true} if the permission is granted.
     */
    public boolean grantedTo(Permissible permissible)
    {
        return ZTeams.get().permissionsChecker().hasPermission(permissible, this);
    }
}
