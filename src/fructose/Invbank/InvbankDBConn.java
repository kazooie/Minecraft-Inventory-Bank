package fructose.Invbank;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

/**
 * InvbankDBConn
 * Class for database queries directly related to the Inventory Bank. Uses the DBToolKit to perform queries.
 * 
 * @author Steven Biersteker
 *
 */
public class InvbankDBConn {
	
	public InvbankDBConn(){
		if(!this.isConnected()){
			connect();
		}
	}
	/**
	 * Called automatically when initialized. Initializes the DBToolKit if it isn't already connected.
	 */
	private boolean connect(){
		//TODO make this more flexible, use a configuration file.
		try {
			DBToolKit.init("jdbc:mysql://mother.dyndns.tv:3306/minecraft","inventorybank","syrup");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			System.out.println("Please add the jdbc mysql jar to the lib directory.");
			return false;
		}
		return true;
	}
	public boolean isConnected(){
		return !(DBToolKit.getInstance()==null);
	}	
	
	/**
	 * Updates user information in database, or creates new entry if user does not exist.
	 * @param player
	 * @return true if user exists, false if user didn't exist or doesn't have password.
	 */
	public boolean updateUser(Player player)
	{
		if(this.isConnected()){
			int playerExists = 0;
			try {
				playerExists = DBToolKit.getInstance().selectQuery("SELECT password FROM user WHERE name = '"+player.getName()+"'");
				if(playerExists == 0)
				{
					DBToolKit.getInstance().updateQuery("INSERT INTO user (name) VALUES ('"+player.getName()+"')");
					return false;
				}
				if(DBToolKit.getInstance().get_value(0, 0) == null)
					return false;
				else
					return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	public static String toHex(byte[] bytes) {
	    BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "x", bi);
	}
	
	public boolean updatePassword(Player player, String password)
	{
		
		byte[] bytesOfMessage;
		byte[] thedigest;
		try {
			bytesOfMessage = password.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			thedigest = md.digest(bytesOfMessage);
		} catch (UnsupportedEncodingException e) {
			player.sendMessage("Unable to hash password");
			e.printStackTrace();
			return false;
		} catch (NoSuchAlgorithmException e) {
			player.sendMessage("Unable to hash password");
			e.printStackTrace();
			return false;
		}
		
		try {
			DBToolKit.getInstance().updateQuery("" +
					"UPDATE user " +
					"SET password = '"+toHex(thedigest)+"' "+
					"WHERE user.name = '"+player.getName()+"'");
		} catch (SQLException e) {
			player.sendMessage("Unable to store password");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Updates the item entry in the database. Adds it to the database if the item has never been stored before.
	 * @param item
	 */
	private void updateItem(ItemStack item)
	{
		try {
			int itemExists = DBToolKit.getInstance().selectQuery("SELECT id FROM item WHERE block_id = "+item.getTypeId());
			if(itemExists == 0)
			{
				DBToolKit.getInstance().updateQuery("INSERT INTO item (name,block_id) VALUES ('"+item.getType().toString()+"', "+item.getTypeId()+")");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Takes a player reference and returns the user_id from the database.
	 * @param player
	 * @return
	 */
	public int getUserDatabaseId(Player player)
	{
		try {
			int playerExists = DBToolKit.getInstance().selectQuery("SELECT id FROM user WHERE name = '"+player.getName()+"'");
			if(playerExists == 1)
				return Integer.parseInt(DBToolKit.getInstance().get_value(0, 0));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Couldn't find player: "+player.getName());
		return -1;
	}
	/**
	 * Takes a user id and returns their name.
	 * @param user_id
	 * @return
	 */
	public String getUserName(int user_id)
	{
		try {
			int playerExists = DBToolKit.getInstance().selectQuery("SELECT name FROM user WHERE id = " + user_id + ";");
			if(playerExists == 1)
				return String.parseString(DBToolKit.getInstance().get_value(0, 0));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Couldn't find player: "+player.getName());
		return "";
	}
	/**
	 * Takes an ItemStack reference and returns the item_id from the database. This is NOT the same as block_id.
	 * @param item
	 * @return
	 */
	public int getItemDatabaseId(ItemStack item)
	{
		try {
			int itemExists = DBToolKit.getInstance().selectQuery("SELECT id FROM item WHERE block_id = "+item.getTypeId());
			if(itemExists > 0)
				return Integer.parseInt(DBToolKit.getInstance().get_value(0, 0));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	public int getBlockId(int itemId)
	{
		try {
			int blockExists = DBToolKit.getInstance().selectQuery("" +
					"SELECT block_id FROM item WHERE id = "+itemId);
			if(blockExists > 0){
				return Integer.parseInt(DBToolKit.getInstance().get_value(0, 0));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public int getItemId(int blockId)
	{
		try {
			int itemExists = DBToolKit.getInstance().selectQuery("" +
					"SELECT id FROM item WHERE block_id = " + blockId);
			if(itemExists >0) {
				return Integer.parseInt(DBToolKit.getInstance().get_value(0, 0));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	/**
	 * Deposits a single ItemStack reference into the database. 
	 * Adds the item entry if it doesn't already exist.
	 * Adds the inventory entry if it doesn't already exist.
	 * @param player
	 * @param item
	 */
	public void depositItem(Player player, ItemStack item, boolean depositAll){
		updateItem(item);
		int prevQuantity = 0;
		MaterialData itemMeta = item.getData();
		short itemDur = 0;
		byte itemData = 0x0;
		if(itemMeta!=null){
			itemData = itemMeta.getData();
			//player.sendMessage("Metadata found: "+itemMeta.toString());
			player.sendMessage("WARNING: InvBank cannot currently store metadata with items (wool color, etc)");
		}
		itemDur = item.getDurability();
		
		int amount = 0;
		if(depositAll){
			ItemStack[] items = player.getInventory().getContents();
			for(int i=0; i< items.length; i++){
				if(items[i].getType() == item.getType()){
					amount += items[i].getAmount();
					//player.getInventory().setItem(i, null);
				}
			}
		}
		else{
			amount = item.getAmount();
		}
		
		if(amount!=0)
		{
			player.sendMessage("Item Data Byte:"+Integer.toHexString((int)itemData)+" Durability:"+itemDur);
			try {
				int invExists = DBToolKit.getInstance().selectQuery("" +
						"SELECT inventory.quantity " +
						"FROM inventory, user, item " +
						"WHERE inventory.item_id = item.id AND " +
						"inventory.user_id = user.id AND " +
						"user.name = '"+player.getName()+"' AND " +
						"item.block_id = "+item.getTypeId());
				if(invExists == 0)
				{
					int userId = getUserDatabaseId(player);
					int itemId = getItemDatabaseId(item);
					DBToolKit.getInstance().updateQuery("INSERT INTO inventory (user_id, item_id, quantity) VALUES ("+userId+","+itemId+","+amount+")");
				}
				else
				{
					prevQuantity = Integer.parseInt(DBToolKit.getInstance().get_value(0, 0));
					int userId = getUserDatabaseId(player);
					int itemId = getItemDatabaseId(item);
					DBToolKit.getInstance().updateQuery("" +
							"UPDATE inventory " +
							"SET quantity = "+(prevQuantity + amount)+" " +
							"WHERE user_id = "+userId+" AND " +
							"item_id = "+itemId);
				}
				player.sendMessage(item.getType().toString()+": Previous: "+prevQuantity+" Now: "+(prevQuantity+amount));
			} catch (SQLException e) {
				player.sendMessage("An error occurred. The server probably needs restarted.");
				e.printStackTrace();
				DBToolKit.getInstance().close();
				return;
			}
		
		
			//Remove inventory
			
			if(depositAll){
				ItemStack[] items = player.getInventory().getContents();
				for(int i=0; i< items.length; i++){
					if(items[i].getType() == item.getType()){
						player.getInventory().setItem(i, null);
					}
				}
			}
			else{
				player.setItemInHand(null);
			}
		}		
	}
	public void listItems(Player player, int page){
		int textRows = 9;
		int userId = getUserDatabaseId(player);
		try {
			int numItems = DBToolKit.getInstance().selectQuery("" +
					"SELECT item.block_id, item.name, inventory.quantity " +
					"FROM item, inventory, user " +
					"WHERE user.id = " + userId + " AND " +
					"user.id = inventory.user_id AND " +
					"item.id = inventory.item_id " +
					"AND inventory.quantity > 0 " +
					"ORDER BY item.block_id ASC");
			int startPoint = (page-1)*textRows;
			int totalPages = (int) Math.ceil((double)numItems / (double)textRows);
			if(startPoint > numItems || page < 1){
				player.sendMessage("Invalid page number. Number of pages: "+totalPages);
				return;
			}
			player.sendMessage("Storing "+numItems+" item types. Page "+page+" of "+totalPages);
			int endPoint = startPoint + textRows;
			if(endPoint > numItems)
				endPoint = numItems;
			for(int i = startPoint; i < endPoint; i++)
			{
				int blockId = Integer.parseInt(DBToolKit.getInstance().get_value(i, 0));
				String itemName = DBToolKit.getInstance().get_value(i,1);
				int quantity = Integer.parseInt(DBToolKit.getInstance().get_value(i,2));
				player.sendMessage("id"+blockId + ":"+itemName+" - "+quantity);
				
			}
		} catch (SQLException e) {
			player.sendMessage("Database error.");
			e.printStackTrace();
		}
	}
	/**
	 * Withdraws a quantity of a given item and adds it to the player's inventory.
	 * 
	 * @param player
	 * @param blockId
	 * @param quantity
	 */
	public void withdrawItem(Player player, int blockId, int quantity){
		try {
			int itemExists = DBToolKit.getInstance().selectQuery("SELECT id FROM item WHERE block_id = "+blockId);
			if(itemExists <= 0){
				player.sendMessage("Item #"+blockId+" does not exist in the database.");
			}
			else{
				int itemId = Integer.parseInt(DBToolKit.getInstance().get_value(0, 0));
				int userId = getUserDatabaseId(player);
				int invExists = DBToolKit.getInstance().selectQuery("" +
						"SELECT quantity " +
						"FROM inventory " +
						"WHERE user_id = " + userId + " AND " +
						"item_id = " + itemId);
				if(invExists <=0){
					player.sendMessage("No "+Material.getMaterial(blockId)+" found in your inventory.");
				}
				else{
					int prevQuantity = Integer.parseInt(DBToolKit.getInstance().get_value(0,0));
					if(prevQuantity < quantity)
						quantity = prevQuantity;
					int leftover = prevQuantity - quantity;
					DBToolKit.getInstance().updateQuery("" +
							"UPDATE inventory " +
							"SET quantity = "+leftover+" " +
							"WHERE user_id = "+userId+" AND "+
							"item_id = "+itemId);
					int toAdd = 0;
					while(quantity != 0)
					{
						if(quantity > 64)
						{
							quantity -=64;
							toAdd = 64;
						}
						else
						{
							toAdd = quantity;
							quantity = 0;
						}
						ItemStack item = new ItemStack(blockId,toAdd);
						HashMap<Integer, ItemStack> map = player.getInventory().addItem(item);
						if(map.size()!=0)
						{
							quantity+=toAdd;
							player.sendMessage("Not enough room in your inventory. Returning "+quantity);
							leftover +=quantity;
							DBToolKit.getInstance().updateQuery("" +
									"UPDATE inventory " +
									"SET quantity = "+leftover+" " +
									"WHERE user_id = "+userId+" AND "+
									"item_id = "+itemId);
							break;
						}
					}
					DBToolKit.getInstance().updateQuery("" +
							"DELETE FROM inventory " +
							"WHERE user_id = "+userId+" AND "+
							"item_id = "+itemId+" AND " +
							"quantity = 0");
					player.sendMessage(Material.getMaterial(blockId)+": Previous: "+prevQuantity+" Now: "+leftover);
					
					
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void disconnect() {
		if(this.isConnected())
			DBToolKit.getInstance().close();		
	}
	public void listKits(Player player) {
		int userId = getUserDatabaseId(player);
		int numKits = 0;
		try {
			numKits = DBToolKit.getInstance().selectQuery("" +
					"SELECT name FROM kit_master WHERE user_id = " + userId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(numKits > 0){
			String kits = "Your kits: ";
			for(int i=0; i< numKits; i++)
				kits += DBToolKit.getInstance().get_value(i, 0)+", ";
			kits = kits.substring(0, kits.length()-2);
			player.sendMessage(kits);			
		}
	}
	public void withdrawKit(Player player, String kitName) {
		int userId = getUserDatabaseId(player);
		try {
			int kitExists = DBToolKit.getInstance().selectQuery("" +
					"SELECT kit.item_id, kit.stacks FROM kit, kit_master " +
					"WHERE kit_master.user_id = "+userId+" AND " +
					"kit_master.name = '"+kitName+"' AND " +
					"kit_master.id = kit.kit_id");
			if(kitExists > 0){
				int [] item_ids = new int[kitExists];
				int [] item_counts = new int[kitExists];
				for(int i=0; i < kitExists; i++){
					item_ids[i] = Integer.parseInt(DBToolKit.getInstance().get_value(i, 0));
					item_counts[i] = Integer.parseInt(DBToolKit.getInstance().get_value(i, 1));
				}
				for(int i=0; i < kitExists; i++){
					int blockId = getBlockId(item_ids[i]);
					withdrawItem(player, blockId, item_counts[i]);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public int getBalance(String player) {
		// TODO test if this works as an int, the balance seems to be have a decimal
		try {
			int balanceExists = DBToolKit.getInstance().selectQuery("" +
					"SELECT balance FROM iconomy WHERE username = " + user + ";");
			if(balanceExists > 0){
				return Integer.parseInt(DBToolKit.getInstance().get_value(0, 0));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public int setBalance(String user, int balance) {
		// TODO again not sure if int is right, and also there may be better iconomy methods for this
		try {
			DBToolKit.getInstance().updateQuery("" +
					"UPDATE iconomy SET balance = " + balance + " " +
					"WHERE username = " + user + ";");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void buyItem(Player player, int blockId, int amount) {
		// idea is to just buy cheapest available up to amount desired limited by money available
		// this could maybe be improved by using item stacks instead
		int userId = getUserDatabaseId(player);
		int itemId = getItemId(blockId);
		int currentBalance = getBalance(player.name());
		try {
			// first we want to know how much of this item the buyer already has, if any
			int supply = 0;
			int currentSupply = DBToolKit.getInstance().selectQuery("" +
					"SELECT quantity FROM inventory i " +
					"WHERE user_id = " + userId + " AND " +
					"item_id = " + itemId + ";");
			if(currentSupply > 0) {
				supply = Integer.parseInt(DBToolKit.getInstance().get_value(0, 0));
			}
			// now keep buying until buyer is satisfied
			while(amount > 0) {
				int lowestPrice = DBToolKit.getInstance().selectQuery("" +
						"SELECT seller_id, stock, MIN(price) FROM store " +
						"WHERE item_id = " + itemId + " " +
						"GROUP BY 1,2 ORDER BY 3 LIMIT 1;");
				if(lowestPrice > 0) {
					int seller = Integer.parseInt(DBToolKit.getInstance().get_value(0, 0));
					int stock = Integer.parseInt(DBToolKit.getInstance().get_value(0, 1));
					int price = Integer.parseInt(DBToolKit.getInstance().get_value(0, 2));
					String sellerName = getUserName(seller);
					int sellerBalance = getBalance(sellerName);
					i
					if(stock > amount) {
						int bought = amount; // buy what you need
					} else {
						int bought = stock; // buy what you can
					}
					// first check that buyer can afford and update money
					int cost = bought * price;
					if(cost < currentBalance) {
						currentBalance -= cost;
						sellerBalance += cost;
					} else {
						player.sendMessage("Cannot afford " + Material.getMaterial(blockId).toString() + "!");
						break; // not 100% sure this will get all the way out of the while, maybe return?
					}
					setBalance(player.name(), currentBalance);
					setBalance(sellerName, sellerBalance);
					// then actually update store/inventory
					supply += bought;
					stock -= bought;
					DBToolKit.getInstance().updateQuery("" +
							"UPDATE inventory SET quantity = " + supply + " " +
							"WHERE user_id = " + userId + " AND " +
							"item_id = " + itemId + ";");						
					DBToolKit.getInstance().updateQuery("" +
							"UPDATE store SET stock = " + stock + " " +
							"WHERE seller_id=" + seller + " AND " +
							"item_id=" + itemId + ";");
					player.sendMessage("Bought %d of " + Material.getMaterial(blockId).toString(), bought);
					amount -= bought;
					}
				}
				else {
					player.sendMessage("No more " + Material.getMaterial(blockId).toString() + " available!");
					amount = 0; // maybe just break instead?
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void sellItem(Player player, int blockId, int quantityPer,
			int price, int amount) {
		int userId = getUserDatabaseId(player);
		
		//First remove from user's inventory
		try {
			int hasItem = DBToolKit.getInstance().selectQuery("" +
					"SELECT inventory.quantity, item.id FROM inventory, item " +
					"WHERE item.block_id = "+blockId+" AND " +
					"item.id = inventory.item_id AND " +
					"inventory.user_id = "+ userId);
			if(hasItem > 0){
				int currentQuantity = Integer.parseInt(DBToolKit.getInstance().get_value(0, 0));
				int itemId = Integer.parseInt(DBToolKit.getInstance().get_value(0, 1));
				if(currentQuantity < amount){
					//SELL ALL
					player.sendMessage("Selling all "+currentQuantity+" "+Material.getMaterial(blockId).toString()+" from your inventory.");
					amount = currentQuantity;
					DBToolKit.getInstance().updateQuery("" +
							"DELETE FROM inventory " +
							"WHERE user_id = "+userId+" AND "+
							"item_id = "+itemId);
				}
				else{
					//SELL SOME
					int newQuantity = currentQuantity - amount;
					player.sendMessage(Material.getMaterial(blockId)+": Previous: "+currentQuantity+" Now: "+newQuantity);
					//TODO Adjust user's inventory here
				}
				//TODO add item to store table here
				
			}
			else{
				player.sendMessage("Please add some "+Material.getMaterial(blockId).toString()+" in your inventory bank first.");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}


}
