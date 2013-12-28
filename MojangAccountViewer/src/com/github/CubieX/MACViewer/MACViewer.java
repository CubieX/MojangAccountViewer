/*
 * MACViewer - A CraftBukkit plugin that shows a players unique Mojang account UUID
 * Copyright (C) 2013  CubieX
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

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.edawg878.identifier.IdentifierAPI;

public class MACViewer extends JavaPlugin
{
   public static final Logger log = Bukkit.getServer().getLogger();
   public static final String logPrefix = "[MACViewer] "; // Prefix to go in front of all log entries
   public static final int MAX_RETRIEVAL_TIME = 1000;  // max time in ms to wait for an async PlayerUUID request to deliver its result
   // This prevents async task jam in case HTTP is unreachable or connection is very slow
   private CommandHandler comHandler = null;
   private ConfigHandler cHandler = null;  
   private EntityListener eListener = null;
   private SchedulerHandler schedHandler = null;
   private IdentifierAPI identAPI = null;

   public static boolean debug = false;

   //*************************************************
   static String usedConfigVersion = "1"; // Update this every time the config file version changes, so the plugin knows, if there is a suiting config present
   //*************************************************

   @Override
   public void onEnable()
   {
      cHandler = new ConfigHandler(this); 
      
      if(!checkConfigFileVersion())
      {
         log.severe(logPrefix + "Outdated or corrupted config file(s). Please delete your config files."); 
         log.severe(logPrefix + "will generate a new config for you.");
         log.severe(logPrefix + "will be disabled now. Config file is outdated or corrupted.");
         getServer().getPluginManager().disablePlugin(this);
         return;
      }
      
      readConfigValues();
    
      //schedHandler = new SchedulerHandler(this);     
      eListener = new EntityListener(this, schedHandler);
     
      identAPI = new IdentifierAPI();
      
      comHandler = new CommandHandler(this, cHandler, identAPI);      
      getCommand("av").setExecutor(comHandler);
      
      //schedHandler.startNotifierScheduler_SynchRepeating();

      log.info(logPrefix + "version " + getDescription().getVersion() + " is enabled!");            
   }

   private boolean checkConfigFileVersion()
   {      
      boolean configOK = false;     

      if(getConfig().isSet("config_version"))
      {
         String configVersion = cHandler.getConfig().getString("config_version");

         if(configVersion.equals(usedConfigVersion))
         {
            configOK = true;
         }
      }

      return (configOK);
   }

   public void readConfigValues()
   {      
      boolean exceed = false;
      boolean invalid = false;

      if(getConfig().isSet("debug")){debug = getConfig().getBoolean("debug");}else{invalid = true;}
           
      if(exceed)
      {
         log.warning(logPrefix + "One or more config values are exceeding their allowed range. Please check your config file!");
      }

      if(invalid)
      {
         log.warning(logPrefix + "One or more config values are invalid. Please check your config file!");
      }
   }

   @Override
   public void onDisable()
   {
      this.getServer().getScheduler().cancelTasks(this);      
      cHandler = null;
      eListener = null;
      comHandler = null;
      schedHandler = null;
      identAPI = null;
      log.info(this.getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
   }   

   // ####################################################################################################
   // ####################################################################################################
   
   
}


