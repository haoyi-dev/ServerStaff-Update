package camchua.serverstaff.papi;

import org.bukkit.OfflinePlayer;

import camchua.serverstaff.Main;
import camchua.serverstaff.StaffData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class ServerStaffExpansion extends PlaceholderExpansion {

    private final Main plugin;

    public ServerStaffExpansion(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "serverstaff";
    }

    @Override
    public String getAuthor() {
        return "CamChua_VN, modernized";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("staff_online_count")) {
            return String.valueOf(plugin.active_staff.size());
        }

        if (player == null || player.getName() == null) {
            return "0";
        }

        StaffData data = StaffData.data.get(player.getName());
        if (data == null) {
            return "0";
        }

        if (params.equalsIgnoreCase("online_time")) {
            return String.valueOf(data.getOnlineTime());
        }

        if (params.equalsIgnoreCase("vote")) {
            return String.valueOf(data.getVote());
        }

        if (params.equalsIgnoreCase("report")) {
            return String.valueOf(data.getReport());
        }

        return null;
    }
}
