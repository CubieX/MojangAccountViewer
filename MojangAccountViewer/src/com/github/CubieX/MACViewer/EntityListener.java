package com.github.CubieX.MACViewer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class EntityListener implements Listener
{
   private MACViewer plugin = null;

   public EntityListener(MACViewer plugin)
   {        
      this.plugin = plugin;    
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

 //================================================================================================    
   @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
   public void onAsyncUUIDRetrievedEvent(final AsyncUUIDRetrievedEvent e)
   {
      // Beware! This event is asynchronously called!
      // so make sure to use a sync task if you are accessing any Bukkit API methods
      Bukkit.getServer().getScheduler().runTask(plugin, new Runnable()
      {
         @Override
         public void run()
         {
            if(null != e.getSender())
            {
               if(e.getSender() instanceof Player)
               {
                  Player p = (Player)e.getSender();

                  if(p.isOnline()) // player may have quit while query was running
                  {
                     if(null != e.getPlayerUUID())
                     {
                        if(!e.getPlayerUUID().equals(""))
                        {
                           p.sendMessage(ChatColor.GREEN + "UUID von " + ChatColor.WHITE + e.getPlayerName() + ChatColor.GREEN + ":\n" + ChatColor.WHITE + e.getPlayerUUID());
                        }
                        else
                        {
                           p.sendMessage(ChatColor.YELLOW + "Dieser Spieler ist nicht bei Mojang registriert!");
                        }                     
                     }
                     else
                     {
                        p.sendMessage(MACViewer.logPrefix + "Request timed out! (" + MACViewer.MAX_RETRIEVAL_TIME + " ms)");
                     }
                  }
               }
               else
               {
                  if(null != e.getPlayerUUID())
                  {
                     if(!e.getPlayerUUID().equals(""))
                     {
                        e.getSender().sendMessage(ChatColor.GREEN + "UUID von " + ChatColor.WHITE + e.getPlayerName() + ChatColor.GREEN + ": " + ChatColor.WHITE + e.getPlayerUUID());
                     }
                     else
                     {
                        e.getSender().sendMessage(ChatColor.YELLOW + "Dieser Spieler ist nicht bei Mojang registriert!");
                     }                     
                  }
                  else
                  {
                     e.getSender().sendMessage(MACViewer.logPrefix + "Request timed out! (" + MACViewer.MAX_RETRIEVAL_TIME + " ms)");
                  }
               }
            }
         }
      });
   }

   //############################################################################


}
