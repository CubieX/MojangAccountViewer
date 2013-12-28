package com.github.CubieX.MACViewer;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncUUIDRetrievedEvent extends Event
{
   private static final HandlerList handlers = new HandlerList();
   private CommandSender sender = null;
   private String playerName = null;
   private String playerUUID = null;
   
   //Constructor
   public AsyncUUIDRetrievedEvent(CommandSender sender, String playerName, String playerUUID)
   {
      this.sender = sender;
      this.playerName = playerName;
      this.playerUUID = playerUUID;
   }

   public CommandSender getSender()
   {
      return (this.sender);
   }
   
   public String getPlayerName()
   {
      return (this.playerName);
   }
   
   public String getPlayerUUID()
   {
      return (this.playerUUID);
   }

   public HandlerList getHandlers()
   {
      return handlers;
   }

   public static HandlerList getHandlerList()
   {
      return handlers;
   }
}
