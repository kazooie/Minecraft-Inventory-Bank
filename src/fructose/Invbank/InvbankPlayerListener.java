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
	/**
	 * Parses an item string, whether it's a number or the material name and returns the int. -1 if error.
	 * @param item
	 * @return
	 */
	private int parseItem(String item)
	{
		int blockId = -1;
		try{
			blockId = Integer.parseInt(item);
		}
		catch(NumberFormatException e){
			try{
			blockId = Material.valueOf(item.toUpperCase()).getId();
			}
			catch(IllegalArgumentException e2){
				blockId = -1;
			}
		}
		return blockId;
		
	}
	private void executeSetPass(String[] split, Player player)
	{
		if(split.length > 2){
			if(dbConn.updatePassword(player, split[2]))
				unregisteredPlayers.remove(player);
		}
		else{
			player.sendMessage("Please enter a password as an argument.");
		}
	}
	
	private void executeWithdraw(String[] split, Player player)
	{
		if(split.length<3){
			player.sendMessage("Please enter item id");
		}
		else{
			int blockId = parseItem(split[2]);
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
			else
				player.sendMessage("Unable to find "+split[2]);
		}
	}
	private void executeDeposit(String[] split, Player player)
	{
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
	private void executeList(String[] split, Player player)
	{
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
	/**
	 * "/inv sell <item> <quantity_per> <price> <amount>"
	 * @param split
	 * @param player
	 */
	private void executeSell(String[] split, Player player) {
		if(split.length == 6){
			int blockId = parseItem(split[2]);
			try{
				int quantityPer = Integer.parseInt(split[3]);
				int price = Integer.parseInt(split[4]);
				int amount = Integer.parseInt(split[5]);
				dbConn.sellItem(player, blockId, quantityPer, price, amount);
			}
			catch(NumberFormatException e){
				player.sendMessage("Unable to read arguments.");
			}
		}
		else{
			player.sendMessage("Please enter all arguments.");
		}
	}
	private void exeucteBuy(String[] split, Player player) {
		player.sendMessage("Recieved message: buy");
		//TODO
	}
	private void executeShopList(String[] split, Player player) {
		player.sendMessage("Recieved message: shoplist");
		//TODO
	}
	private void executeKit(String[] split, Player player) {
		if(split.length == 2){
			//User just entered "inv kit", list their kits
			dbConn.listKits(player);
		}
		else if(split.length > 2){
			dbConn.withdrawKit(player, split[2]);
		}
		else{
			player.sendMessage("An error occured.");
		}
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
				executeDeposit(split,player);
			}
			else if(split[1].equalsIgnoreCase("withdraw")|| split[1].equalsIgnoreCase("w")){
				executeWithdraw(split, player);
			}
			else if(split[1].equalsIgnoreCase("list") || split[1].equalsIgnoreCase("l")){
				executeList(split, player);
			}
			else if(split[1].equalsIgnoreCase("setpass") || split[1].equalsIgnoreCase("sp"))
			{
				executeSetPass(split, player);
			}
			else if(split[1].equalsIgnoreCase("shoplist") || split[1].equalsIgnoreCase("sl"))
			{
				executeShopList(split, player);
			}
			else if(split[1].equalsIgnoreCase("buy") || split[1].equalsIgnoreCase("b"))
			{
				exeucteBuy(split,player);
			}
			else if(split[1].equalsIgnoreCase("sell") || split[1].equalsIgnoreCase("s"))
			{
				executeSell(split,player);
			}
			else if(split[1].equalsIgnoreCase("kit") || split[1].equalsIgnoreCase("k"))
			{
				executeKit(split,player);
			}
		}
	}

	private void displayHelp(Player player) {
		player.sendMessage("---Invbank Help---");
		player.sendMessage("() - optional, <> - required");
		player.sendMessage("/inv deposit (all) - Deposits what you're holding (all for all of same type)");
		player.sendMessage("/inv withdraw <item> (quantity) - withdraws <item>");
		player.sendMessage("/inv list (page#)- view stored items");
		player.sendMessage("/inv shoplist (page#) - view items for sale");
		player.sendMessage("/inv buy <item> (quantity)- buys an item");
		player.sendMessage("/inv sell <item> <quantity_per> <price> <amount>");
		player.sendMessage("/inv kit <name> - withdraws the kit items");
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
