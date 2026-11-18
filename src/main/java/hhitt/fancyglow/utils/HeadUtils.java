package hhitt.fancyglow.utils;

import hhitt.fancyglow.FancyGlow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;
import java.util.logging.Logger;

public class HeadUtils {
    private static final Logger LOGGER = FancyGlow.getPlugin(FancyGlow.class).getLogger();

    /**
     * @param base64 Base64 string to use as texture.
     *
     * @return A head item stack with the specified texture.
     */
    public static ItemStack getCustomSkull(String base64) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        if (base64 == null || base64.isEmpty()) {
            return skull;
        }
        URL urlObject;
        try {
            urlObject = getUrlFromBase64(base64);
        } catch (MalformedURLException e) {
            LOGGER.warning("Unexpected exception while trying to get textured head with the following message: " + e.getMessage());
            return skull;
        }
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "FancyCustomHead");
        PlayerTextures textures = profile.getTextures();
        textures.setSkin(urlObject); // Set the skin of the player profile to the URL
        profile.setTextures(textures); // Set the textures back to the profile

        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwnerProfile(profile); // Set the owning player of the head to the player profile
        skull.setItemMeta(meta);

        return skull;
    }

    /**
     * @param base64 Base64 string.
     *
     * @return URL object to use as skin.
     * @throws MalformedURLException
     */
    public static URL getUrlFromBase64(String base64) throws MalformedURLException {
        final String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(base64.trim()), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new MalformedURLException("Texture is not valid base64");
        }

        // Find the skin URL instead of cutting fixed offsets: the old substring() only worked for the exact
        // {"textures":{"SKIN":{"url":"..."}}} layout and threw StringIndexOutOfBoundsException (uncaught, breaking
        // the menu) or built a garbage URL for textures that also contain timestamp/profileId/metadata fields.
        Matcher matcher = SKIN_URL.matcher(decoded);
        if (!matcher.find()) {
            throw new MalformedURLException("No skin URL found in texture");
        }

        return new URL(matcher.group(1));
    }

    private static final Pattern SKIN_URL = Pattern.compile("\"SKIN\"\\s*:\\s*\\{[^}]*?\"url\"\\s*:\\s*\"([^\"]+)\"");
}
