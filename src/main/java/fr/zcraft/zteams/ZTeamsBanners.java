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

import fr.zcraft.zlib.tools.items.TextualBanners;
import fr.zcraft.zteams.texts.TextUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.zcraft.zlib.tools.items.TextualBanners.getBannerMeta;

public class ZTeamsBanners
{
    private static final Map<String, BannerMeta> specialBanners = new HashMap<>();

    static
    {
        specialBanners.put("banana", getBannerMeta(DyeColor.WHITE, Arrays.asList(new Pattern(DyeColor.YELLOW, PatternType.MOJANG), new Pattern(DyeColor.BROWN, PatternType.STRIPE_DOWNRIGHT), new Pattern(DyeColor.WHITE, PatternType.CIRCLE_MIDDLE), new Pattern(DyeColor.WHITE, PatternType.STRIPE_TOP), new Pattern(DyeColor.WHITE, PatternType.HALF_VERTICAL_MIRROR), new Pattern(DyeColor.WHITE, PatternType.STRIPE_TOP))));
    }

    public static ItemStack getDefaultBanner(final String name, final DyeColor color)
    {
        if (ZTeams.settings().bannerAllowSpecialShapes())
        {
            final String lowerName = name.toLowerCase();

            if (lowerName.contains("banana") || lowerName.contains("banane"))
            {
                return getSpecialBanner("banana", color != DyeColor.YELLOW ? color : DyeColor.GREEN);
            }
        }

        if (ZTeams.settings().bannerShapeWriteLetter())
        {
            return TextualBanners.getCharBanner(
                    Character.toUpperCase(TextUtils.getInitialLetter(name)),
                    color,
                    ZTeams.settings().bannerShapeAddBorder()
            );
        }
        else
        {
            final ItemStack banner = new ItemStack(Material.BANNER);
            final BannerMeta meta = (BannerMeta) banner.getItemMeta();

            meta.setBaseColor(color);
            banner.setItemMeta(meta);

            return banner;
        }
    }

    private static ItemStack getSpecialBanner(final String name, final DyeColor background)
    {
        final BannerMeta meta = specialBanners.get(name);
        if (meta == null) throw new IllegalArgumentException("Unknown special banner " + name);

        List<Pattern> patterns = meta.getPatterns();

        for (int i = 0; i < patterns.size(); i++)
        {
            DyeColor patternColor = patterns.get(i).getColor().equals(DyeColor.WHITE) ? background : patterns.get(i).getColor();
            patterns.set(i, new Pattern(patternColor, patterns.get(i).getPattern()));
        }

        return TextualBanners.getBanner(meta.getBaseColor().equals(DyeColor.WHITE) ? background : meta.getBaseColor(), patterns);
    }
}
