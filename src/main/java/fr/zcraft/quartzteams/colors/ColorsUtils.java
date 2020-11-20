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
package fr.zcraft.quartzteams.colors;

import com.google.common.collect.ImmutableSet;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;

import java.util.Set;


public final class ColorsUtils
{
    private static final Set<String> dyeableMaterials = ImmutableSet.of(
            "BANNER",
            "BED",
            "CARPET",
            "CONCRETE",
            "CONCRETE_POWDER",
            "DYE",
            "GLAZED_TERRACOTTA",
            "SHULKER_BOX",
            "STAINED_GLASS",
            "STAINED_GLASS_PANE",
            "TERRACOTTA",
            "WALL_BANNER",
            "WOOL"
    );

    private ColorsUtils() {}

    /**
     * Converts a chat color to its dye equivalent.
     *
     * <p>The transformation is not perfect as there is no 1:1
     * correspondence between dyes and chat colors.</p>
     *
     * @param chatColor The chat color.
     * @return The corresponding dye.
     */
    public static DyeColor chat2Dye(ChatColor chatColor)
    {
        switch (chatColor)
        {
            case BLACK:
                return DyeColor.BLACK;

            case BLUE:
            case DARK_BLUE:
                return DyeColor.BLUE;

            case DARK_GREEN:
                return DyeColor.GREEN;

            case DARK_AQUA:
                return DyeColor.CYAN;

            case DARK_RED:
                return DyeColor.RED;

            case DARK_PURPLE:
                return DyeColor.PURPLE;

            case GOLD:
            case YELLOW:
                return DyeColor.YELLOW;

            case GRAY:
                return DyeColor.LIGHT_GRAY;

            case DARK_GRAY:
                return DyeColor.GRAY;

            case GREEN:
                return DyeColor.LIME;

            case AQUA:
                return DyeColor.LIGHT_BLUE;

            case RED:
                return DyeColor.ORANGE;

            case LIGHT_PURPLE:
                return DyeColor.PINK;

            // White, reset & formatting
            default:
                return DyeColor.WHITE;
        }
    }

    /**
     * Converts a dye color to a dyeable material.
     *
     * @param color The dye color.
     * @param materialBaseName The base name of the material: its name without the
     *                         color part. E.g. {@code "STAINED_GLASS"} or {@code "BED"}.
     * @return The corresponding material.
     * @throws IllegalArgumentException If the given block is not dyeable.
     */
    public static Material dye2Block(final DyeColor color, final String materialBaseName) {
        if (!dyeableMaterials.contains(materialBaseName)) {
            throw new IllegalArgumentException(materialBaseName + " is not dyeable.");
        }

        return Material.valueOf(color.name() + "_" + materialBaseName.toUpperCase());
    }

    /**
     * Converts a chat color to a dyeable material.
     *
     * @param color The chat color.
     * @param materialBaseName The base name of the material: its name without the
     *                         color part. E.g. {@code "STAINED_GLASS"} or {@code "BED"}.
     * @return The corresponding material.
     * @throws IllegalArgumentException If the given block is not dyeable.
     */
    public static Material chat2Block(final ChatColor color, final String materialBaseName) {
        return dye2Block(chat2Dye(color), materialBaseName);
    }
}
