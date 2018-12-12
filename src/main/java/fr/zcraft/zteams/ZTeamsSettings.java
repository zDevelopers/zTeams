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
package fr.zcraft.zteams;

import fr.zcraft.zlib.core.ZLib;
import fr.zcraft.zteams.guis.TeamsGUIItemType;
import org.bukkit.scoreboard.Scoreboard;


public class ZTeamsSettings
{
    private Scoreboard scoreboard = null;

    private boolean bannerShapeWriteLetter = true;
    private boolean bannerShapeAddBorder = false;
    private boolean bannerAllowSpecialShapes = true;

    private boolean teamsOptionsSeeFriendlyInvisibles = true;
    private boolean teamsOptionsFriendlyFire = true;
    private boolean teamsOptionsColorizeChat = true;
    private boolean teamsOptionsAllowDuplicatedNames = false;

    private TeamsGUIItemType teamsGUIItemType = TeamsGUIItemType.BANNER;
    private boolean teamsGUIGlowOnCurrentTeam = true;

    private int maxPlayersPerTeam = 0;

    private boolean teamChatLogInConsole = false;



    /* *** Getters *** */


    /**
     * @return The Minecraft scoreboard the teams will use.
     */
    public Scoreboard scoreboard()
    {
        return scoreboard != null ? scoreboard : ZLib.getPlugin().getServer().getScoreboardManager().getMainScoreboard();
    }

    /**
     * @return {@code true} if the first meaningful letter of the team name should be written
     * on the default team banners.
     */
    public boolean bannerShapeWriteLetter()
    {
        return bannerShapeWriteLetter;
    }

    /**
     * @return {@code true} if a border should be added to the default team banners.
     */
    public boolean bannerShapeAddBorder()
    {
        return bannerShapeAddBorder;
    }

    /**
     * @return {@code true} if “easter-egg” default banners with special shapes based on names are allowed.
     */
    public boolean bannerAllowSpecialShapes()
    {
        return bannerAllowSpecialShapes;
    }

    /**
     * @return {@code true} if the players should see their invisible teammates.
     */
    public boolean teamsOptionsSeeFriendlyInvisibles()
    {
        return teamsOptionsSeeFriendlyInvisibles;
    }

    /**
     * @return {@code true} if the PvP should be enabled between teammates.
     */
    public boolean teamsOptionsFriendlyFire()
    {
        return teamsOptionsFriendlyFire;
    }

    /**
     * @return {@code true} if the pseudonyms in the chat should be colorized
     * (by setting their display name including their team color).
     */
    public boolean teamsOptionsColorizeChat()
    {
        return teamsOptionsColorizeChat;
    }

    /**
     * @return {@code true} if duplicated team names are allowed.
     */
    public boolean teamsOptionsAllowDuplicatedNames()
    {
        return teamsOptionsAllowDuplicatedNames;
    }

    /**
     * @return The GUI item to use to represent teams.
     */
    public TeamsGUIItemType teamsGUIItemType()
    {
        return teamsGUIItemType;
    }

    /**
     * @return {@code true} if one's current team should glow on the GUIs.
     */
    public boolean teamsGUIGlowOnCurrentTeam()
    {
        return teamsGUIGlowOnCurrentTeam;
    }

    /**
     * @return The maximal number of players per team. {@code 0} means “no limit”.
     */
    public int maxPlayersPerTeam()
    {
        return maxPlayersPerTeam;
    }

    /**
     * @return {@code true} if the teams' private chat should be logged into the console.
     */
    public boolean teamChatLogInConsole()
    {
        return teamChatLogInConsole;
    }



    /* *** Setters *** */


    /**
     * Sets the scoreboard to be used by the teams to be displayed and registered into Bukkit.
     *
     * If this is never set, or reset by setting the scoreboard to {@code null}, the main Bukkit scoreboard
     * will be used.
     *
     * @param scoreboard The scoreboard to use.
     */
    public ZTeamsSettings setScoreboard(final Scoreboard scoreboard)
    {
        this.scoreboard = scoreboard;
        return this;
    }

