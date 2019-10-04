package comp1206.sushi.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import comp1206.sushi.common.Comms;
import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Order;
import comp1206.sushi.common.Postcode;
import comp1206.sushi.common.Restaurant;
import comp1206.sushi.common.UpdateEvent;
import comp1206.sushi.common.UpdateListener;
import comp1206.sushi.common.User;

public class Client implements ClientInterface {

    private static final Logger logger = LogManager.getLogger("Client");

	public Restaurant restaurant;
	public List<User> registeredUsers = new ArrayList<>();
	public List<Postcode> postcodes = new ArrayList<>();
	public List<Dish> dishes = new ArrayList<>();
	public List<Order> orders = new ArrayList<>();
	public List<UpdateListener> listeners = new ArrayList<>();
	private Comms comms = new Comms(this);

	
	public Client() {
        logger.info("Starting up client...");
        
        Postcode restaurantPostcode = new Postcode("SO17 1BJ");
		restaurant = new Restaurant("Mock Restaurant",restaurantPostcode);
		
        Postcode connaught = addPostcode("SO18 2NS");
        @SuppressWarnings("unused")
		Postcode monte = addPostcode("SO18 2NU");
        
        User usr1 = register("pepe","asd","lul",connaught);
        //User kaah = register("kaah1g18", "qwerty","oof",monte);        
        Dish dish = addDish("aa", "ss", 4, 5, 6);
        addDishToBasket(usr1, dish, 3);
        System.out.println("Does this even work?");
        System.out.println("reached this point");
	}
	
	//TODO implement sending and receiving messages
	public void sendMessage(Serializable message)
	{
		comms.sendMessage(message);
	}
	
	@SuppressWarnings("unchecked")
	public void receiveMessage(Object message)
	{
		if (message instanceof List<?>)
		{
			List<?> received = (List<?>) message;
			if (received.size() > 0)
			{
				if (received.get(0) instanceof Dish)
				{
					this.setDishes((List<Dish>) received);
				}
				
				else if (received.get(0) instanceof User)
				{
					this.setUsers((List<User>) received);
				}
				
				else if (received.get(0) instanceof Postcode)
				{
					this.setPostcodes((List<Postcode>) received);
				}
				
				else throw new IllegalArgumentException("Received list has invalid contents!");
			}
			else System.out.println("Empty list received from server!");
		}
		
		else if (message instanceof Restaurant)
		{
			this.setRestaurant((Restaurant) message); 
		}
		
		else if (message instanceof Dish)
		{
			Dish dish = (Dish) message;
			if (isExistingDish(dish))
			{
				Dish existingVersion = findDishByName(dish.getName());
				removeDish(existingVersion);
				
				if ((!existingVersion.getPrice().equals(dish.getPrice())) || (!existingVersion.getName().equals(dish.getName())) || (!existingVersion.getDescription().equals(dish.getDescription())))
					addDish(dish.getName(), dish.getDescription(), dish.getPrice(), dish.getRestockThreshold(), dish.getRestockAmount());
			}
			else {
				addDish(dish.getName(), dish.getDescription(), dish.getPrice(), dish.getRestockThreshold(), dish.getRestockAmount());
			}
		}
		
		else if (message instanceof Order)
		{
			Order order = (Order) message;
			
			if (isExistingOrder(order))
			{
				Order orderToEdit = findOrderByName(order.getName());
				orderToEdit.setStatus(order.getStatus());
				System.out.println("Order status ammended");
			}
		}
		
		else if (message instanceof Postcode)
		{
			Postcode pc = (Postcode) message;
			if (isExistingPostcode(pc))
			{
				removePostcode(findPostcodeByName(pc.getName()));
			}
			else addPostcode(pc.getName());
		}
		
		else if (message instanceof User)
		{
			User usr = (User) message;
			if (isExistingUser(usr))
			{
				removeUser(findUserByName(usr.getName()));
			}
			else addUser(usr);
		}
		
		
		
		else throw new IllegalArgumentException("Message received from server  has invalid type!");

	}
	
