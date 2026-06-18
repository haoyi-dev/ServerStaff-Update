package camchua.serverstaff.gui;

import camchua.serverstaff.FileManager;
import camchua.serverstaff.Main;
import camchua.serverstaff.StaffData;
import camchua.serverstaff.Utils;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VoteGui implements Listener {

    public static void open(Player p) {
        FileConfiguration gui = FileManager.getFileConfig(FileManager.Files.GUI);
        FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);

        Inventory inv = Bukkit.createInventory(null, gui.getInt("VoteGui.Rows") * 9, gui.getString("VoteGui.Title").replace("&", "§"));
        Calendar c = Calendar.getInstance();
        int month = c.getTime().getMonth() + 1;

        ItemStack blank = new ItemStack(Utils.matchMaterial(gui.getString("VoteGui.Content.blank.ID")), 1, (byte) gui.getInt("VoteGui.Content.blank.Data"));
        ItemMeta mblank = blank.getItemMeta();
        mblank.setDisplayName(" ");
        blank.setItemMeta(mblank);

        for(int s : gui.getIntegerList("VoteGui.Content.blank.Slots")) {
            inv.setItem(s, blank);
        }

        if(data.contains("StaffList")) {
            List<String> stafflist = data.getStringList("StaffList");
            for(String staff : stafflist) {
                if(p.getName().equals(staff)) {
                    continue;
                }
                if(data.getStringList("Data." + p.getName() + ".Player.Vote." + month).contains(staff)) {
                    continue;
                }

                ItemStack sff = new ItemStack(Utils.matchMaterial(gui.getString("VoteGui.Format.StaffIcon.ID")), 1, (byte) gui.getInt("VoteGui.Format.StaffIcon.Data"));
                SkullMeta msff = (SkullMeta) sff.getItemMeta();
                msff.setDisplayName(gui.getString("VoteGui.Format.StaffIcon.Name").replace("&", "§").replace("<name>", staff));
                List<String> lores = new ArrayList<>();
                for(String lore : gui.getStringList("VoteGui.Format.StaffIcon.Lore")) {
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

    public VoteGui(Main main) {
        this.main = main;
    }


    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if(!viewers.contains(p)) return;

        FileConfiguration messages = FileManager.getFileConfig(FileManager.Files.MESSAGES);
        FileConfiguration gui = FileManager.getFileConfig(FileManager.Files.GUI);
        FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);

        e.setCancelled(true);
        if(e.getCurrentItem().getType() == Utils.matchMaterial(gui.getString("VoteGui.Format.StaffIcon.ID"))) {
            Calendar c = Calendar.getInstance();
            int month = c.getTime().getMonth() + 1;

            String replacePattern = gui.getString("VoteGui.Format.StaffIcon.Name").replace("&", "§").replace("<name>", "");
            String name = e.getCurrentItem().getItemMeta().getDisplayName().replace(replacePattern, "");
            p.closeInventory();
            StaffData staff = StaffData.data.get(name);
            staff.setVote(staff.getVote() + 1);
            StaffData.data.replace(name, staff);

            List<String> vote = data.getStringList("Data." + p.getName() + ".Player.Vote." + month);
            vote.add(name);
            data.set("Data." + p.getName() + ".Player.Vote." + month, vote);
            FileManager.saveFileConfig(data, FileManager.Files.DATA);

            open(p);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("Voted")).replace("<name>", name));
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if(viewers.contains(p)) viewers.remove(p);
    }

}
