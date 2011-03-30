//The Package
package fructose.Invbank;
//All the imports
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
//Starts the class BasicPlayer listener
public class InvbankPlayerListener extends PlayerListener{
	public static Invbank plugin;
	private InvbankDBConn dbConn;
	public InvbankPlayerListener(Invbank instance) {
		plugin = instance;
		dbConn = new InvbankDBConn();
		if(dbConn.isConnected())
			plugin.log("Connected to database.");
		else
			plugin.log("Unable to connect to database.");
	}
	//This method is called whenever a player uses a command.
	public void onPlayerCommand(CommandSender sender, String[] split) {
		if(sender instanceof Player)
		{	
			executeCommand(split,(Player) sender);
		}		
	}
	public void onPlayerLogin(PlayerLoginEvent event){
		dbConn.updateUser(event.getPlayer());
	}
	private void executeCommand(String[] split, Player player)
	{
		if(split.length == 1)//User typed "/inv", display help
		{
			displayHelp(player);
		}
		else if(split.length >= 2)
		{
			if(split[1].equalsIgnoreCase("deposit") || split[1].equalsIgnoreCase("d"))
			{
				if(split.length==2)
				{
					//Deposit item player is holding
					dbConn.depositItem(player, player.getItemInHand());
					player.setItemInHand(null);
				}
			}
			else if(split[1].equalsIgnoreCase("withdraw")|| split[1].equalsIgnoreCase("w"))
			{
				if(split.length<3){
					player.sendMessage("Please enter item id");
				}
				else
				{
					int blockId = -1;
					try{
						blockId = Integer.parseInt(split[2]);
					}
					catch(NumberFormatException e){
						try{
						blockId = Material.valueOf(split[2].toUpperCase()).getId();
						}
						catch(IllegalArgumentException e2)
						{
							player.sendMessage("Couldn't find "+split[2]);
							blockId=-1;
						}
					}
					if(blockId>0){
						if(split.length<4){
							dbConn.withdrawItem(player, blockId, 64);
						}
						else{
							try{
								int quantity = Integer.parseInt(split[3]);
								dbConn.withdrawItem(player, blockId, quantity);
							}
							catch(NumberFormatException e){
								player.sendMessage("Unable to read quantity; defaulted to 64.");
								dbConn.withdrawItem(player, blockId, 64);
							}
						}
					}
				}
			}
			else if(split[1].equalsIgnoreCase("list") || split[1].equalsIgnoreCase("l")){
				if(split.length<3)
				{
					dbConn.listItems(player, 1);
				}
			}
		}
	}
	private void displayHelp(Player player) {
		player.sendMessage("---Invbank Help---");
		player.sendMessage("/inv deposit - Deposits what you're holding");
		player.sendMessage("/inv withdraw <item> (quantity) - withdraws <item>");
		player.sendMessage("/inv list - view stored items");
	}
	public void disconnect(){
		dbConn.disconnect();
	}
}
