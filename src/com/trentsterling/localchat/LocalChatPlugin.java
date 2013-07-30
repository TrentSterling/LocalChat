package com.trentsterling.localchat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class LocalChatPlugin extends JavaPlugin implements Listener
{

	public void messageAllPlayers(String msg)
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			p.sendMessage(msg);
		}
	}

	public void messageLocal(Player from, String msg)
	{
		for (Player player : from.getWorld().getPlayers())
		{
			if (from.getLocation().distance(player.getLocation()) <= 100)
			{
				player.sendMessage(msg);
			}
		}
	}

	public void messageStaff(String msg)
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (p.isOp() || p.hasPermission("localchat.admin"))
			{
				p.sendMessage(msg);
			}
		}
	}

	@EventHandler
	public void onChatMessage(AsyncPlayerChatEvent e)
	{
		if (e.isCancelled())
		{
			return;
		}
		Player player = e.getPlayer();
		String chatmode = String.valueOf(player.getMetadata("ChatMode").get(0).asString());
		if (chatmode == "GLOBAL")
		{
			if (player.isOp() || player.hasPermission("localchat.global"))
			{
				messageAllPlayers("[G]<" + player.getDisplayName() + "> " + e.getMessage());
				getLogger().info("G: " + player.getName() + ": " + e.getMessage());
			}
			else
			{
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Global permissions needed, changing your chatmode!");

				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Chat mode is L(ocal)");
				player.setMetadata("ChatMode", new FixedMetadataValue(this, "LOCAL"));
			}
		}
		if (chatmode == "LOCAL")
		{
			messageLocal(player, "[Local]<" + player.getDisplayName() + "> " + ChatColor.GRAY + e.getMessage());
			getLogger().info("L: " + player.getName() + ": " + e.getMessage());
		}
		if (chatmode == "ADMIN")
		{
			if (player.isOp() || player.hasPermission("localchat.admin"))
			{
				messageStaff("[Admin]<" + player.getDisplayName() + "> " + ChatColor.GREEN + e.getMessage());
				getLogger().info("A: " + player.getName() + ": " + e.getMessage());
			}
			else
			{
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Admin permissions needed, changing your chatmode!");

				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Chat mode is L(ocal)");
				player.setMetadata("ChatMode", new FixedMetadataValue(this, "LOCAL"));
			}
		}
		e.setCancelled(true);

	}

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage("This command can only be run by a PLAYER.");
			return true;
		}

		Player player = (Player) sender;

		if (arg2.equalsIgnoreCase("L"))
		{
			player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Chat mode is L(ocal)");
			player.setMetadata("ChatMode", new FixedMetadataValue(this, "LOCAL"));
			return true;
		}

		if (arg2.equalsIgnoreCase("G"))
		{
			if (player.isOp() || player.hasPermission("localchat.global"))
			{
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Chat mode is G(lobal)");
				player.setMetadata("ChatMode", new FixedMetadataValue(this, "GLOBAL"));
				return true;
			}
			else
			{
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Global permissions needed");
			}
			return true;
		}

		if (arg2.equalsIgnoreCase("A"))
		{
			if (player.isOp() || player.hasPermission("localchat.admin"))
			{
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Chat mode is A(dmin)");
				player.setMetadata("ChatMode", new FixedMetadataValue(this, "ADMIN"));
			}
			else
			{
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Admin permissions needed");
			}
			return true;
		}
		player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Use /G, /L, or /A to set your chat mode!");
		return true;
	}

	@Override
	public void onEnable()
	{
		super.onEnable();
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Nachos Local Chat Plugin Loaded");
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		player.setMetadata("ChatMode", new FixedMetadataValue(this, "LOCAL"));
		player.sendMessage("CHATMODE = LOCAL");
	}
}
