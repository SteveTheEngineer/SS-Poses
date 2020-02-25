package me.ste.stevesseries.poses;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ste.stevesseries.coreposelib.Pose;
import ru.ste.stevesseries.coreposelib.PoseLibApi;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class Poses extends JavaPlugin {
    private PoseLibApi poseLibApi;

    @Override
    public void onEnable() {
        poseLibApi = Objects.requireNonNull(getServer().getServicesManager().getRegistration(PoseLibApi.class)).getProvider();
        File CONFIG_FILE = new File(getDataFolder(), "config.yml");
        if (!CONFIG_FILE.exists()) {
            saveDefaultConfig();
        }
        reloadConfig();
    }

    public static String replaceSmart(String text, String from, String to) {
        String r;
        if (!from.equals(from.toLowerCase()) && !from.equals(from.toUpperCase())) {
            r = text.replaceAll(from, to);
            r = r.replaceAll(from.toLowerCase(), to.toLowerCase());
        } else {
            r = text.replaceAll(from.toLowerCase(), to.toLowerCase());
        }
        r = r.replaceAll(from.toUpperCase(), to.toUpperCase());
        return r;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("posesreload")) {
            if(sender.hasPermission("stevesseries.poses.reload")) {
                reloadConfig();
                String message = getConfig().getString("messages.pluginReloaded");
                message = replaceSmart(message, "%%Sender%%", sender.getName());
                message = ChatColor.translateAlternateColorCodes('&', message);
                sender.sendMessage(message);
            } else {
                String message = getConfig().getString("messages.noPermissionReload");
                message = replaceSmart(message, "%%Sender%%", sender.getName());
                message = ChatColor.translateAlternateColorCodes('&', message);
                sender.sendMessage(message);
            }
            return true;
        } else if(command.getName().equals("pose")) {
            if(sender instanceof Player) {
                if(sender.hasPermission("stevesseries.poses.pose")) {
                    Set<String> poses = Objects.requireNonNull(getConfig().getConfigurationSection("poses")).getKeys(false);
                    Set<String> availablePoses = new LinkedHashSet<>();
                    poses.forEach(pose -> {
                        if(sender.hasPermission("stevesseries.poses.pose." + pose)) {
                            availablePoses.add(pose);
                        }
                    });
                    if(args.length == 1) {
                        if(poses.contains(args[0])) {
                            if(availablePoses.contains(args[0])) {
                                Player player = (Player) sender;
                                if(!poseLibApi.getPose(player).isPresent()) {
                                    try {
                                        poseLibApi.setPose(player, Class.forName(getConfig().getString("poses." + args[0] + ".poseClass")).asSubclass(Pose.class), 0, 1);
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                                String message = getConfig().getString("poses." + args[0] + ".changedMessage");
                                message = replaceSmart(message, "%%Player%%", sender.getName());
                                message = ChatColor.translateAlternateColorCodes('&', message);
                                sender.sendMessage(message);
                            } else {
                                String message = getConfig().getString("poses." + args[0] + ".noPermissionMessage");
                                message = replaceSmart(message, "%%Player%%", sender.getName());
                                message = ChatColor.translateAlternateColorCodes('&', message);
                                sender.sendMessage(message);
                            }
                        } else {
                            String message = getConfig().getString("messages.unknownPose");
                            message = replaceSmart(message, "%%Sender%%", sender.getName());
                            message = replaceSmart(message, "%%Pose%%", args[0]);
                            message = ChatColor.translateAlternateColorCodes('&', message);
                            sender.sendMessage(message);
                        }
                    } else {
                        if(!availablePoses.isEmpty()) {
                            String message = getConfig().getString("messages.poseList");
                            message = replaceSmart(message, "%%Sender%%", sender.getName());
                            message = replaceSmart(message, "%%Available%%", String.join(", ", availablePoses));
                            message = ChatColor.translateAlternateColorCodes('&', message);
                            sender.sendMessage(message);
                        } else {
                            String message = getConfig().getString("messages.poseListEmpty");
                            message = replaceSmart(message, "%%Sender%%", sender.getName());
                            message = ChatColor.translateAlternateColorCodes('&', message);
                            sender.sendMessage(message);
                        }
                    }
                }
            } else {
                String message = getConfig().getString("messages.playersOnly");
                message = replaceSmart(message, "%%Sender%%", sender.getName());
                message = ChatColor.translateAlternateColorCodes('&', message);
                sender.sendMessage(message);
            }
        }
        return false;
    }
}