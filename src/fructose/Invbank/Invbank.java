//The Package
package fructose.Invbank;
//All the imports
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

/**
 * Basic for Bukkit
 *
 * @author Samkio
 */
//Starts the class
public class Invbank extends JavaPlugin{
	//Links the BasicPlayerListener
	private final InvbankPlayerListener playerListener = new InvbankPlayerListener(this);
	//Links the BasicBlockListener
    private final InvbankBlockListener blockListener = new InvbankBlockListener(this);

	@Override
	//When the plugin is disabled this method is called.
	public void onDisable() {
		//Print "Basic Disabled" on the log.
		playerListener.disconnect();
		log(this.getDescription().getName()+ " Disabled");		
	}

	@Override
	//When the plugin is enabled this method is called.
	public void onEnable() {
		//Create the pluginmanage pm.
		PluginManager pm = getServer().getPluginManager();
		//Create PlayerCommand listener
	    //pm.registerEvent(Event.Type., this.playerListener, Event.Priority.Normal, this);
	    //Create BlockPlaced listener
        pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_LOGIN, this.playerListener, Event.Priority.Normal, this);
       //Get the infomation from the yml file.
        PluginDescriptionFile pdfFile = this.getDescription();
        //Print that the plugin has been enabled!
        log( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
		
	}

	 @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        try {
            String[] split = new String[args.length + 1];
            split[0] = cmd.getName().toLowerCase();
            String fullCmd = split[0];

            for (int i = 0; i < args.length; i++) {
                split[i + 1] = args[i];
                fullCmd +=" "+ args[i];
            }
            log(sender.toString()+" sent command: "+fullCmd);

            playerListener.onPlayerCommand(sender, split);
          } catch (Exception e) {
             e.printStackTrace();
         }

         return false;
     }
	 public void log(String msg)
	 {
		 System.out.println("[Invbank] "+msg);
	 }
}
