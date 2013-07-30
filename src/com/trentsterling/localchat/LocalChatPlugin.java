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

	public void messageOps(String msg)
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (p.isOp())
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
		// messageOps("CHAT: <" + p.getDisplayName() + "> " + e.getMessage());

		// Are we global or local? Lets find out!

		String chatmode = String.valueOf(player.getMetadata("ChatMode").get(0).asString());

		if (chatmode == "GLOBAL")
		{
			messageAllPlayers("[G]<" + player.getDisplayName() + "> " + e.getMessage());
			getLogger().info("G: " + player.getName() + ": " + e.getMessage());
		}

		if (chatmode == "LOCAL")
		{
			messageLocal(player, "[Local]<" + player.getDisplayName() + "> " + e.getMessage());
			getLogger().info("L: " + player.getName() + ": " + e.getMessage());
		}

		if (chatmode == "ADMIN")
		{
			messageOps("[Admin]<" + player.getDisplayName() + "> " + ChatColor.GREEN + e.getMessage());
			getLogger().info("A: " + player.getName() + ": " + e.getMessage());
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
		if (arg3.length == 0)
		{
			player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Use /chat (G)lobal, (L)ocal, or (A)dmin to set your chat mode!");
		}
		else
		{
			String desiredMode = arg3[0];
			if (desiredMode.equalsIgnoreCase("G"))
			{
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Chat mode is G(lobal)");
				player.setMetadata("ChatMode", new FixedMetadataValue(this, "GLOBAL"));
			}
			else if (desiredMode.equalsIgnoreCase("L"))
			{
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Chat mode is L(ocal)");
				player.setMetadata("ChatMode", new FixedMetadataValue(this, "LOCAL"));
			}
			else if (desiredMode.equalsIgnoreCase("A"))
			{

				if (player.isOp())
				{
					player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Chat mode is A(dmin)");
					player.setMetadata("ChatMode", new FixedMetadataValue(this, "ADMIN"));
				}
				else
				{
					player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Sorry, OPs only, my friend.");
				}
			}
			else
			{
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Use /chat (G)lobal, (L)ocal, or (A)dmin to set your chat mode!");
			}
		}
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
