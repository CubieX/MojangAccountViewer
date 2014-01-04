/*
 * MACViewer - A CraftBukkit plugin that shows a players unique Mojang account UUID.
 * Also works as API for other plugins to retrieve a players UUID.
 * Copyright (C) 2014  CubieX
 * Used parts of IdentifierAPI from EDawg878
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.github.CubieX.MACViewer;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.edawg878.identifier.IdentifierAPI;

public class MACViewer extends JavaPlugin
{
   public static final Logger log = Bukkit.getServer().getLogger();
   public static final String logPrefix = "[MACViewer] "; // Prefix to go in front of all log entries
   public static final int MAX_RETRIEVAL_TIME = 2000;  // max time in ms to wait for an async PlayerUUID request to deliver its result
   // This prevents async task jam in case HTTP is unreachable or connection is very slow
   private CommandHandler comHandler = null;
   private EntityListener eListener = null;
   private IdentifierAPI identAPI = null;

   public static boolean debug = false;

   //*************************************************
   static String usedConfigVersion = "1"; // Update this every time the config file version changes, so the plugin knows, if there is a suiting config present
   //*************************************************

   @Override
   public void onEnable()
   {      
      eListener = new EntityListener(this);     
      identAPI = new IdentifierAPI();      
      comHandler = new CommandHandler(this, identAPI);      
      getCommand("av").setExecutor(comHandler);

      log.info(logPrefix + "version " + getDescription().getVersion() + " is enabled!");            
   }

   @Override
   public void onDisable()
   {
      this.getServer().getScheduler().cancelTasks(this);     
      eListener = null;
      comHandler = null;
      identAPI = null;
      log.info(this.getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
   }

   // ####################################################################################################
   // ####################################################################################################

   /**
    * Executes a POST to get the players Mojang UUID from Mojang account server via an async callback method<br>
    * and delivers the ResultSet through a Future object by firing a custom Event.
    * To use this, make sure to listen for Event AsyncUUIDRetrievedEvent
    * CAUTION: This may deliver no UUID if more than one player with the same name are registered at Mojang!
    * 
    * @param playerName The player name to get he UUID for
    * 
    * @return playerUUID The players UUID
    * */
   public void getPlayerUUIDcallback(final CommandSender sender, final String playerName)
   {
      Bukkit.getServer().getScheduler().runTaskAsynchronously(this, new Runnable()
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
                  playerUUID = identAPI.getPlayerUUID(playerName); // get players UUID from Mojang servers. Probably better do this once on every login!

                  if(null != Bukkit.getServer().getPlayer(playerName))
                  {
                     sender.sendMessage("PlayerUUID from Bukkit: " + Bukkit.getServer().getPlayer(playerName).getUniqueId().toString()); // get the UUID from Bukkit (only for online players, but faster, because it gets fetched on login)
                  }

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
               Bukkit.getServer().getPluginManager().callEvent(getUUIDEvent); // fire Event         
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


