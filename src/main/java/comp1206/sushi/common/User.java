package comp1206.sushi.common;

import java.util.HashMap;
import java.util.Map;

public class User extends Model {
	
	private String name;
	private String password;
	private String address;
	private Postcode postcode;
	private Map<Dish, Number> orderBasket = new HashMap<>();

	public User(String username, String password, String address, Postcode postcode) {
		this.name = username;
		this.password = password;
		this.address = address;
		this.postcode = postcode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.notifyUpdate("name", this.name, name);
		this.name = name;	
	}

	public Number getDistance() {
		return postcode.getDistance();
	}

	public Postcode getPostcode() {
		return this.postcode;
	}
	
	public void setPostcode(Postcode postcode) {
		this.notifyUpdate("postcode", this.postcode, postcode);
		this.postcode = postcode;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public Map<Dish, Number> getBasket() {
		return this.orderBasket;
	}
	
	public void addEntryToBasket(Dish dish, Number quantity)
	{
		Map<Dish, Number> oldBasket = this.orderBasket;
		this.orderBasket.put(dish, quantity);	
		this.notifyUpdate("orderBasket", oldBasket, this.orderBasket);
	}
	
	public void removeDishFromBasket(Dish dish)
	{
		Map<Dish, Number> oldBasket = this.orderBasket;
		this.orderBasket.remove(dish);	
		this.notifyUpdate("orderBasket", oldBasket, this.orderBasket);
	}
	
	public void resetBasket() {
		Map<Dish, Number> empty = new HashMap<Dish,Number>();
		this.notifyUpdate("orderBasket", this.orderBasket, empty);
		this.orderBasket = empty;
		
	}
	
	public Number getBasketCost()
	{
		Integer cost = 0;
		for (Map.Entry<Dish, Number> entry: orderBasket.entrySet())
		{
			Integer dishPrice = (Integer) entry.getKey().getPrice();
			Integer quantity = (Integer) entry.getValue();
			cost += dishPrice*quantity;
		}
		return cost;
	}
	
}
