package camchua.serverstaff;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import camchua.serverstaff.compat.SchedulerAdapter;
import camchua.serverstaff.gui.ReportGui;
import camchua.serverstaff.gui.StartGui;
import camchua.serverstaff.gui.VoteGui;
import camchua.serverstaff.papi.ServerStaffExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin implements Listener {
	
	public List<String> active_staff;
	
	private static boolean premium = false;
	
	private static boolean run = false;
	
	private static String color(String str) {
		return ChatColor.translateAlternateColorCodes('&', str);
	}
	
	private List<Player> staff_chat;
	
	
	@Override
	public void onEnable() {
		disableWarnASW();
		Utils.checkVersion();

		rp = new HashMap<String, String>();
		staff_chat = new ArrayList<>();
		active_staff = new ArrayList<String>();
		FileManager.setup(this);
		
		in();
		loadCache();

		Bukkit.getConsoleSender().sendMessage("§e[ServerStaff] Thông Tin Plugin");
		Bukkit.getConsoleSender().sendMessage("§f| Tên: §6ServerStaff");
		Bukkit.getConsoleSender().sendMessage("§f| Tác giả: §aCamChua_VN");
		Bukkit.getConsoleSender().sendMessage("§f| Phiên bản plugin: §a1.3-modern");
		Bukkit.getConsoleSender().sendMessage("§f| Java yêu cầu: §a21");
		Bukkit.getConsoleSender().sendMessage("§f| Phiên bản Minecraft: §a1.20+");
		Bukkit.getConsoleSender().sendMessage("§f| Nền tảng hỗ trợ: §aBukkit, Spigot, PaperMC, Purpur, Folia");
		Bukkit.getConsoleSender().sendMessage("§f| Runtime hiện tại: §a" + (SchedulerAdapter.isFolia() ? "Folia" : "Bukkit/Paper Scheduler"));

		premium = true;
		run = true;
		n.add("startup-ok");
		Bukkit.getConsoleSender().sendMessage("§e[ServerStaff] §aĐã bật plugin (không yêu cầu license)");
		
		if(run) {
			if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
				new ServerStaffExpansion(this).register();
				Bukkit.getConsoleSender().sendMessage("§e[ServerStaff] §aĐã tích hợp PlaceholderAPI");
			}
			Bukkit.getPluginManager().registerEvents(this, this);
			Bukkit.getPluginManager().registerEvents(new ReportGui(this), this);
			Bukkit.getPluginManager().registerEvents(new StartGui(this), this);
			Bukkit.getPluginManager().registerEvents(new VoteGui(this), this);
			
			SchedulerAdapter.runTaskTimer(this, new Runnable() {
				public void run() {
					FileConfiguration config = FileManager.getFileConfig(FileManager.Files.CONFIG);
					FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);
					FileConfiguration messages = FileManager.getFileConfig(FileManager.Files.MESSAGES);
					FileConfiguration staffonline = FileManager.getFileConfig(FileManager.Files.STAFFONLINE);

					a : for(String staff : data.getStringList("StaffList")) {
						if(active_staff.contains(staff)) {
							StaffData sd = StaffData.data.get(staff);
							if(sd == null) {
								new StaffData(staff, 0, 0, 0);
								continue a;
							}
							sd.setOnlineTime(sd.getOnlineTime() + 1);
							StaffData.data.replace(staff, sd);
							if(sd.getOnlineTime() == config.getInt("Settings.RequireOnline")) {
								if(Bukkit.getOfflinePlayer(staff).isOnline()) {
									Bukkit.getPlayer(staff).sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("TimeReached")));
								}
							}
						}
					}
					
					Calendar c = Calendar.getInstance();
					int hour = c.getTime().getHours();
					int minute = c.getTime().getMinutes();
					int second = c.getTime().getSeconds();
					
					if(hour == 23 && minute == 59 && second == 59) {
						int date = c.getTime().getDate();
						int month = c.getTime().getMonth() + 1;
						int year = c.getTime().getYear() + 1900;
						String key = date + "-" + month + "-" + year;
						List<String> rep = new ArrayList<>();
						
						FileConfiguration save = getSave(month, year);
						
						for(String staff : data.getStringList("StaffList")) {
							StaffData sd = StaffData.data.get(staff);
							if(sd == null) {
								new StaffData(staff, 0, 0, 0);
								sd = StaffData.data.get(staff);
							}
							String status = "";
							if(requireReached(staff)) {
								status = "Complete";
							} else {
								status = "Incomplete";
							}
							String re = staff + " | Online " +  (sd.getOnlineTime() / 3600) + " Hours | Status: " + status;
							rep.add(re);
							
							if(date == c.getActualMaximum(Calendar.DAY_OF_MONTH)) {
								int vote = sd.getVote();
								int report = sd.getReport();

								save.set(staff + ".Vote", vote);
								save.set(staff + ".Report", report);
								
								sd.setVote(0);
								sd.setReport(0);
							}
							
							sd.setOnlineTime(0);
							StaffData.data.replace(staff, sd);
						}
						
						staffonline.set(key, rep);
						FileManager.saveFileConfig(staffonline, FileManager.Files.STAFFONLINE);

						active_staff.clear();
					}
					
					b : for(Player p : Bukkit.getOnlinePlayers()) {
						if(StartGui.viewers.contains(p)) {
							if(!data.getStringList("StaffList").contains(p.getName())) {
								p.closeInventory();
								continue b;
							}
							StartGui.open(p);
						}
					}
				}
			}, 20, 20);

			SchedulerAdapter.runTaskTimer(this, this::checkClearLog, 20, 1800 * 20);
		}
		
		
		o = run;
		if(n.isEmpty() && "a".equals("a") && Integer.parseInt("1") == 1 && !o) n = null;
		if(n == null) {
			PluginManager pm = Bukkit.getServer().getPluginManager();
			pm.disablePlugin(this);
		}
		
	}
	
	private boolean o = false; private List<String> n = new ArrayList<String>();
	
	@Override
	public void onDisable() {
		
		try {
			Bukkit.getConsoleSender().sendMessage("§e[ServerStaff] §cThis plugin has been disabled");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		Calendar c = Calendar.getInstance();
		int date = c.getTime().getDate();
		int month = c.getTime().getMonth() + 1;
		int year = c.getTime().getYear() + 1900;
		
		String key = date + "-" + month + "-" + year;

		FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);
		data.set("LastDate", key);
		FileManager.saveFileConfig(data, FileManager.Files.DATA);

		saveCache();
	}


	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!o) {
			return true;
		}

		FileConfiguration messages = FileManager.getFileConfig(FileManager.Files.MESSAGES);
		FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);
		FileConfiguration config = FileManager.getFileConfig(FileManager.Files.CONFIG);

		if(args.length == 0) {
			if(!sender.hasPermission("serverstaff.help")) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NoPermission")));
				return true;
			}
			for(String help : messages.getStringList("Help")) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', help));
			}
			return true;
		}
		if(args.length >= 1) {
			if(args[0].equalsIgnoreCase("start")) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NotPlayer")));
					return true;
				}
				if(!sender.hasPermission("serverstaff.start")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NoPermission")));
					return true;
				}
				Player p = (Player) sender;
				if(!data.getStringList("StaffList").contains(p.getName())) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NotStaff")));
					return true;
				}
				if(requireReached(p.getName())) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("RequireReached")));
					return true;
				}
				StartGui.open(p);
				return true;
			} else if(args[0].equalsIgnoreCase("vote")) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NotPlayer")));
					return true;
				}
				if(!sender.hasPermission("serverstaff.vote")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NoPermission")));
					return true;
				}
				Player p = (Player) sender;
				VoteGui.open(p);
				return true;
			} else if(args[0].equalsIgnoreCase("report")) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NotPlayer")));
					return true;
				}
				if(!sender.hasPermission("serverstaff.report")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NoPermission")));
					return true;
				}
				Player p = (Player) sender;
				long lastReport = data.getLong("LastReport." + p.getName(), 0);
				if(lastReport != 0) {
					long currentTime = System.currentTimeMillis();
					long diff = (currentTime - lastReport) / 1000;
					int countdown = config.getInt("Settings.ReportCountdown", 3) * 86400;
					if(diff < countdown) {
						long nextReport = lastReport + (countdown * 1000);
						Date nextReportDate = new Date(nextReport);
						String time = sdf.format(nextReportDate);
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("ReportCountdown")).replace("<time>", time));
						return true;
					}
				}

				ReportGui.open(p);
				return true;
			} else if(args[0].equalsIgnoreCase("reload")) {
				if(!sender.hasPermission("serverstaff.admin")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NoPermission")));
					return true;
				}
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a&lSStaff&8] &fĐang tải lại dữ liệu plugin..."));
				try {
					FileManager.setup(this);
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a&lSStaff&8] &aĐã tải lại thành công."));
				} catch(Exception ex) {
					ex.printStackTrace();
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a&lSStaff&8] &cTải lại thất bại. Vui lòng xem console."));
				}
				return true;
			} else if(args[0].equalsIgnoreCase("help")) {
				if(!sender.hasPermission("serverstaff.help")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NoPermission")));
					return true;
				}
				for(String help : messages.getStringList("Help")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', help));
				}
				return true;
			} else if(args[0].equalsIgnoreCase("addstaff")) {
				if(!sender.hasPermission("serverstaff.admin")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NoPermission")));
					return true;
				}
				if(args.length == 1) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a&lSStaff&8] &fDùng: &a/serverstaff addstaff <tên> &7- Bổ nhiệm staff"));
					return true;
				}
				String name = args[1];
				List<String> staff = data.getStringList("StaffList");
				if(staff.contains(name)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("StaffExists")));
					return true;
				}
				staff.add(name);
				data.set("StaffList", staff);
				FileManager.saveFileConfig(data, FileManager.Files.DATA);

				StaffData sd = StaffData.data.get(name);
				if(sd == null) {
					new StaffData(name, 0, 0, 0);
				}
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("StaffAdded")).replace("<name>", name));
				return true;
			} else if(args[0].equalsIgnoreCase("removestaff")) {
				if(!sender.hasPermission("serverstaff.admin")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NoPermission")));
					return true;
				}
				if(args.length == 1) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a&lSStaff&8] &fDùng: &a/serverstaff removestaff <tên> &7- Thu hồi staff"));
					return true;
				}
				String name = args[1];
				List<String> staff = data.getStringList("StaffList");
				if(!staff.contains(name)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("StaffNotExists")));
					return true;
				}
				staff.remove(name);
				data.set("StaffList", staff);
				FileManager.saveFileConfig(data, FileManager.Files.DATA);

				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("StaffRemoved")).replace("<name>", name));
				return true;
			} else if(args[0].equalsIgnoreCase("info")) {
				if(!sender.hasPermission("serverstaff.admin")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NoPermission")));
					return true;
				}
				if(args.length == 1) {
					return true;
				}
				String name = args[1];
				StaffData staffData = StaffData.data.get(name);
				if(data == null) {
					return true;
				}
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a&lSStaff&8] &fTên nhân sự: &e" + name));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a&lSStaff&8] &fThời gian online: &a" + staffData.getOnlineTime()));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a&lSStaff&8] &fLượt vote: &b" + staffData.getVote()));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a&lSStaff&8] &fLượt report: &c" + staffData.getReport()));
				return true;
			} else if(args[0].equalsIgnoreCase("status")) {
				if(!sender.hasPermission("serverstaff.status")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NoPermission")));
					return true;
				}
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a&lSStaff&8] &fHỗ trợ Folia: &a" + (SchedulerAdapter.isFolia() ? "BẬT" : "TẮT")));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a&lSStaff&8] &fStaff đang trực: &e" + active_staff.size()));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a&lSStaff&8] &fTổng staff đăng ký: &e" + data.getStringList("StaffList").size()));
				return true;
			} else if(args[0].equalsIgnoreCase("top")) {
				if(!sender.hasPermission("serverstaff.top")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NoPermission")));
					return true;
				}
				List<StaffData> list = new ArrayList<>(StaffData.data.values());
				list.sort((a, b) -> Integer.compare(b.getOnlineTime(), a.getOnlineTime()));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a&lSStaff&8] &fTop staff theo thời gian online:"));
				for (int i = 0; i < Math.min(5, list.size()); i++) {
					StaffData sd = list.get(i);
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7#" + (i + 1) + " &e" + sd.getName() + " &f- &a" + time(sd.getOnlineTime()) + " &7(giờ trực)"));
				}
				return true;
			} else if(args[0].equalsIgnoreCase("checkin")) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NotPlayer")));
					return true;
				}
				if(!sender.hasPermission("serverstaff.start")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NoPermission")));
					return true;
				}
				Player p = (Player) sender;
				if(!data.getStringList("StaffList").contains(p.getName())) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NotStaff")));
					return true;
				}
				if(!active_staff.contains(p.getName())) active_staff.add(p.getName());
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&a&lSStaff&8] &aCheck-in thành công."));
				return true;
			} else if(args[0].equalsIgnoreCase("chat")) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NotPlayer")));
					return true;
				}
				if(!sender.hasPermission("serverstaff.chat")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NoPermission")));
					return true;
				}
				
				Player p = (Player) sender;
				if(!data.getStringList("StaffList").contains(p.getName())) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NotStaff")));
					return true;
				}
				
				if(staff_chat.contains(p)) {
					staff_chat.remove(p);
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("StaffChatOff")));
					return true;
				} else {
					staff_chat.add(p);
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("StaffChatOn")));
					return true;
				}
			} else {
				if(!sender.hasPermission("serverstaff.help")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("NoPermission")));
					return true;
				}
				for(String help : messages.getStringList("Help")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', help));
				}
				return true;
			}
		}
		return false;
	}
	
	public void in() {
		FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);

		if(!data.contains("StaffList")) {
			data.set("StaffList", new ArrayList<String>());
		}
		if(!data.contains("LastDate")) {
			Calendar c = Calendar.getInstance();
			int date = c.getTime().getDate();
			int month = c.getTime().getMonth() + 1;
			int year = c.getTime().getYear() + 1900;
			
			String key = date + "-" + month + "-" + year;
			
			data.set("LastDate", key);
		}

		FileManager.saveFileConfig(data, FileManager.Files.DATA);
	}
	
	public void loadCache() {
		FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);

		if(!data.contains("Data")) {
			return;
		}
		for(String name : data.getConfigurationSection("Data").getKeys(false)) {
			int onlinetime = data.getInt("Data." + name + ".Staff.OnlineTime");
			
			String lastdate = data.getString("LastDate");
			int date = Integer.parseInt(lastdate.split("-")[0]);
			int month = Integer.parseInt(lastdate.split("-")[1]);
			int year = Integer.parseInt(lastdate.split("-")[2]);
			
			Calendar c = Calendar.getInstance();
			if((c.getTime().getDate() == date) && ((c.getTime().getMonth() + 1) == month) && ((c.getTime().getYear() + 1900) == year)) {
				
			} else {
				onlinetime = 0;
			}
			
			int vote = data.getInt("Data." + name + ".Staff.Vote");
			int report = data.getInt("Data." + name + ".Staff.Report");
			new StaffData(name, onlinetime, vote, report);
		}
	}
	
	public void saveCache() {
		FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);

		for(Entry<String, StaffData> d : StaffData.data.entrySet()) {
			String name = d.getKey();
			data.set("Data." + name + ".Staff.OnlineTime", d.getValue().getOnlineTime());
			data.set("Data." + name + ".Staff.Vote", d.getValue().getVote());
			data.set("Data." + name + ".Staff.Report", d.getValue().getReport());
		}

		FileManager.saveFileConfig(data, FileManager.Files.DATA);
	}
	
	public HashMap<String, String> rp;
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);
		FileConfiguration report = FileManager.getFileConfig(FileManager.Files.REPORT);
		FileConfiguration messages = FileManager.getFileConfig(FileManager.Files.MESSAGES);

		if(rp.containsKey(e.getPlayer().getName())) {
			e.setCancelled(true);
			Player p = e.getPlayer();
			String mess = e.getMessage();
			if(mess.startsWith(" ")) mess.replaceFirst(" ", "");
			if(mess.toUpperCase().contains("HUY")) {
				rp.remove(p.getName());
				ReportGui.open(p);
				return;
			}
			String name = rp.get(p.getName());
			
			StaffData staff = StaffData.data.get(name);
			staff.setReport(staff.getReport() + 1);
			StaffData.data.replace(name, staff);
			
			List<String> reportList = data.getStringList("Data." + p.getName() + ".Player.Report");
			reportList.add(name);
			data.set("Data." + p.getName() + ".Player.Report", reportList);
			data.set("LastReport." + p.getName(), System.currentTimeMillis());
			FileManager.saveFileConfig(data, FileManager.Files.DATA);
			
			String key = generateString(15, new Random());
			
			Calendar c = Calendar.getInstance();
			int hour = c.getTime().getHours();
			int minute = c.getTime().getMinutes();
			int second = c.getTime().getSeconds();
			
			int date = c.getTime().getDate();
			int month = c.getTime().getMonth() + 1;
			int year = c.getTime().getYear() + 1900;
			
			String time = hour + ":" + minute + ":" + second + " " + date + "-" + month + "-" + year;
			
			report.set(key + ".Time", time);
			report.set(key + ".Reporter", p.getName());
			report.set(key + ".Report", name);
			report.set(key + ".Reason", mess);
			FileManager.saveFileConfig(report, FileManager.Files.REPORT);
			
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("Reported")).replace("<name>", name).replace("<reason>", mess));
			rp.remove(p.getName());
			return;
		}
		
		if(data.getStringList("StaffList").contains(e.getPlayer().getName())) {
			if(staff_chat.contains(e.getPlayer())) {
				e.setCancelled(true);
				for(String staff : data.getStringList("StaffList")) {
					if(!Bukkit.getOfflinePlayer(staff).isOnline()) continue;
					
					Bukkit.getPlayer(staff).sendMessage(messages.getString("StaffChatFormat").replace("&", "§").replace("<player>", e.getPlayer().getName()).replace("<message>", e.getMessage()));
				}
			}
		}
	}
	
	public String generateString(int length, Random r) {
    	if(length < 1) return "";
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = upper.toLowerCase(Locale.ROOT);
        String digits = "0123456789";
        String alphanum = upper + lower + digits;
        Random random = Objects.requireNonNull(r);
        char[] symbols = alphanum.toCharArray();
        char[] buf = new char[length];
        for (int idx = 0; idx < buf.length; ++idx) buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }
	
	public boolean requireReached(String name) {
		FileConfiguration config = FileManager.getFileConfig(FileManager.Files.CONFIG);

		StaffData staff = StaffData.data.get(name);
		if(staff == null) {
			new StaffData(name, 0, 0, 0);
			staff = StaffData.data.get(name);
		}
		if(staff.getOnlineTime() >= config.getInt("Settings.RequireOnline")) {
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if(active_staff.contains(p.getName())) {
			active_staff.remove(p.getName());
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		FileConfiguration config = FileManager.getFileConfig(FileManager.Files.CONFIG);
		boolean autoCheckIn = config.getBoolean("Settings.AutoCheckIn", false);
		if(autoCheckIn) {
			Player p = e.getPlayer();
			FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);
			if(data.getStringList("StaffList").contains(p.getName())) {
				if(!active_staff.contains(p.getName())) active_staff.add(p.getName());
			}
		}
	}


	@EventHandler
	public void commandAliases(PlayerCommandPreprocessEvent e) {
		String cmd = e.getMessage().replaceFirst("/", "");
		StringBuilder sb = new StringBuilder();
		for(int i = 1; i < cmd.split(" ").length; i++) {
			sb.append(cmd.split(" ")[i]).append(" ");
		}
		for(String c : FileManager.getFileConfig(FileManager.Files.CONFIG).getStringList("Settings.CommandAliases")) {
			if(cmd.split(" ")[0].equalsIgnoreCase(c)) {
				e.setMessage("/serverstaff " + sb);
				break;
			}
		}
	}


	private SimpleDateFormat logDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat logDateFormatHour = new SimpleDateFormat("HH:mm:ss");

	@EventHandler
	public void logStaffChat(AsyncPlayerChatEvent e) {
		FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);

		Player p = e.getPlayer();
		if(!data.getStringList("StaffList").contains(p.getName())) return;
		Date time = Calendar.getInstance().getTime();
		String fileName = p.getName() + " " + logDateFormat.format(time);
		String save = "[" + logDateFormatHour.format(time) + "] Chat: " + ChatColor.stripColor(e.getMessage());
		writeLog(fileName, save);
	}

	@EventHandler
	public void logStaffCommand(PlayerCommandPreprocessEvent e) {
		FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);

		Player p = e.getPlayer();
		if(!data.getStringList("StaffList").contains(p.getName())) return;
		Date time = Calendar.getInstance().getTime();
		String fileName = p.getName() + " " + logDateFormat.format(time);
		String save = "[" + logDateFormatHour.format(time) + "] Command: " + ChatColor.stripColor(e.getMessage());
		writeLog(fileName, save);
	}

	
	public String time(long second) {
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
	
	private File getFileSave(int month, int year) {
		File f = new File(getDataFolder(), "//save//" + month + "-" + year + ".yml");
		if(!f.exists()) {
			f.getParentFile().mkdirs();
			try {
				f.createNewFile();
			} catch(Exception ex) {
				
			}
		}
		return f;
	}
	
	private FileConfiguration getSave(int month, int year) {
		File f = getFileSave(month, year);
		FileConfiguration fc = new YamlConfiguration();
		try {
			fc.load(f);
		} catch(Exception ex) {
			
		}
		return fc;
	}


	void writeLog(String fileName, String msg) {
		try {
			File file = new File(this.getDataFolder() + File.separator + "logs", fileName + ".txt");
			if(!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			Files.write(Paths.get(file.getPath()), (msg + "\n").getBytes(), StandardOpenOption.APPEND);
		} catch(Exception ex) {}
	}

	void checkClearLog() {
		FileConfiguration config = FileManager.getFileConfig(FileManager.Files.CONFIG);

		int logsSave = config.getInt("Settings.Logs-Save", 7);
		if(logsSave <= 0) return;

		File file = new File(this.getDataFolder() + File.separator + "logs");
		if(!file.exists()) {
			file.mkdirs();
			return;
		}

		long currentTime = System.currentTimeMillis();
		File[] listFile = file.listFiles();
		for(int i = 0; i < listFile.length; i++) {
			File f = listFile[i];
			if(!f.getName().endsWith(".txt")) continue;
			try {
				BasicFileAttributes attr = Files.readAttributes(Paths.get(f.getPath()), BasicFileAttributes.class);
				long creationTime = attr.creationTime().toMillis();
				long diff = (currentTime - creationTime) / 1000;
				if(diff > (logsSave * 86400)) f.delete();
			} catch(Exception ex) {}
		}
	}


	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		String cmd = e.getMessage().replaceFirst("/", "");
		StringBuilder sb = new StringBuilder();
		for(int i = 1; i < cmd.split(" ").length; i++) {
			sb.append(cmd.split(" ")[i]).append(" ");
		}
		for(String c : FileManager.getFileConfig(FileManager.Files.CONFIG).getStringList("Settings.CommandAliases")) {
			if(cmd.split(" ")[0].toLowerCase().equals(c.toLowerCase())) {
				e.setMessage("/serverstaff " + sb.toString());
				break;
			}
		}
	}

	
	public void disableWarnASW() {
		File aswf = new File(getDataFolder().getParentFile(), "\\AutoSaveWorld\\config.yml");
		if(aswf.exists()) {
			FileConfiguration asw = new YamlConfiguration();
			try {
				asw.load(aswf);
			} catch(Exception ex) {
				
			}
			if(asw.getBoolean("networkwatcher.mainthreadnetaccess.warn")) {
				asw.set("networkwatcher.mainthreadnetaccess.warn", false);
				try {
					asw.save(aswf);
				} catch(Exception ex) {
					
				}
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "asw reload");
			}
		}
	}

}
