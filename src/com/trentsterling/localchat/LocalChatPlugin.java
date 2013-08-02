package com.trentsterling.localchat;

import java.util.HashMap;
import java.util.List;

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
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class LocalChatPlugin extends JavaPlugin implements Listener
{

	String font = ChatColor.GOLD + "" + ChatColor.BOLD;

	/*
	 * UTILITIES - Metadata, Get players, check if console
	 * 
	 * FIXME: Move these utils to a seperate library.
	 */

	public Boolean isSenderPlayer(CommandSender sender)
	{
		if (sender instanceof Player)
		{
			return true;
		}
		return false;
	}

	public Boolean isSenderConsole(CommandSender sender)
	{
		if (sender instanceof Player)
		{
			return false;
		}
		return true;
	}

	public Boolean hasNeitherOpNorPermission(Player player, String perm)
	{
		if (!player.isOp() && !player.hasPermission(perm))
		{
			return true;
		}
		return false;
	}

	public Boolean hasOpOrPermission(Player player, String perm)
	{
		if (player.isOp() || player.hasPermission(perm))
		{
			return true;
		}
		return false;
	}

	public void setMetadata(Player player, String key, Object value)
	{
		player.setMetadata(key, new FixedMetadataValue(this, value));
	}

	public Object getMetadata(Player player, String key)
	{
		List<MetadataValue> values = player.getMetadata(key);

		// why are we iterating?
		for (MetadataValue value : values)
		{
			return value.value();
		}

		// if we got here, we didnt get a metadata value... why?

		return null;
	}

	public Boolean hasMetadata(Player player, String key)
	{
		if (getMetadata(player, key) != null)
		{
			return true;
		}
		return false;
	}

	/**
	 * Meat of plugin below
	 */

	public void messageAllPlayers(Player from, String msg)
	{
		getLogger().info("[G] " + from.getName() + ": " + msg);
		getLogger().info(msg);
		for (Player p : Bukkit.getOnlinePlayers())
		{
			p.sendMessage("[G]<" + from.getDisplayName() + "> " + msg);
		}
	}

	public void messageLocal(Player from, String msg)
	{
		getLogger().info("[L] " + from.getName() + ": " + msg);
		for (Player player : from.getWorld().getPlayers())
		{
			if (from.getLocation().distance(player.getLocation()) <= 100)
			{
				player.sendMessage("[Local]<" + from.getDisplayName() + "> " + ChatColor.GREEN + msg);
			}
		}
	}

	public void messageStaff(Player from, String msg)
	{
		getLogger().info("[A] " + from.getName() + ": " + msg);
		getLogger().info(msg);
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (hasOpOrPermission(p, "localchat.admin"))
			{
				p.sendMessage("[Admin]<" + from.getDisplayName() + "> " + ChatColor.AQUA + msg);
			}
		}
	}

	public void DEBUGERROR(Player player, String msg)
	{

		getLogger().info(msg);
		messageStaff(player, msg);
	}

	@EventHandler
	public void onChatMessage(AsyncPlayerChatEvent e)
	{
		// If cancelled by muting plugin or something, dont continue.
		if (e.isCancelled())
		{
			return;
		}

		// Cancel this event because we never really want to use normal chat anymore.
		e.setCancelled(true);

		// Get player
		Player player = e.getPlayer();

		String chatmode = "LOCAL";

		// TODO: Construct message tags and brackets using the message(locality) function
		// Check if data is set
		if (hasMetadata(player, "chatmode"))
		{
			chatmode = (String) getMetadata(player, "chatmode");
		}
		else
		{

			// Why dont he have this fuckin data? Set it.
			DEBUGERROR(player, ChatColor.DARK_RED + "NACHOERROR: Data wasnt set on: " + player.getName() + " - saying message: " + e.getMessage());
			this.DEBUGERROR(player, ChatColor.RED + "NACHOERROR: HASMETADATA BEFORE: " + hasMetadata(player, "chatmode"));
			forceLocalMode(player);
			this.DEBUGERROR(player, ChatColor.GREEN + "NACHOERROR: HASMETADATA AFTER: " + hasMetadata(player, "chatmode"));
			// this.DEBUGERROR(player, "NACHOERROR: Report to NachoHat - logs need checking.");

			// player.sendMessage(ChatColor.AQUA + "RANDOM BUG: chatmode SET TO LOCAL! TELL NACHOHAT IF THIS HAPPENS PLEASE.");

			// lets see if player was added to the hashmap

			String added = map.get(player);

			if (added == null)
			{

				this.DEBUGERROR(player, ChatColor.RED + "NACHOERROR: PLAYER NOT IN HASHMAP");

			}
			else
			{
				this.DEBUGERROR(player, ChatColor.RED + "NACHOERROR: IN HASH: " + added);

			}

		}

		/**
		 * local
		 */
		if (chatmode == "LOCAL")
		{
			messageLocal(player, e.getMessage());
		}

		/**
		 * global
		 */
		if (chatmode == "GLOBAL")
		{
			if (hasOpOrPermission(player, "localchat.global"))
			{
				messageAllPlayers(player, e.getMessage());
			}
			else
			{
				// In global but dont have perms? Fuck off good sir!
				player.sendMessage(font + "Permissions needed, changing your chatmode!");
				forceLocalMode(player);
				// we'll be nice and go ahead and pass the chat in local
				messageLocal(player, e.getMessage());
			}
		}

		/**
		 * admin
		 */
		if (chatmode == "ADMIN")
		{
			if (hasOpOrPermission(player, "localchat.admin"))
			{
				messageStaff(player, e.getMessage());
			}
			else
			{
				// In adminchat but dont have perms? Fuck off good sir!
				player.sendMessage(font + "Permissions needed, changing your chatmode!");
				forceLocalMode(player);
				// we'll be nice and go ahead and pass the chat in local
				messageLocal(player, e.getMessage());
			}
		}
	}

	/**
	 * Force modes
	 */
	public void forceLocalMode(Player player)
	{
		player.sendMessage(font + "Chat mode is (L)ocal");
		setMetadata(player, "chatmode", "LOCAL");
		map.put(player, "ADDED");
	}

	public void forceAdminMode(Player player)
	{
		player.sendMessage(font + "Chat mode is (A)dmin");
		setMetadata(player, "chatmode", "ADMIN");
		map.put(player, "ADDED");
	}

	public void forceGlobalMode(Player player)
	{
		player.sendMessage(font + "Chat mode is (G)lobal");
		setMetadata(player, "chatmode", "GLOBAL");
		map.put(player, "ADDED");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3)
	{
		/**
		 * Switch to Global, Local, or Admin chat modes
		 */
		if (isSenderConsole(sender))
		{
			sender.sendMessage("This command can only be run by a PLAYER.");
			return true;
		}

		// cast player
		Player player = (Player) sender;

		/**
		 * switch to local
		 */
		if (arg2.equalsIgnoreCase("L"))
		{
			forceLocalMode(player);
			return true;
		}

		/**
		 * switch to global
		 */
		if (arg2.equalsIgnoreCase("G"))
		{
			if (hasOpOrPermission(player, "localchat.global"))
			{
				this.forceGlobalMode(player);
			}
			else
			{
				player.sendMessage(font + "Global permissions needed");
			}
			return true;
		}

		/**
		 * switch to admin
		 */
		if (arg2.equalsIgnoreCase("A"))
		{
			if (hasOpOrPermission(player, "localchat.admin"))
			{
				this.forceAdminMode(player);
			}
			else
			{
				player.sendMessage(font + "Admin permissions needed");
			}
			return true;
		}

		// No good input, lets display help
		player.sendMessage(font + "Use /G, /L, or /A to set your chat mode!");
		return true;
	}

	@Override
	public void onEnable()
	{
		super.onEnable();
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Nachos Local Chat Plugin Loaded");
		for (Player p : Bukkit.getOnlinePlayers())
		{
			forceLocalMode(p);
		}
	}

	HashMap<Player, String> map = new HashMap<Player, String>();

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		forceLocalMode(player);
	}

}