	@Override
	public Restaurant getRestaurant() {
		return this.restaurant;
	}
	
	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
		this.notifyUpdate();
	}
	
	@Override
	public String getRestaurantName() {
		return this.getRestaurant().getName();
	}

	@Override
	public Postcode getRestaurantPostcode() {
		return this.getRestaurant().getLocation();
	}
	
	@Override
	public User register(String username, String password, String address, Postcode postcode) {
		User newUsr = new User(username,password,address,postcode);
		registeredUsers.add(newUsr);
		System.out.println(newUsr.getName());
		sendMessage(newUsr);
		return newUsr;
	}

	@Override
	public User login(String username, String password) {
			User attempted = findUserByName(username);
			
			if (attempted.getPassword().equals(password))
			{
				return attempted;
			}
			else return null;
	}

	@Override
	public List<Postcode> getPostcodes() {
		return this.postcodes;
	}
	
	public void setPostcodes(List<Postcode> pcs) 
	{
		this.postcodes = pcs;
		this.notifyUpdate();
	}

	@Override
	public List<Dish> getDishes() {
		return this.dishes;
	}
	
	public void setDishes(List<Dish> dishes) 
	{
		this.dishes = dishes;
		this.notifyUpdate();
	}

	@Override
	public String getDishDescription(Dish dish) {
		return dish.getDescription();
	}

	@Override
	public Number getDishPrice(Dish dish) {
		return dish.getPrice();
	}

	@Override
	public Map<Dish, Number> getBasket(User user) {
		return user.getBasket();
	}

	@Override
	public Number getBasketCost(User user) {
		return user.getBasketCost();
	}

	@Override
	public void addDishToBasket(User user, Dish dish, Number quantity) {
		user.addEntryToBasket(dish, quantity);
		this.notifyUpdate();
	}

	@Override
	public void updateDishInBasket(User user, Dish dish, Number quantity) {
		user.removeDishFromBasket(dish);
		user.addEntryToBasket(dish, quantity);
		this.notifyUpdate();
	}

	@Override
	public Order checkoutBasket(User user) {
		Order newOrder = new Order(user, user.getBasket());
		this.orders.add(newOrder);
		user.resetBasket();
		this.notifyUpdate();
		sendMessage(newOrder);
		return newOrder;
	}

	@Override
	public void clearBasket(User user) {
		user.resetBasket();
		this.notifyUpdate();
	}

	@Override
	public List<Order> getOrders(User user) {
		List<Order> userOrders = new ArrayList<>();
		for (Order order: this.orders)
		{
			if (order.getCustomer() == user)
			{
				userOrders.add(order);
			}
		}
		return userOrders;	
	}
	
	public List<Order> getAllOrders()
	{
		return this.orders;
	}
	
	public Order findOrderByName(String name)
	{
		for (Order order: this.getAllOrders())
		{
			if (order.getName().equals(name))
			{
				return order;
			}
		}
		throw new IllegalArgumentException("No orders with a matching name " + name + " found!");
	}
	
	public boolean isExistingOrder(Order order)
	{
		try {
			findOrderByName(order.getName());
		} catch (IllegalArgumentException e) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean isOrderComplete(Order order) {
		return order.isComplete();
	}

	@Override
	public String getOrderStatus(Order order) {
		return order.getStatus();
	}

	@Override
	public Number getOrderCost(Order order) {
		return order.getCost();
	}

	@Override
	public void cancelOrder(Order order) {
		this.orders.remove(order);
		sendMessage(order);
		this.notifyUpdate();
	}
	
	@Override
	public void addUpdateListener(UpdateListener listener) {
		this.listeners.add(listener);
	}
	
	@Override
	public void notifyUpdate() {
		this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
	}

	
	public User findUserByName(String name) throws IllegalArgumentException
	{
		for (User user: this.getUsers())
		{
			if (user.getName().equals(name))
			{
				return user;
			}
		}
		throw new IllegalArgumentException("No users with a matching name " + name + " found!");
	}
	
	public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount)
	{
		Dish newDish = new Dish(name, description, price, restockThreshold, restockAmount);
		this.dishes.add(newDish);
		this.notifyUpdate();
		return newDish;
	}
	
	public void removeDish(Dish dish)
	{
		this.dishes.remove(dish);
		this.notifyUpdate();
	}
	
	public Dish findDishByName(String name) throws IllegalArgumentException
	{
		for (Dish dish: this.getDishes())
		{
			if (dish.getName().equals(name))
			{
				return dish;
			}
		}
		throw new IllegalArgumentException("No dishes with a matching name " + name + " found!");
	}
	
	public boolean isExistingDish(Dish dish)
	{
		try {
			findDishByName(dish.getName());
		} catch (IllegalArgumentException e) {
			return false;
		}
		
		return true;
	}
	
	public Postcode addPostcode(String code)
	{
		Postcode newpc = new Postcode(code,getRestaurant());
		this.postcodes.add(newpc);
		this.notifyUpdate();
		return newpc;
	}
	
	public void removePostcode(Postcode pc)
	{
		this.postcodes.remove(pc);
		this.notifyUpdate();
	}
	
	public Postcode findPostcodeByName(String name)
	{
		for(Postcode post: getPostcodes())
		{
			if (post.getName().equals(name)) return post;
		}
		
		throw new IllegalArgumentException("No postcodes with a matching name " + name + " found!");
	}
	
	public boolean isExistingPostcode(Postcode pc)
	{
		try {
			findPostcodeByName(pc.getName());
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
		
	}
	
	public List<User> getUsers()
	{
		return this.registeredUsers;
	}
	
	public void setUsers(List<User> users)
	{
		this.registeredUsers = users;
		this.notifyUpdate();
	}
	
	private void addUser(User user)
	{
		this.registeredUsers.add(user);
		this.notifyUpdate();
	}
	
	private void removeUser(User user)
	{
		this.registeredUsers.remove(user);
		this.notifyUpdate();
	}
	
	private boolean isExistingUser(User user)
	{
		try {
			findUserByName(user.getName());
		} catch (IllegalArgumentException e) {
			return false;
		}
		
		return true;
		
	}
}
