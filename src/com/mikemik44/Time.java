package com.mikemik44;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Time extends JavaPlugin implements Listener {
	HashMap<String, WorldData> worldData = new HashMap<>();

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		BukkitRunnable r = new BukkitRunnable() {
			private boolean day = true;

			public void run() {

				for (World w : Bukkit.getWorlds()) {
					if (!worldData.containsKey(w.getName())) {
						WorldData wd = new WorldData();
						wd.timeForDayToEnd = 60 * 10;
						wd.timeForNightToEnd = 60 * 10;
						wd.changer = -1;
						worldData.put(w.getName(), wd);
					}
					WorldData wd = worldData.get(w.getName());

					double dayCalc = 12000.0 / (20 * wd.timeForDayToEnd);
					double nightCalc = 12000.0 / (20 * wd.timeForNightToEnd);
					double time = wd.timer;
					if (wd.changer != -1) {
						long l = wd.changer;
						wd.changer = -1;
						if (l > 12000) {
							day = false;
							l -= 12000;
							time = l / nightCalc;
						} else {
							day = true;
							time = l / dayCalc;
						}
					}
					if (day && time > 20 * wd.timeForDayToEnd) {
						time = 0;
						day = false;
					}
					if (!day && time > 20 * wd.timeForNightToEnd) {
						time = 0;
						day = true;
					}
					if (day) {
						w.setTime((long) (time * dayCalc));
					} else {
						w.setTime((long) (time * nightCalc) + 12000);
					}
					time++;
					wd.timer = time;
				}
			}

		};
		r.runTaskTimer(this, 0, 1);
		getCommand("tsday").setTabCompleter(new TabCompleter() {

			@Override
			public List<String> onTabComplete(CommandSender p, Command c, String label, String[] args) {
				ArrayList<String> res = new ArrayList<>();
				if (args.length == 0) {
					res.add("set");
					res.add("modify");
					return res;
				} else if (args.length == 1) {
					if (args[0].equals("set") || args[0].equals("modify")) {
						if (args[0].equals("modify")) {
							res.add("modify day");
							res.add("modify night");
						}
					} else {
						if (match(args[0], "set")) {
							res.add("set");
						}

						if (match(args[0], "modify")) {
							res.add("modify");
						}
					}
				} else if (args.length == 2) {
					if (args[0].equals("modify")) {
						if (match(args[1], "day")) {
							res.add("day");
						}
						if (match(args[1], "night")) {
							res.add("night");
						}
					}
				} else if (args.length == 4) {
					if (args[0].equals("modify")) {
						if (match(args[1], "day") || match(args[1], "night")) {
							res.add("seconds");
							res.add("minutes");
							res.add("hours");
							res.add("days");
							res.add("millaseconds");
						}
					}
				}

				return res;
			}
		});
	}

	private boolean match(String s, String other) {

		for (int i = 0; i < s.length(); i++) {
			if (other.length() <= i) {
				return false;
			} else if (s.charAt(i) != other.charAt(i)) {
				return false;
			}
		}

		return true;
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		String[] data = e.getMessage().split(" ");
		Player p = e.getPlayer();
		if (data[0].equals("/tsday")) {
			if (data.length > 1) {
				if (data[1].equals("set")) {
					if (data.length > 2) {
						try {
							long num = Long.parseLong(data[2]);
							if (num < 0) {
								num = -num;
							}
							WorldData wd = worldData.get(p.getWorld().getName());
							wd.changer = num;
							p.sendMessage("§bWorld " + p.getWorld().getName() + "'s time set to " + num + "!");
						} catch (Exception ex) {
							p.sendMessage("§c" + data[2] + " is not a number that is of type int");
						}
					} else {
						p.sendMessage("§cyou need to provide a number to set the time of day");
					}
				} else if (data[1].equals("modify")) {
					if (data.length > 2) {
						if (data[2].equals("day")) {
							if (data.length > 3) {
								try {
									double num = Double.parseDouble(data[3]);
									if (num < 0) {
										num = -num;
									}
									if (data.length > 4) {
										if (data[3].equals("minutes") || data[3].equals("minute")) {
											num *= 60;
										} else if (data[3].equals("hours") || data[3].equals("hour")) {
											num *= 60 * 60;
										} else if (data[3].equals("days") || data[3].equals("day")) {
											num *= 60 * 60 * 24;
										} else if (data[3].equals("ticks") || data[3].equals("tick")) {
											num /= 20.0;
										} else if (data[3].equals("seconds") || data[3].equals("second")) {

										} else {
											p.sendMessage("§cUnknown time unit " + data[3]
													+ "! Please use seconds, minutes, hours, days, or ticks.");
										}
									}
									WorldData wd = worldData.get(p.getWorld().getName());
									wd.changer = p.getWorld().getTime();
									wd.timeForDayToEnd = num;

									p.sendMessage("§bWorld " + p.getWorld().getName()
											+ "'s day time is now set to be arround " + con(num));
								} catch (Exception ex) {
									p.sendMessage("§c" + data[3] + " is not a number");
								}
							} else {
								p.sendMessage("§c Please provide a number");
							}
						} else if (data[2].equals("night")) {
							if (data.length > 3) {
								try {
									double num = Double.parseDouble(data[3]);
									if (num < 0) {
										num = -num;
									}
									if (data.length > 4) {
										if (data[3].equals("minutes") || data[3].equals("minute")) {
											num *= 60;
										} else if (data[3].equals("hours") || data[3].equals("hour")) {
											num *= 60 * 60;
										} else if (data[3].equals("days") || data[3].equals("day")) {
											num *= 60 * 60 * 24;
										} else if (data[3].equals("ticks") || data[3].equals("tick")) {
											num /= 20.0;
										} else if (data[3].equals("seconds") || data[3].equals("second")) {

										} else {
											p.sendMessage("§cUnknown time unit " + data[3]
													+ "! Please use seconds, minutes, hours, days, or ticks.");
										}
									}
									WorldData wd = worldData.get(p.getWorld().getName());
									wd.changer = p.getWorld().getTime();
									wd.timeForNightToEnd = num;
									p.sendMessage("§bWorld " + p.getWorld().getName()
											+ "'s night time is now set to be arround " + con(num));
								} catch (Exception ex) {
									p.sendMessage("§c" + data[3] + " is not a number");
								}
							} else {
								p.sendMessage("§c Please provide a number");
							}
						} else {
							p.sendMessage("§cUnknown parameter " + data[2] + "! Correct params are day or night");
						}
					} else {
						p.sendMessage("§cyou need to put what timing your modifying day or night");
					}
				} else {
					p.sendMessage("§cUnknown parameter " + data[1] + "! correct parameters are \"set\" and \"modify\"");
				}
			} else {
				p.sendMessage("§cparameters are \"set\" and \"modify\"");
			}
			e.setCancelled(true);
		}
	}

	public static String con(double val) {
		float v = (float) (val - Math.floor(val));
		val -= v;
		v *= Math.pow(10, (v + "").length() - 2);
		int m = (int) (val / 60);
		int s = (int) (val - m * 60);
		int h = m / 60;
		m -= h * 60;
		int d = h / 24;
		h -= d * 24;
		String res = "";
		if (v != 0) {
			res = v + " millaSeconds";
		}
		if (s != 0) {
			if (!res.isEmpty()) {
				res = s + " seconds " + res;
			} else {
				res = s + " seconds";
			}
		}
		if (m != 0) {
			if (!res.isEmpty()) {
				res = m + " minutes " + res;
			} else {
				res = m + " minutes";
			}
		}
		if (h != 0) {
			if (!res.isEmpty()) {
				res = h + " hours " + res;
			} else {
				res = h + " hours";
			}
		}

		if (d != 0) {
			if (!res.isEmpty()) {
				res = d + " days " + res;
			} else {
				res = d + " days";
			}
		}
		return res;
	}

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		super.onDisable();
	}

}
