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

import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zlib.tools.items.TextualBanners;
import fr.zcraft.zteams.colors.ColorsUtils;
import fr.zcraft.zteams.colors.TeamColor;
import fr.zcraft.zteams.creator.ZTeamCreator;
import fr.zcraft.zteams.events.PlayerJoinedTeamEvent;
import fr.zcraft.zteams.events.PlayerLeftTeamEvent;
import fr.zcraft.zteams.events.PlayerPreJoinTeamEvent;
import fr.zcraft.zteams.events.PlayerPreLeaveTeamEvent;
import fr.zcraft.zteams.events.TeamUpdatedEvent;
import fr.zcraft.zteams.texts.TextUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.scoreboard.Team;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Represents a team.
 *
 * If you need to add your own teams attribute, subclass this class,
 * instantiate a custom parametrized {@link ZTeams<YourTeamClass>} instance,
 * and provide a custom {@link ZTeamCreator} to create new teams instances.
 */
public class ZTeam
{
    private static final Random random = new Random();

    private String name = null;
    private String internalName;
    private String displayName = null;
    private TeamColor color = null;
    private ItemStack defaultBanner = null;
    private ItemStack banner = null;

    private Team internalTeam;

    private Set<UUID> players = new HashSet<>();


    public ZTeam(String name, TeamColor color)
    {
        Validate.notNull(name, "The name cannot be null.");

        // We use a random internal name because the name of a team, in Minecraft vanilla, is limited
        // (16 characters max).
        this.internalName = String.valueOf(random.nextInt(99999999)) + String.valueOf(random.nextInt(99999999));
        this.internalTeam = ZTeams.settings().scoreboard().registerNewTeam(this.internalName);

        setName(name);
        setColor(color);
        updateTeamOptions();
        updateDefaultBanner();
    }



    /* *** Getters *** */


    /**
     * Returns the name of the team.
     *
     * Can include spaces.
     *
     * @return The name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the display name of the team.
     *
     * This name is:
     *  - if the team is uncolored, the name of the team;
     *  - else, the name of the team with:
     *     - before, the color of the team;
     *     - after, the "reset" formatting mark (§r).
     *
     * @return The display name.
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * @return the color of the team.
     */
    public TeamColor getColor()
    {
        return color;
    }

    /**
     * @return the color of the team, or white if the color is set to null. Never returns {@code null}.
     */
    public TeamColor getColorOrWhite()
    {
        return color != null ? color : TeamColor.WHITE;
    }

    /**
     * Returns this team's banner. If not set, falls back to the default one.
     *
     * @return the banner.
     */
    public ItemStack getBanner()
    {
        return banner == null ? defaultBanner : banner;
    }

    /**
     * Return the default banner for this team, following the
     * banners options set.
     *
     * @return the generated banner.
     */
    public ItemStack getDefaultBanner()
    {
        return defaultBanner;
    }

    /**
     * Returns the players inside this team.
     *
     * @return The players.
     */
    public Set<OfflinePlayer> getPlayers()
    {
        return players.stream()
                .map(uuid -> Bukkit.getServer().getOfflinePlayer(uuid))
                .collect(Collectors.toSet());
    }

    /**
     * Returns the UUIDs of the players inside this team.
     *
     * @return The UUIDs of the players.
     */
    @SuppressWarnings ("unchecked")
    public Set<UUID> getPlayersUUID()
    {
        return Collections.unmodifiableSet(players);
    }

