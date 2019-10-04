package comp1206.sushi.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import comp1206.sushi.common.Comms;
import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Drone;
import comp1206.sushi.common.Ingredient;
import comp1206.sushi.common.Order;
import comp1206.sushi.common.Postcode;
import comp1206.sushi.common.Restaurant;
import comp1206.sushi.common.Staff;
import comp1206.sushi.common.Supplier;
import comp1206.sushi.common.UpdateEvent;
import comp1206.sushi.common.UpdateListener;
import comp1206.sushi.common.User;
 
public class Server implements ServerInterface , Serializable {

    private static final Logger logger = LogManager.getLogger("Server");
	
	public Restaurant restaurant;
	public ArrayList<Dish> dishes = new ArrayList<Dish>();
	public ArrayList<Drone> drones = new ArrayList<Drone>();
	public ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
	public ArrayList<Order> orders = new ArrayList<Order>();
	public ArrayList<Staff> staff = new ArrayList<Staff>();
	public ArrayList<Supplier> suppliers = new ArrayList<Supplier>();
	public ArrayList<User> users = new ArrayList<User>();
	public ArrayList<Postcode> postcodes = new ArrayList<Postcode>();
	private ArrayList<UpdateListener> listeners = new ArrayList<UpdateListener>();
	private StockManager manager = new StockManager();
	private Comms comms = new Comms(this);

	
	public Server() {
        logger.info("Starting up server...");
		
		Postcode restaurantPostcode = new Postcode("SO17 1BJ");
		restaurant = new Restaurant("Mock Restaurant",restaurantPostcode);

	}

	public void sendMessage(Serializable message)
	{
		comms.sendMessage(message);
	}
	
	public void receiveMessage(Object message)
	{
		if (message instanceof Order)	
		{
			Order receivedOrder = (Order) message;
			if (isOrderName(receivedOrder.getName()))
			{
				removeOrder(findOrderByName(receivedOrder.getName()));
			}
			else addOrder(receivedOrder);
		}
		else if (message instanceof User)
		{
			User receivedUser = (User) message;
			if (isUserName(receivedUser.getName()))
			{
				removeUser(findUserByName(receivedUser.getName()));
			}
			else addUser(receivedUser);
		}
		else throw new IllegalArgumentException("Invalid object type received from client!");
	}
	
	@Override
	public List<Dish> getDishes() {
		return this.dishes;
	}

