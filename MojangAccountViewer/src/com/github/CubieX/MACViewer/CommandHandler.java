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
   private ConfigHandler cHandler = null;
   private IdentifierAPI identAPI = null;  

   public CommandHandler(MACViewer plugin, ConfigHandler cHandler, IdentifierAPI identAPI) 
   {
      this.plugin = plugin;
      this.cHandler = cHandler;
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

            // RELOAD the plugin ================================
            if (args[0].equalsIgnoreCase("reload"))
            {
               if(sender.hasPermission("macviewer.admin"))
               {
                  cHandler.reloadConfig(sender);                  
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to reload " + plugin.getDescription().getName() + "!");
               }

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
                  getPlayerUUIDcallback(sender, args[1]);                 
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

   /**
    * Executes a POST to get the players Mojang UUID from Mojang account server via an async callback method<br>
    * and delivers the ResultSet through a Future object by firing a custom Event.
    * 
    * @param playerName The player name to get he UUID for
    * 
    * @return playerUUID The players UUID
    * */
   public void getPlayerUUIDcallback(final CommandSender sender, final String playerName)
   {
      Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable()
      {
         @Override
         public void run()
         {
            ExecutorService executor = Executors.newSingleThreadExecutor();

            FutureTask<String> future = new FutureTask<String>(new Callable<String>()
                  {
               @Override
               public String call()
               {
                  if(MACViewer.debug){MACViewer.log.info(MACViewer.logPrefix + "getPlayerUUIDcallback running its callable and waiting for UUID...");}

                  String playerUUID = "";
                  playerUUID = identAPI.getPlayerUUID(playerName); // get players UUID from Mojang servers

                  if(MACViewer.debug){MACViewer.log.info(MACViewer.logPrefix + "getPlayerUUIDcallback callable has retrieved the UUID. Returning the UUID now...");}
                  return playerUUID;
               }});

            executor.execute(future); // start the callable task to retrieve the result

            if(MACViewer.debug){MACViewer.log.info(MACViewer.logPrefix + "Async tasks main thread working while waiting for UUID...work...work...");}

            try
            {
               String playerUUID = "";

               try
               {
                  playerUUID = future.get(MACViewer.MAX_RETRIEVAL_TIME, TimeUnit.MILLISECONDS); // will wait until result is ready, but will return if MAX_RETRIEVAL_TIME has expired                  
               }
               catch (TimeoutException e)
               {
                  playerUUID = null;
                  //e.printStackTrace();
               }
               finally
               {
                  executor.shutdown(); // shutdown executor service after Callable has finished and returned its value (will block current task until all threads in the pool have finished!)
                  // Using this order will make sure, the task will return after given MAX_RETRIEVAL_TIME and not block forever, if the DB connection is dead or slow
               }

               // future.get() will block this thread until the result is ready
               if(MACViewer.debug){MACViewer.log.info(MACViewer.logPrefix + "UUID aquired. Now firing AsyncUUIDRetrievedEvent...");}

               // fire custom query event ================================================                                
               AsyncUUIDRetrievedEvent getUUIDEvent = new AsyncUUIDRetrievedEvent(sender, playerName, playerUUID); // Create the event
               plugin.getServer().getPluginManager().callEvent(getUUIDEvent); // fire Event         
               //==========================================================================                 
            }
            catch (InterruptedException e)
            {
               e.printStackTrace();
            }
            catch (ExecutionException e)
            {        
               MACViewer.log.severe(MACViewer.logPrefix + e.getMessage());
            }

            if(MACViewer.debug){MACViewer.log.info(MACViewer.logPrefix + "getPlayerUUIDcallback task finished.");}
         }
      });
   }
}
