package com.github.CubieX.MACViewer;

public class SchedulerHandler
{
   private MACViewer plugin = null;

   public SchedulerHandler(MACViewer plugin)
   {
      this.plugin = plugin;
   }
   
   public void startNotifierScheduler_SynchRepeating()
   {
      plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable()
      {
         public void run()
         { 
            
         }
      }, 20L, 20L); // 10 seconds initial delay, 10 minutes cycle
   }
}
