package comp1206.sushi.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import comp1206.sushi.common.*;

public class Configuration {
	
	String filename;
	BufferedReader reader;
	Server server;
	
	public Configuration (String name, Server server) throws FileNotFoundException, InvalidConfigException
	{
		this.filename = name;
		this.reader = new BufferedReader(new FileReader(name));
		this.server = server;
		this.processConfigFile();
	}
	
	public void processConfigFile() throws InvalidConfigException{
		String currLine;
		int currLineNumber = 0;
		try {
			while((currLine  = reader.readLine()) != null)
			{
				currLineNumber++;
				processLine(currLine,currLineNumber);
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new InvalidConfigException(currLineNumber);
		}
	}
	
	public void parseRestaurant(String[] parts) 
	{
		server.setRestaurant(new Restaurant(parts[1],server.findPostcodeByName(parts[2])));
	}
	
	public void parsePostcode(String[] parts)
	{
		server.addPostcode(parts[1]);
	}
	
	public void parseSupplier(String[] parts)
	{
		server.addSupplier(parts[1],server.findPostcodeByName(parts[2]));
	}
	
	public void parseIngredient(String[] parts)
	{
		server.addIngredient(parts[1], parts[2], server.findSupplierByName(parts[3]), 
				Integer.valueOf(parts[4]), Integer.valueOf(parts[5]), Integer.valueOf(parts[6]));
	}
	
	public void parseDish(String[] parts)
	{
		server.addDish(parts[1], parts[2], Integer.valueOf(parts[3]), Integer.valueOf(parts[4]), Integer.valueOf(parts[5]));
		Dish newDish = server.findDishByName(parts[1]);
		parseNewDishRecipe(newDish, parts[6]);
	}
	
	private void parseNewDishRecipe(Dish newDish, String listing) {
		HashMap<String, Integer> recipe = parseMapFromList(listing);
		recipe.forEach((key,value) -> server.addIngredientToDish(newDish, server.findIngredientByName(key), value));
	}

	public HashMap<String,Integer> parseMapFromList(String list)
	{
		HashMap<String,Integer> parsedList = new HashMap<>();
		String[] entries = list.split(",");
		
		for (String entry: entries)
		{
			Integer quantity = Integer.valueOf(entry.split("\\*")[0].substring(0,entry.split("\\*")[0].length() - 1));
			String name = entry.split("\\*")[1].substring(1);
			parsedList.put(name, quantity);
		}
		
		return parsedList;
	}
	
	public void parseUser(String[] parts)
	{
		server.addUser(parts[1],parts[2],parts[3],server.findPostcodeByName(parts[4]));
	}
	
	public void parseStaff(String[] parts)
	{
		server.addStaff(parts[1]);
	}
	
	public void parseDrone(String[] parts)
	{
		server.addDrone(Integer.valueOf(parts[1]));
	}
	
	public void parseOrder(String[] parts)
	{
		server.placeOrder(server.findUserByName(parts[1]), parseOrderMapFromGenericMap(parseMapFromList(parts[2])));
	}
	
	private Map<Dish, Number> parseOrderMapFromGenericMap(HashMap<String, Integer> generic) 
	{
		Map<Dish, Number> orderContents = new HashMap<>();
		generic.forEach((dishName,number) -> orderContents.put(server.findDishByName(dishName), number));
		return orderContents;
	}
	
	private void parseStock(String[] parts)
	{
		if (server.isDishName(parts[1]))
		{
			server.setStock(server.findDishByName(parts[1]), Integer.valueOf(parts[2]));
		}
		
		else if (server.isIngredientName(parts[1]))
		{
			server.setStock(server.findIngredientByName(parts[1]), Integer.valueOf(parts[2]));
		}
	}
	
	private void processLine(String currLine, int lineNumber) throws InvalidConfigException
	{
		if (!currLine.equals(""))
		{
			String[] parts = currLine.split(":");
			String id = parts[0];
			switch (id) {
				case("RESTAURANT"):
					parseRestaurant(parts);
					break;
				case("POSTCODE"):
					parsePostcode(parts);
					break;
				case("SUPPLIER"):
					parseSupplier(parts);
					break;
				case("INGREDIENT"):
					parseIngredient(parts);
					break;
				case("DISH"):
					parseDish(parts);
					break;
				case("USER"):
					parseUser(parts);
					break;
				case("STAFF"):
					parseStaff(parts);
					break;
				case("DRONE"):
					parseDrone(parts);
					break;
				case("ORDER"):
					parseOrder(parts);
					break;
				case("STOCK"):
					parseStock(parts);
					break;
				default:
					throw new InvalidConfigException(lineNumber);				
			}
		}
	}
}

@SuppressWarnings("serial")
class InvalidConfigException extends Exception 
{
	public InvalidConfigException(int currLine)
	{
		super("Config file format invalid! Error in line " + currLine);
	}
}
