//The Package
package fructose.Invbank;
//All the imports

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
//Starts the class BasicPlayer listener
public class InvbankPlayerListener extends PlayerListener{
	public static Invbank plugin;
	private InvbankDBConn dbConn;
	private ArrayList<Player> unregisteredPlayers;
	public InvbankPlayerListener(Invbank instance) {
		plugin = instance;
		dbConn = new InvbankDBConn();
		if(dbConn.isConnected()){
			plugin.log("Connected to database.");
		}
		else{
			plugin.log("Unable to connect to database.");
		}
		unregisteredPlayers = new ArrayList<Player>();
	}
	//This method is called whenever a player uses a command.
	public void onPlayerCommand(CommandSender sender, String[] split) {
		if(sender instanceof Player)
		{	
			executeCommand(split,(Player) sender);
		}		
	}
	public void onPlayerLogin(PlayerLoginEvent event){
		if(!dbConn.updateUser(event.getPlayer())){
			unregisteredPlayers.add(event.getPlayer());
		}
		else
			plugin.log(event.getPlayer().getName()+" has password.");
			
	}
	private void executeCommand(String[] split, Player player)
	{
		
		if(split.length == 1){//User typed "/inv", display help
			displayHelp(player);
			if(unregisteredPlayers.contains(player)){
				player.sendMessage("Reminder: Set your InvBank password using /inv setpass <password>");
			}
		}
		else if(split.length >= 2){
			if(unregisteredPlayers.contains(player) && !split[1].equalsIgnoreCase("setpass")){
				player.sendMessage("Reminder: Set your InvBank password using /inv setpass <password>");
			}
			if(split[1].equalsIgnoreCase("deposit") || split[1].equalsIgnoreCase("d")){
				if(split.length==2){
					//Deposit item player is holding
					dbConn.depositItem(player, player.getItemInHand(), false);
				}
				else if(split.length > 2){
					if(split[2].equalsIgnoreCase("all")){
						dbConn.depositItem(player, player.getItemInHand(), true);
					}
				}
			}
			else if(split[1].equalsIgnoreCase("withdraw")|| split[1].equalsIgnoreCase("w")){
				if(split.length<3){
					player.sendMessage("Please enter item id");
				}
				else{
					int blockId = -1;
					try{
						blockId = Integer.parseInt(split[2]);
					}
					catch(NumberFormatException e){
						try{
						blockId = Material.valueOf(split[2].toUpperCase()).getId();
						}
						catch(IllegalArgumentException e2){
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
				if(split.length > 2){
					try{
						int page = Integer.parseInt(split[2]);
						dbConn.listItems(player, page);
					}
					catch(NumberFormatException e){
						player.sendMessage("Unable to read page number; defaulted to 1");
						dbConn.listItems(player, 1);
					}
				}
				else{
					dbConn.listItems(player, 1);
				}
			}
			else if(split[1].equalsIgnoreCase("setpass"))
			{
				if(split.length > 2){
					if(dbConn.updatePassword(player, split[2]))
						unregisteredPlayers.remove(player);
				}
				else{
					player.sendMessage("Please enter a password as an argument.");
				}
			}
		}
	}
	private void displayHelp(Player player) {
		player.sendMessage("---Invbank Help---");
		player.sendMessage("/inv deposit (all) - Deposits what you're holding (all for all of same type)");
		player.sendMessage("/inv withdraw <item> (quantity) - withdraws <item>");
		player.sendMessage("/inv list (page#)- view stored items");
		if(!unregisteredPlayers.contains(player))
			player.sendMessage("/inv setpass <password> - change your password");
		
	}
	public boolean isConnected(){
		return dbConn.isConnected();
	}
	public void disconnect(){
		dbConn.disconnect();
	}
}