    /**
     * Updates the settings followed to generate the default teams banners.
     *
     * All default banners will be regenerated when this method is called, according to the new settings.
     *
     * @param bannerShapeWriteLetter {@code true} if the first meaningful letter of the team name
     *                               should be written on the default team banners. Default {@code true}.
     * @param bannerShapeAddBorder {@code true} if a border should be added to the default team banners. Default {@code false}.
     */
    public ZTeamsSettings setBannerOptions(final boolean bannerShapeWriteLetter, final boolean bannerShapeAddBorder)
    {
        return setBannerOptions(bannerShapeWriteLetter, bannerShapeAddBorder, true);
    }

    /**
     * Updates the settings followed to generate the default teams banners.
     *
     * All default banners will be regenerated when this method is called, according to the new settings.
     *  @param bannerShapeWriteLetter {@code true} if the first meaningful letter of the team name
     *                                should be written on the default team banners. Default {@code true}.
     * @param bannerShapeAddBorder {@code true} if a border should be added to the default team banners.
     *                             Default {@code false}.
     * @param bannerAllowSpecialShapes {@code true} if “easter-egg” default banners with special shapes
     *                                 based on names are allowed. Default {@code true}.
     */
    public ZTeamsSettings setBannerOptions(final boolean bannerShapeWriteLetter, final boolean bannerShapeAddBorder, boolean bannerAllowSpecialShapes)
    {
        this.bannerShapeWriteLetter = bannerShapeWriteLetter;
        this.bannerShapeAddBorder = bannerShapeAddBorder;
        this.bannerAllowSpecialShapes = bannerAllowSpecialShapes;

        ZTeams.get().updateDefaultBanners();

        return this;
    }

    /**
     * Updates the teams options for all teams.
     *
     * All teams settings will be updated when this method is called.
     *
     * @param teamsOptionsSeeFriendlyInvisibles {@code true} if the players should see their invisible teammates. Default {@code true}.
     * @param teamsOptionsFriendlyFire {@code true} if the PvP should be enabled between teammates. Default {@code true}.
     * @param teamsOptionsColorizeChat {@code true} to colorize the players name in the chat (by setting their display
     *                                 name including their team color). Default {@code true}.
     * @param teamsOptionsAllowDuplicatedNames {@code true} to allow multiple teams with the same name to be registered. Default {@code false}.
     */
    public ZTeamsSettings setTeamsOptions(
            final boolean teamsOptionsSeeFriendlyInvisibles,
            final boolean teamsOptionsFriendlyFire,
            final boolean teamsOptionsColorizeChat,
            final boolean teamsOptionsAllowDuplicatedNames)
    {
        this.teamsOptionsSeeFriendlyInvisibles = teamsOptionsSeeFriendlyInvisibles;
        this.teamsOptionsFriendlyFire = teamsOptionsFriendlyFire;
        this.teamsOptionsColorizeChat = teamsOptionsColorizeChat;
        this.teamsOptionsAllowDuplicatedNames = teamsOptionsAllowDuplicatedNames;

        ZTeams.get().updateTeamsOptions();

        return this;
    }

    public ZTeamsSettings setTeamsChatOptions(final boolean teamChatLogInConsole)
    {
        this.teamChatLogInConsole = teamChatLogInConsole;

        return this;
    }

    /**
     * Updates the GUI settings.
     *
     * All GUIs will be updated to follow these new settings when this method is called.
     *
     * @param teamsGUIItemType The item to use to represent teams on the GUIs. Default {@link TeamsGUIItemType#BANNER BANNER}.
     * @param teamsGUIGlowOnCurrentTeam {@code true} to add glow on the player's current team. Default {@code true}.
     */
    public ZTeamsSettings setGUIOptions(final TeamsGUIItemType teamsGUIItemType, final boolean teamsGUIGlowOnCurrentTeam)
    {
        this.teamsGUIItemType = teamsGUIItemType != null ? teamsGUIItemType : TeamsGUIItemType.BANNER;
        this.teamsGUIGlowOnCurrentTeam = teamsGUIGlowOnCurrentTeam;

        ZTeams.get().updateGUIs();

        return this;
    }

    /**
     * Updates the maximal amount of players per team. If some teams overflow the new limit, players will not be kicked
     * but new one will not be able to join.
     *
     * @param maxPlayersPerTeam The maximal number of players per team. {@code 0} to remove the limit (default value).
     */
    public ZTeamsSettings setMaxPlayersPerTeam(final int maxPlayersPerTeam)
    {
        this.maxPlayersPerTeam = maxPlayersPerTeam;

        ZTeams.get().updateGUIs();

        return this;
    }
}
