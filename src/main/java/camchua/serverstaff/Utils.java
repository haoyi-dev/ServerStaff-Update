package camchua.serverstaff;

import org.bukkit.Bukkit;
import org.bukkit.Material;

public class Utils {

    private static boolean LEGACY = true;

    public static void checkVersion() {
        String version = Bukkit.getBukkitVersion().split("-")[0];
        int versionMajor = Integer.parseInt(version.split("\\.")[0]);
        int versionMinor = Integer.parseInt(version.split("\\.")[1]);
        int versionMicro = Integer.parseInt(version.split("\\.")[2]);

        if(versionMinor >= 13) LEGACY = false;
    }

    public static boolean isLegacy() {
        return LEGACY;
    }

    public static Material matchMaterial(String mat) {
        return LEGACY ? Material.matchMaterial(mat) : Material.matchMaterial(mat, LEGACY);
    }

}
