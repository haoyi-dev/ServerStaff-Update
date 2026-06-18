package camchua.serverstaff.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import camchua.serverstaff.FileManager;
import camchua.serverstaff.Main;
import camchua.serverstaff.StaffData;
import camchua.serverstaff.Utils;
import camchua.serverstaff.compat.SchedulerAdapter;

public class StartGui implements Listener {

    private static String time(long second) {
        long diff = second * 1000;
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
        String hou = diffHours + "";
        String min = diffMinutes + "";
        String sec = diffSeconds + "";
        if(diffHours < 10) {
            hou = "0" + diffHours;
        }
        if(diffMinutes < 10) {
            min = "0" + diffMinutes;
        }
        if(diffSeconds < 10) {
            sec = "0" + diffSeconds;
        }
        return hou + ":" + min + ":" + sec;
    }

    public static void open(Player p) {
        FileConfiguration config = FileManager.getFileConfig(FileManager.Files.CONFIG);
        FileConfiguration gui = FileManager.getFileConfig(FileManager.Files.GUI);

        StaffData data = StaffData.data.get(p.getName());
        Inventory inv = Bukkit.createInventory(null, gui.getInt("StartGui.Rows") * 9, gui.getString("StartGui.Title").replace("&", "§").replace("<online>", time(data.getOnlineTime())).replace("<req_online>", time(config.getInt("Settings.RequireOnline"))));

        ItemStack blank = new ItemStack(Utils.matchMaterial(gui.getString("StartGui.Content.blank.ID")), 1, (byte) gui.getInt("StartGui.Content.blank.Data"));
        ItemMeta mblank = blank.getItemMeta();
        mblank.setDisplayName(" ");
        blank.setItemMeta(mblank);

        ItemStack start = new ItemStack(Utils.matchMaterial(gui.getString("StartGui.Content.start.ID")), 1, (byte) gui.getInt("StartGui.Content.start.Data"));
        ItemMeta mstart = start.getItemMeta();
        mstart.setDisplayName(gui.getString("StartGui.Content.start.Name").replace("&", "§"));
        List<String> lores = new ArrayList<>();
        for(String lore : gui.getStringList("StartGui.Content.start.Lore")) {
            lores.add(lore.replace("&", "§"));
        }
        mstart.setLore(lores);
        start.setItemMeta(mstart);

        ItemStack stop = new ItemStack(Utils.matchMaterial(gui.getString("StartGui.Content.stop.ID")), 1, (byte) gui.getInt("StartGui.Content.stop.Data"));
        ItemMeta mstop = stop.getItemMeta();
        mstop.setDisplayName(gui.getString("StartGui.Content.stop.Name").replace("&", "§"));
        lores = new ArrayList<>();
        for(String lore : gui.getStringList("StartGui.Content.stop.Lore")) {
            lores.add(lore.replace("&", "§"));
        }
        mstop.setLore(lores);
        stop.setItemMeta(mstop);

        for(int s : gui.getIntegerList("StartGui.Content.blank.Slots")) {
            inv.setItem(s, blank);
        }

        for(int s : gui.getIntegerList("StartGui.Content.start.Slots")) {
            inv.setItem(s, start);
        }

        for(int s : gui.getIntegerList("StartGui.Content.stop.Slots")) {
            inv.setItem(s, stop);
        }

        p.openInventory(inv);
        if(!viewers.contains(p)) viewers.add(p);
    }

    public static List<Player> viewers = new ArrayList<>();


    private Main main;

    public StartGui(Main main) {
        this.main = main;
    }


    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if(!viewers.contains(p)) return;

        FileConfiguration messages = FileManager.getFileConfig(FileManager.Files.MESSAGES);
        FileConfiguration gui = FileManager.getFileConfig(FileManager.Files.GUI);

        e.setCancelled(true);
        if(gui.getIntegerList("StartGui.Content.start.Slots").contains(e.getSlot())) {
            if(!main.active_staff.contains(p.getName())) {
                main.active_staff.add(p.getName());
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("CountStart")));
                SchedulerAdapter.runTaskLater(main, p::closeInventory, 10);
            }
        }
        if(gui.getIntegerList("StartGui.Content.stop.Slots").contains(e.getSlot())) {
            if(main.active_staff.contains(p.getName())) {
                main.active_staff.remove(p.getName());
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("CountStop")));
                SchedulerAdapter.runTaskLater(main, p::closeInventory, 10);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if(viewers.contains(p)) viewers.remove(p);
    }

}
