//The package
package fructose.Invbank;
//All the imports
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
//Start the class BasicBlockListener
public class InvbankBlockListener extends BlockListener{
	 public static Invbank plugin;
	 public InvbankBlockListener(Invbank instance) {
    	 plugin = instance;
    }
	 //This method is called when ever a block is placed.
	 public void onBlockPlace(BlockPlaceEvent event) {
		 //Get the player doing the placing
			Player player = event.getPlayer();
			//Get the block that was placed
			Block block = event.getBlockPlaced();
			//If the block is a torch and the player has the command enabled. Do this.
			//if(block.getType() == Material.TORCH && InvbankPlayerListener.plugin.enabled(player)){
				//Tells the player they have placed a torch
			//player.sendMessage("You placed a torch!");
			
		}
}