	@Override
	public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
		Dish newDish = new Dish(name,description,price,restockThreshold,restockAmount);
		this.dishes.add(newDish);
		manager.addNew(newDish);
		this.notifyUpdate();
		sendMessage(newDish);
		return newDish;
	}
	
	@Override
	public void removeDish(Dish dish) {
		this.dishes.remove(dish);
		manager.remove(dish);
		this.notifyUpdate();
		sendMessage(dish);
	}

	@Override
	public Map<Dish, Number> getDishStockLevels() {
		return manager.getDishes();
	}
	
	@Override
	public void setRestockingIngredientsEnabled(boolean enabled) {
		manager.setIngredientRestockingEnabled(enabled);
	}

	@Override
	public void setRestockingDishesEnabled(boolean enabled) {
		manager.setDishRestockingEnabled(enabled);
	}
	
	@Override
	public void setStock(Dish dish, Number stock) {
		manager.setStock(dish, stock);
		this.notifyUpdate();
	}

	@Override
	public void setStock(Ingredient ingredient, Number stock) {
		manager.setStock(ingredient, stock);
		this.notifyUpdate();
	}

	@Override
	public List<Ingredient> getIngredients() {
		return this.ingredients;
	}

	@Override
	public Ingredient addIngredient(String name, String unit, Supplier supplier,
			Number restockThreshold, Number restockAmount, Number weight) {
		Ingredient mockIngredient = new Ingredient(name,unit,supplier,restockThreshold,restockAmount,weight);
		this.ingredients.add(mockIngredient);
		this.manager.addNew(mockIngredient);
		this.notifyUpdate();
		return mockIngredient;
	}

	@Override
	public void removeIngredient(Ingredient ingredient) {
		int index = this.ingredients.indexOf(ingredient);
		this.ingredients.remove(index);
		this.manager.remove(ingredient);
		this.notifyUpdate();
	}
	
	@Override
	public List<Supplier> getSuppliers() {
		return this.suppliers;
	}

	@Override
	public Supplier addSupplier(String name, Postcode postcode) {
		Supplier mock = new Supplier(name,postcode);
		this.suppliers.add(mock);
		this.notifyUpdate();
		return mock;
	}


	@Override
	public void removeSupplier(Supplier supplier) {
		int index = this.suppliers.indexOf(supplier);
		this.suppliers.remove(index);
		this.notifyUpdate();
	}

	@Override
	public List<Drone> getDrones() {
		return this.drones;
	}

	@Override
	public Drone addDrone(Number speed) {
		Drone mock = new Drone(1000,speed,this);
		Thread droneThread = new Thread(mock);
		this.drones.add(mock);
		droneThread.start();
		System.out.println("Drone created, thread started");
		this.notifyUpdate();
		return mock;
	}
	
	public Drone addDrone(Number capacity, Number speed) {
		Drone mock = new Drone(capacity,speed,this);
		Thread droneThread = new Thread(mock);
		this.drones.add(mock);
		droneThread.start();
		System.out.println("Drone created, thread started");
		this.notifyUpdate();
		return mock;
	}

	@Override
	public void removeDrone(Drone drone) {
		drone.killMePlease();
		int index = this.drones.indexOf(drone);
		
		this.drones.remove(index);
		this.notifyUpdate();
	}

	@Override
	public List<Staff> getStaff() {
		return this.staff;
	}

	@Override
	public Staff addStaff(String name) {
		Staff mock = new Staff(name,manager);
		Thread staffThread = new Thread(mock);
		this.staff.add(mock);
		staffThread.start();
		System.out.println("Staff member created, thread started");
		this.notifyUpdate();
		return mock;
	}

	@Override
	public void removeStaff(Staff staff) {
		this.staff.remove(staff);
		staff.killMe();
		this.notifyUpdate();
	}

	@Override
	public List<Order> getOrders() {
		return this.orders;
	}

	@Override
	public void removeOrder(Order order) {
		this.orders.remove(order);
		this.notifyUpdate();
	}
	
	public void addOrder(Order order) 
	{
		this.orders.add(order);
		synchronized (manager.getIngredients()) {
			manager.getIngredients().notify();
		}
		this.notifyUpdate();
	}
	
	@Override
	public Number getOrderCost(Order order) {
		return order.getCost();
	}

	@Override
	public Map<Ingredient, Number> getIngredientStockLevels() {
		return manager.getIngredients();
	}

	@Override
	public Number getSupplierDistance(Supplier supplier) {
		return supplier.getDistance();
	}

	@Override
	public Number getDroneSpeed(Drone drone) {
		return drone.getSpeed();
	}

	@Override
	public Number getOrderDistance(Order order) {
		Order mock = (Order)order;
		return mock.getDistance();
	}

	@Override
	public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
		dish.addIngredientToRecipe(ingredient, quantity);
	}

	@Override
	public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
		dish.removeIngredientFrommRecipe(ingredient);
	}

	@Override
	public Map<Ingredient, Number> getRecipe(Dish dish) {
		return dish.getRecipe();
	}

	@Override
	public List<Postcode> getPostcodes() {
		return this.postcodes;
	}

	@Override
	public Postcode addPostcode(String code) {
		Postcode mock = new Postcode(code,this.getRestaurant());
		this.postcodes.add(mock);
		this.notifyUpdate();
		sendMessage(mock);
		return mock;
	}

	@Override
	public void removePostcode(Postcode postcode) throws UnableToDeleteException {
		this.postcodes.remove(postcode);
		this.notifyUpdate();
		sendMessage(postcode);
	}

	public User addUser(String username, String password, String address, Postcode postcode) {
		User newUser = new User(username,password,address,postcode);
		this.users.add(newUser);
		this.notifyUpdate();
		sendMessage(newUser);
		return newUser;
	}
	
	@Override
	public List<User> getUsers() {
		return this.users;
	}
	
	@Override
	public void removeUser(User user) {
		this.users.remove(user);
		this.notifyUpdate();
		sendMessage(user);
	}

	private void addUser(User user)
	{
		this.users.add(user);
		this.notifyUpdate();
		sendMessage(user);
	}
	
	@Override
	public void loadConfiguration(String filename) {
		
		clearContents();
		System.out.println("Loaded configuration: " + filename);
		try {
			@SuppressWarnings("unused")
			Configuration config = new Configuration("config.txt", this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
		for(Entry<Ingredient, Number> recipeItem : recipe.entrySet()) {
			addIngredientToDish(dish,recipeItem.getKey(),recipeItem.getValue());
		}
		this.notifyUpdate();
	}

	//TODO IMPLEMENT
	@Override
	public boolean isOrderComplete(Order order) {
		return true;
	}

	@Override
	public String getOrderStatus(Order order) {
		return order.getStatus();
	}
	
	public void setOrderStatus(Order order, String status)
	{
		order.setStatus(status);
		this.notifyUpdate();
		this.sendMessage(order);
	}
	
	@Override
	public String getDroneStatus(Drone drone) {
		return drone.getStatus();
	}
	
	@Override
	public String getStaffStatus(Staff staff) {
		return staff.getStatus();
	}

	@Override
	public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
		
		synchronized(manager.getDishes())
		{
			dish.setRestockThreshold(restockThreshold);
			dish.setRestockAmount(restockAmount);
			manager.getDishes().notify();
		}
		this.notifyUpdate();
	}

	@Override
	public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
		ingredient.setRestockThreshold(restockThreshold);
		ingredient.setRestockAmount(restockAmount);
		this.notifyUpdate();
	}

	@Override
	public Number getRestockThreshold(Dish dish) {
		return dish.getRestockThreshold();
	}

	@Override
	public Number getRestockAmount(Dish dish) {
		return dish.getRestockAmount();
	}

	@Override
	public Number getRestockThreshold(Ingredient ingredient) {
		return ingredient.getRestockThreshold();
	}

	@Override
	public Number getRestockAmount(Ingredient ingredient) {
		return ingredient.getRestockAmount();
	}

	@Override
	public void addUpdateListener(UpdateListener listener) {
		this.listeners.add(listener);
	}
	
	@Override
	public void notifyUpdate() {
		this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
	}

	@Override
	public Postcode getDroneSource(Drone drone) {
		return drone.getSource();
	}

	@Override
	public Postcode getDroneDestination(Drone drone) {
		return drone.getDestination();
	}

	@Override
	public Number getDroneProgress(Drone drone) {
		return drone.getProgress();
	}

	@Override
	public String getRestaurantName() {
		return restaurant.getName();
	}

	@Override
	public Postcode getRestaurantPostcode() {
		return restaurant.getLocation();
	}
	
	@Override
	public Restaurant getRestaurant() {
		return restaurant;
	}
	
	public void setRestaurant(Restaurant restaurant)
	{
		this.restaurant = restaurant;
		sendMessage(restaurant);
	}
	
	public Postcode findPostcodeByName(String name) throws IllegalArgumentException
	{
		for (Postcode code: this.getPostcodes())
		{
			if (code.getName().equals(name))
			{
				return code;
			}
		}
		throw new IllegalArgumentException("No postcodes with a matching name " + name + " found!");
	}
	
	public Supplier findSupplierByName(String name) throws IllegalArgumentException
	{
		for (Supplier supp: this.getSuppliers())
		{
			if (supp.getName().equals(name))
			{
				return supp;
			}
		}
		throw new IllegalArgumentException("No suppliers with a matching name " + name + " found!");
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
	
	public Ingredient findIngredientByName(String name) throws IllegalArgumentException
	{
		for (Ingredient ingredient: this.getIngredients())
		{
			if (ingredient.getName().equals(name))
			{
				return ingredient;
			}
		}
		throw new IllegalArgumentException("No ingredients with a matching name " + name + " found!");
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
	
	public Order findOrderByName(String name) throws IllegalArgumentException
	{
		for (Order order: this.getOrders())
		{
			if (order.getName().equals(name))
			{
				return order;
			}
		}
		throw new IllegalArgumentException("No orders with a matching name " + name + " found!");
	}
	
	public Order placeOrder(User user, Map<Dish, Number> contents)
	{
		Order mock = new Order(user,contents);
		this.orders.add(mock);
		synchronized (manager.getIngredients()) {
			manager.getIngredients().notify();
		}
		this.notifyUpdate();
		return mock;
	}
	
	public boolean isDishName(String name)
	{
		try {
			findDishByName(name);
		} catch (IllegalArgumentException e) {
			return false;
		}
		
		return true;
	}
	
	public boolean isIngredientName(String name)
	{
		try {
			findIngredientByName(name);
		} catch (IllegalArgumentException e) {
			return false;
		}
		
		return true;
	}
	
	public boolean isOrderName(String name) {
		try {
			findOrderByName(name);
		} catch (IllegalArgumentException e) {
			return false;
		}
		
		return true;
	}
	
	public boolean isUserName(String name) {
		try {
			findUserByName(name);
		} catch (IllegalArgumentException e) {
			return false;
		}
		
		return true;
		
	}
	
	public StockManager getStockManager()
	{
		return this.manager;
	}
	
	public List<Order> getPendingOrders()
	{
		synchronized(this.getOrders())
		{
			List<Order> pendingOrders = new ArrayList<>();
			for (Order order: this.getOrders())
			{
				if (order.getStatus().equals("Pending")) pendingOrders.add(order);
			}
			return pendingOrders;
		}
	}
	
	public boolean hasPendingOrders()
	{
		if (getPendingOrders().size() == 0) return false;
		else return true;
	}
	
	public void clearContents()
	{
		removeAllDishes();
		removeAllDrones();
		removeAllIngredients();
		removeAllOrders();
		removeAllStaff();
		removeAllSuppliers();
		removeAllUsers();
		removeAllPostcodes();
		
		this.manager.clearAll();
	}
	
	private void removeAllDishes()
	{
		while(getDishes().size() > 0)
		{
			removeDish(getDishes().get(0));
		}
	}
	
	private void removeAllDrones()
	{
		while(getDrones().size() > 0)
		{
			removeDrone(getDrones().get(0));
		}
	}
	
	private void removeAllIngredients()
	{
		while(getIngredients().size() > 0)
		{
			removeIngredient(getIngredients().get(0));
		}
	}
	
	private void removeAllOrders()
	{
		while(getOrders().size() > 0)
		{
			removeOrder(getOrders().get(0));
		}
	}
	
	private void removeAllStaff()
	{
		while(getStaff().size() > 0)
		{
			removeStaff(getStaff().get(0));
		}
	}
	
	private void removeAllSuppliers()
	{
		while(getSuppliers().size() > 0)
		{
			removeSupplier(getSuppliers().get(0));
		}
	}
	
	private void removeAllUsers()
	{
		while(getUsers().size() > 0)
		{
			removeUser(getUsers().get(0));
		}
	}
	
	private void removeAllPostcodes()
	{
		while(getPostcodes().size() > 0)
		{
			try {
				removePostcode(getPostcodes().get(0));
			} catch (UnableToDeleteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
