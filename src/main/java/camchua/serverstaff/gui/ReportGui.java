package camchua.serverstaff.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    private static final Set<UUID> viewers = new HashSet<>();

    private final Main main;

    public ReportGui(Main main) {
        this.main = main;
    }

    public static void open(Player p) {
        FileConfiguration gui = FileManager.getFileConfig(FileManager.Files.GUI);
        FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);

        Inventory inv = Bukkit.createInventory(
                null,
                gui.getInt("ReportGui.Rows") * 9,
                color(gui.getString("ReportGui.Title"))
        );

        ItemStack blank = new ItemStack(
                Utils.matchMaterial(gui.getString("ReportGui.Content.blank.ID")),
                1,
                (byte) gui.getInt("ReportGui.Content.blank.Data")
        );

        ItemMeta mblank = blank.getItemMeta();
        if (mblank != null) {
            mblank.setDisplayName(" ");
            blank.setItemMeta(mblank);
        }

        for (int s : gui.getIntegerList("ReportGui.Content.blank.Slots")) {
            inv.setItem(s, blank);
        }

        if (data.contains("StaffList")) {
            List<String> stafflist = data.getStringList("StaffList");

            for (String staff : stafflist) {
                if (p.getName().equals(staff)) {
                    continue;
                }

                if (data.getStringList("Data." + p.getName() + ".Player.Report").contains(staff)) {
                    continue;
                }

                ItemStack sff = new ItemStack(
                        Utils.matchMaterial(gui.getString("ReportGui.Format.StaffIcon.ID")),
                        1,
                        (byte) gui.getInt("ReportGui.Format.StaffIcon.Data")
                );

                ItemMeta meta = sff.getItemMeta();
                if (meta == null) {
                    continue;
                }

                meta.setDisplayName(color(gui.getString("ReportGui.Format.StaffIcon.Name")).replace("<name>", staff));

                List<String> lores = new ArrayList<>();
                for (String lore : gui.getStringList("ReportGui.Format.StaffIcon.Lore")) {
                    lores.add(color(lore));
                }
                meta.setLore(lores);

                if (meta instanceof SkullMeta skullMeta) {
                    skullMeta.setOwner(staff);
                }

                sff.setItemMeta(meta);
                inv.addItem(sff);
            }
        }

        p.openInventory(inv);
        viewers.add(p.getUniqueId());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) {
            return;
        }

        if (!viewers.contains(p.getUniqueId())) {
            return;
        }

        e.setCancelled(true);

        Inventory topInventory = e.getView().getTopInventory();
        if (e.getRawSlot() < 0 || e.getRawSlot() >= topInventory.getSize()) {
            return;
        }

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType().isAir()) {
            return;
        }

        FileConfiguration messages = FileManager.getFileConfig(FileManager.Files.MESSAGES);
        FileConfiguration gui = FileManager.getFileConfig(FileManager.Files.GUI);

        if (item.getType() != Utils.matchMaterial(gui.getString("ReportGui.Format.StaffIcon.ID"))) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        String replacePattern = color(gui.getString("ReportGui.Format.StaffIcon.Name")).replace("<name>", "");
        String name = meta.getDisplayName().replace(replacePattern, "");

        main.rp.put(p.getName(), name);

        p.sendMessage(color(messages.getString("Report")).replace("<name>", name));

        SchedulerAdapter.runEntityTaskLater(main, p, p::closeInventory, 10L);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player p) {
            viewers.remove(p.getUniqueId());
        }
    }

    private static String color(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}