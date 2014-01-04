package com.github.CubieX.MACViewer;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.edawg878.identifier.IdentifierAPI;

public class CommandHandler implements CommandExecutor
{
   private MACViewer plugin = null;
   private IdentifierAPI identAPI = null;  

   public CommandHandler(MACViewer plugin, IdentifierAPI identAPI) 
   {
      this.plugin = plugin;
      this.identAPI = identAPI;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
   {
      Player player = null;
      if (sender instanceof Player) 
      {
         player = (Player) sender;
      }

      if (cmd.getName().equalsIgnoreCase("av"))
      {
         if (args.length == 0)
         { //no arguments, so help will be displayed
            return false;
         }
         else if (args.length == 1)
         {
            if (args[0].equalsIgnoreCase("version"))
            {            
               sender.sendMessage(ChatColor.GREEN + "This server is running " + plugin.getDescription().getName() + " version " +
                     plugin.getDescription().getVersion());
               return true;
            }            
         }
         else if(args.length == 2)
         {
            // UUID of Mojang account for given player name will be displayed =================================
            if (args[0].equalsIgnoreCase("uuid"))
            {
               if(sender.hasPermission("macviewer.use"))
               {                  
                  plugin.getPlayerUUIDcallback(sender, args[1]);
               }

               return true;
            }
         }
         else
         {
            sender.sendMessage(ChatColor.YELLOW + "Invalid argument count.");         
         }

         return false;
      }

      return false;
   }

   // ###################################################################################################

   
}