    /**
     * Returns the online players inside this team.
     *
     * @return The online players.
     */
    public Set<Player> getOnlinePlayers()
    {
        return players.stream()
                .map(uuid -> Bukkit.getServer().getPlayer(uuid))
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the UUIDs of the online players inside this team.
     *
     * @return The UUID of the online players.
     */
    public Set<UUID> getOnlinePlayersUUID()
    {
        return players.stream()
                .map(uuid -> Bukkit.getServer().getPlayer(uuid))
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .map(Player::getUniqueId)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the size of this team.
     *
     * @return The size.
     */
    public int getSize()
    {
        return players.size();
    }

    /**
     * Returns true if the team is empty.
     *
     * @return The emptiness.
     */
    public boolean isEmpty()
    {
        return getSize() == 0;
    }

    /**
     * Returns true if the team is full.
     *
     * @return The fullness.
     */
    public boolean isFull()
    {
        return ZTeams.settings().maxPlayersPerTeam() != 0 && getSize() >= ZTeams.settings().maxPlayersPerTeam();
    }



    /* *** Setters *** */


    /**
     * Changes the name of this team.
     *
     * @param name The new name.
     */
    public void setName(final String name)
    {
        Validate.notNull(internalTeam, "This team was deleted");

        if (name == null || (this.name != null && this.name.equals(name)))
            return;

        this.name = name;

        updateDisplayName();
        updateDefaultBanner();

        ZTeams.fireEvent(new TeamUpdatedEvent(this));
    }

    /**
     * Updates the team color.
     *
     * @param color The new color.
     */
    public void setColor(final TeamColor color)
    {
        Validate.notNull(internalTeam, "This team was deleted");

        // We don't use generateColor directly because we want to keep the "null" color.
        this.color = color == TeamColor.RANDOM ? ZTeams.get().generateColor(color) : color;

        updateDisplayName();

        // The team color needs to be updated
        if (this.color != null)
        {
            internalTeam.setPrefix(this.color.toChatColor().toString());
        }

        // The players names too
        getOnlinePlayers().forEach(player -> ZTeams.get().colorizePlayer(player));

        // The default banner too
        updateDefaultBanner();

        ZTeams.fireEvent(new TeamUpdatedEvent(this));
    }

    /**
     * Updates this team's banner.
     *
     * @param banner The new banner. {@code null} to use the default banner. The provided {@link ItemStack} will be cloned.
     */
    public void setBanner(final ItemStack banner)
    {
        if (banner == null)
        {
            this.banner = null;
            return;
        }

        if (banner.getType() != Material.BANNER)
            throw new IllegalArgumentException("A banner is required");

        this.banner = new ItemStackBuilder(banner.clone())
                .title(displayName)
                .amount(1)
                .hideAttributes()
                .item();

        if (banner.hasItemMeta())
        {
            BannerMeta meta = (BannerMeta) this.banner.getItemMeta();
            meta.setBaseColor(((BannerMeta) banner.getItemMeta()).getBaseColor());
            this.banner.setItemMeta(meta);
        }

        ZTeams.fireEvent(new TeamUpdatedEvent(this));
    }

    /**
     * Updates this team's banner.
     *
     * @param banner The new banner. {@code null} to use the default banner.
     */
    public void setBanner(final BannerMeta banner)
    {
        if (banner == null)
        {
            this.banner = null;
            return;
        }

        this.banner = new ItemStackBuilder(Material.BANNER)
                .title(displayName)
                .amount(1)
                .hideAttributes()
                .item();

        this.banner.setItemMeta(banner.clone());

        ZTeams.fireEvent(new TeamUpdatedEvent(this));
    }



    /* *** Players manipulation *** */


    /**
     * Adds a player inside this team.
     *
     * @param player The player to add.
     */
    public void addPlayer(final OfflinePlayer player)
    {
        Validate.notNull(internalTeam, "This team was deleted");
        Validate.notNull(player, "The player cannot be null.");

        if (ZTeams.settings().maxPlayersPerTeam() != 0 && this.players.size() >= ZTeams.settings().maxPlayersPerTeam())
        {
            throw new RuntimeException("The team " + name + " is full");
        }

        final PlayerPreJoinTeamEvent event = new PlayerPreJoinTeamEvent(this, player);
        ZTeams.fireEvent(event);

        if (event.isCancelled()) return;

        // Removes the player for an eventual other team
        ZTeams.get().removePlayerFromTeam(player, true);

        players.add(player.getUniqueId());
        internalTeam.addPlayer(player);

        ZTeams.get().colorizePlayer(player);

        ZTeams.fireEvent(new PlayerJoinedTeamEvent(this, player));
    }

    /**
     * Removes a player from this team.
     *
     * Nothing is done if the player wasn't in this team.
     *
     * @param player The player to remove.
     */
    public void removePlayer(final OfflinePlayer player)
    {
        removePlayer(player, false);
    }

    /**
     * Removes a player from this team.
     *
     * Nothing is done if the player wasn't in this team.
     *
     * @param playerID The player to remove.
     */
    public void removePlayer(UUID playerID)
    {
        removePlayer(playerID, false);
    }

    /**
     * Removes a player from this team.
     *
     * Nothing is done if the player wasn't in this team.
     *
     * @param player The player to remove.
     * @param becauseJoin {@code true} if the player is removed to go into another team.
     */
    void removePlayer(final OfflinePlayer player, boolean becauseJoin)
    {
        Validate.notNull(internalTeam, "This team was deleted");
        Validate.notNull(player, "The player cannot be null.");

        if (!becauseJoin)
        {
            final PlayerPreLeaveTeamEvent event = new PlayerPreLeaveTeamEvent(this, player);
            ZTeams.fireEvent(event);

            if (event.isCancelled()) return;
        }

        players.remove(player.getUniqueId());
        unregisterPlayer(player);

        ZTeams.fireEvent(new PlayerLeftTeamEvent(this, player, becauseJoin));
    }

    /**
     * Removes a player from this team.
     *
     * Nothing is done if the player wasn't in this team.
     *
     * @param playerID The player to remove.
     * @param becauseJoin {@code true} if the player is removed to go into another team.
     */
    void removePlayer(UUID playerID, boolean becauseJoin)
    {
        Validate.notNull(playerID, "The player's UUID cannot be null");

        final OfflinePlayer player = Bukkit.getOfflinePlayer(playerID);
        Validate.notNull(player, "The player with the UUID " + playerID + " cannot be found");

        removePlayer(player, becauseJoin);
    }

    /**
     * Unregisters a player from the scoreboard and uncolorizes the pseudo.
     *
     * Internal use, avoids a ConcurrentModificationException in this.deleteTeam()
     * (this.players is listed and emptied simultaneously, else).
     */
    private void unregisterPlayer(final OfflinePlayer player)
    {
        if (player == null) return;

        internalTeam.removePlayer(player);
        ZTeams.get().colorizePlayer(player);
    }

    /**
     * Returns true if the given player is in this team.
     *
     * @param player The player to check.
     * @return true if the given player is in this team.
     */
    public boolean containsPlayer(final Player player)
    {
        Validate.notNull(player, "The player cannot be null.");

        return players.contains(player.getUniqueId());
    }

    /**
     * Returns true if the player with the given UUID is in this team.
     *
     * @param playerID The UUID of the player to check.
     * @return true if the given player is in this team.
     */
    public boolean containsPlayer(final UUID playerID)
    {
        Validate.notNull(playerID, "The player cannot be null.");

        return players.contains(playerID);
    }

    /**
     * Deletes this team.
     *
     * The players inside the team are left without any team.
     */
    public void deleteTeam()
    {
        Validate.notNull(internalTeam, "This team was deleted");

        // We removes the players from the team (scoreboard team too)
        players.stream().map(uuid -> Bukkit.getServer().getOfflinePlayer(uuid)).forEach(this::unregisterPlayer);
        players.clear();

        // Then the scoreboard team is deleted.
        internalTeam.unregister();
        internalTeam = null;

        ZTeams.get().unregisterTeam(this);
    }

    /**
     * Teleports the entire team to the given location.
     *
     * @param location The location.
     */
    public void teleportTo(final Location location)
    {
        Validate.notNull(location, "The location cannot be null.");

        players.stream()
                .map(id -> Bukkit.getServer().getPlayer(id))
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .forEach(player -> player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN));
    }



    /* *** Others utilities & internals *** */


    /**
     * Updates the team's display name based on the current color.
     */
    private void updateDisplayName()
    {
        displayName = (color != null) ? color.toChatColor() + name + ChatColor.RESET : name;
        internalTeam.setDisplayName(displayName.substring(0, Math.min(displayName.length(), 32)));
    }

    /**
     * Update the team options according to the current ZTeams configuration.
     */
    void updateTeamOptions()
    {
        internalTeam.setSuffix(ChatColor.RESET.toString());
        internalTeam.setCanSeeFriendlyInvisibles(ZTeams.settings().teamsOptionsSeeFriendlyInvisibles());
        internalTeam.setAllowFriendlyFire(ZTeams.settings().teamsOptionsFriendlyFire());
    }

    /**
     * Regenerates the default banner.
     */
    void updateDefaultBanner()
    {
        // Avoid updating in the constructor before all the object is populated.
        if (name == null)
        {
            defaultBanner = new ItemStack(Material.BANNER);
            return;
        }

        final DyeColor dye = ColorsUtils.chat2Dye(getColorOrWhite().toChatColor());

        if (ZTeams.settings().bannerShapeWriteLetter())
        {
            defaultBanner = TextualBanners.getCharBanner(
                Character.toUpperCase(TextUtils.getInitialLetter(name)),
                dye,
                ZTeams.settings().bannerShapeAddBorder()
            );
        }
        else
        {
            defaultBanner = new ItemStack(Material.BANNER);
            BannerMeta meta = (BannerMeta) defaultBanner.getItemMeta();
            meta.setBaseColor(dye);
            defaultBanner.setItemMeta(meta);
        }
    }


    @Override
    public int hashCode()
    {
        return ((internalName == null) ? 0 : internalName.hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ZTeam))
            return false;

        final ZTeam other = (ZTeam) obj;
        return name == null ? other.name == null : name.equals(other.name);
    }
}
