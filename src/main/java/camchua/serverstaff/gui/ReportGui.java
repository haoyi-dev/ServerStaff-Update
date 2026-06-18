package camchua.serverstaff.gui;

import java.util.ArrayList;
import java.util.Calendar;
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
import org.bukkit.inventory.meta.SkullMeta;

import camchua.serverstaff.FileManager;
import camchua.serverstaff.Main;
import camchua.serverstaff.Utils;
import camchua.serverstaff.compat.SchedulerAdapter;

public class ReportGui implements Listener {

    public static void open(Player p) {
        FileConfiguration gui = FileManager.getFileConfig(FileManager.Files.GUI);
        FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);

        Inventory inv = Bukkit.createInventory(null, gui.getInt("ReportGui.Rows") * 9, gui.getString("ReportGui.Title").replace("&", "§"));

        ItemStack blank = new ItemStack(Utils.matchMaterial(gui.getString("ReportGui.Content.blank.ID")), 1, (byte) gui.getInt("ReportGui.Content.blank.Data"));
        ItemMeta mblank = blank.getItemMeta();
        mblank.setDisplayName(" ");
        blank.setItemMeta(mblank);

        for(int s : gui.getIntegerList("ReportGui.Content.blank.Slots")) {
            inv.setItem(s, blank);
        }

        if(data.contains("StaffList")) {
            List<String> stafflist = data.getStringList("StaffList");
            for(String staff : stafflist) {
                if(p.getName().equals(staff)) {
                    continue;
                }
                if(data.getStringList("Data." + p.getName() + ".Player.Report").contains(staff)) {
                    continue;
                }

                ItemStack sff = new ItemStack(Utils.matchMaterial(gui.getString("ReportGui.Format.StaffIcon.ID")), 1, (byte) gui.getInt("ReportGui.Format.StaffIcon.Data"));
                SkullMeta msff = (SkullMeta) sff.getItemMeta();
                msff.setDisplayName(gui.getString("ReportGui.Format.StaffIcon.Name").replace("&", "§").replace("<name>", staff));
                List<String> lores = new ArrayList<>();
                for(String lore : gui.getStringList("ReportGui.Format.StaffIcon.Lore")) {
                    lores.add(lore.replace("&", "§"));
                }
                msff.setLore(lores);
                msff.setOwner(staff);
                sff.setItemMeta(msff);
                inv.addItem(sff);
            }
        }

        p.openInventory(inv);
        if(!viewers.contains(p)) viewers.add(p);
    }


    private static List<Player> viewers = new ArrayList<>();


    private Main main;

    public ReportGui(Main main) {
        this.main = main;
    }


    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if(!viewers.contains(p)) return;

        FileConfiguration messages = FileManager.getFileConfig(FileManager.Files.MESSAGES);
        FileConfiguration gui = FileManager.getFileConfig(FileManager.Files.GUI);

        e.setCancelled(true);
        if(e.getCurrentItem().getType() == Utils.matchMaterial(gui.getString("ReportGui.Format.StaffIcon.ID"))) {
            Calendar c = Calendar.getInstance();
            int month = c.getTime().getMonth() + 1;

            String replacePattern = gui.getString("ReportGui.Format.StaffIcon.Name").replace("&", "§").replace("<name>", "");
            String name = e.getCurrentItem().getItemMeta().getDisplayName().replace(replacePattern, "");

//            StaffData staff = StaffData.data.get(name);
//            staff.setReport(staff.getReport() + 1);
//            StaffData.data.replace(name, staff);
//
//            List<String> report = data.getStringList("Data." + p.getName() + ".Player.Report");
//            report.add(name);
//            data.set("Data." + p.getName() + ".Player.Report", report);
//            try {
//                data.save(dataf);
//            } catch(Exception ex) {
//
//            }

            main.rp.put(p.getName(), name);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("Report")).replace("<name>", name));
            SchedulerAdapter.runTaskLater(main, p::closeInventory, 10);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if(viewers.contains(p)) viewers.remove(p);
    }

}
