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
			player.sendMessage("Metadata found: "+itemMeta.toString());
		}
		else
			player.sendMessage("Material data not found.");
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

}
