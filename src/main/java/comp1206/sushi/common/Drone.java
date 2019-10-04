package comp1206.sushi.common;

import java.util.ArrayList;
import java.util.List;

import comp1206.sushi.server.Server;
import comp1206.sushi.server.StockManager;

public class Drone extends Model implements Runnable{

	private Number speed;
	private Number progress;
	
	private Number capacity;
	private Number weightCarried;
	
	private List<Model> carriedItems;
	
	private Number battery;
	private boolean alive;
	
	private String status;
	
	private Postcode source;
	private Postcode destination;
	private DroneTripHandler handler;
	
	Server server;
	StockManager manager;
	
	private Number tripTotal;	

	public Drone(Number capacity, Number speed, Server server) {
		this.handler = (new DroneTripHandler(this));
		this.setSpeed(speed);
		this.setCapacity(capacity);
		this.setBattery(100);
		this.manager = server.getStockManager();
		this.server = server;
		this.source = server.getRestaurantPostcode();
		this.destination = server.getRestaurantPostcode();
		this.carriedItems = new ArrayList<>();
		this.setWeightCarried(0);
		this.setTripTotal(0.0);
		this.progress = null;
		this.setStatus("Idle");
		this.alive = true;
	}

	public Number getSpeed() {
		return speed;
	}

	
	public Number getProgress() {
		return progress;
	}
	
	public void setProgress(Number progress) {
		notifyUpdate("progress", this.progress, progress);
		this.progress = progress;
		
	}
	
	public void setSpeed(Number speed) {
		notifyUpdate("speed", this.speed, speed);
		this.speed = speed;
	}
	
	@Override
	public String getName() {
		return "Drone (" + getSpeed() + " speed)";
	}

	public Postcode getSource() {
		return source;
	}

	public void setSource(Postcode source) {
		notifyUpdate("source", this.source, source);
		this.source = source;
	}

	public Postcode getDestination() {
		return destination;
	}

	public void setDestination(Postcode destination) {
		notifyUpdate("destination", this.destination, destination);
		this.destination = destination;
	}

	public void killMePlease()
	{
		this.alive = false;
	}
	
	public Number getCapacity() {
		return capacity;
	}

	public void setCapacity(Number capacity) {
		notifyUpdate("capacity", this.capacity, capacity);
		this.capacity = capacity;
	}

	public Number getBattery() {
		return battery;
	}

	public void setBattery(Number battery) {
		notifyUpdate("battery", this.battery, battery);
		this.battery = battery;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}
	
	public StockManager getStockManager()
	{
		return this.manager;
	}
	
	public Number getWeightCarried()
	{
		return weightCarried;
	}
	
	public void setWeightCarried(Number weight)
	{
		if ( weight.intValue() > this.getCapacity().intValue()) throw new IllegalArgumentException("Can't carry more than drone capacity!");
		notifyUpdate("weightCarried",this.weightCarried,weight);
		this.weightCarried = weight; 
	}
	
	public Number getTripTotal()
	{
		return this.tripTotal;
	}
	
	public void setTripTotal(Number total)
	{
		notifyUpdate("tripTotal",this.tripTotal,total);
		this.tripTotal = total;
	}
	
	public List<Model> getCarriedItems()
	{
		return this.carriedItems;
	}
	
	public void setCarriedItems(List<Model> carriedItems)
	{
		notifyUpdate("carriedItems",this.carriedItems,carriedItems);
		this.carriedItems = carriedItems;
	}

	@Override
	public void run() {
		while(alive)
		{
			
			try {
				Thread.sleep(100);
				handler.decideNextMove();
			} catch (InterruptedException e) {
			} catch (IllegalArgumentException e) { 
				e.printStackTrace();
				break;
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}

	public void returnToBase()
	{
		handler.returnToBase();
	}
}

class DroneTripHandler {
	
	Drone drone;
	
	public DroneTripHandler(Drone drone)
	{
		this.drone = drone;
	}
	
	private void carryItem(Model item)
	{
		if (item instanceof Ingredient)
		{
			addIngredientBatchWeight((Ingredient) item);
		}
		
		else if (item instanceof Order)
		{
			addOrderWeight((Order) item);
		}
		
		List<Model> newCarriedItems = drone.getCarriedItems();
		newCarriedItems.add(item);
		drone.setCarriedItems(newCarriedItems);
		
	}
	
	private void dumpOrder(Order order)
	{
		List<Model> newCarriedItems = drone.getCarriedItems();
		newCarriedItems.remove(order);
		drone.setCarriedItems(newCarriedItems);
		removeOrderWeight(order);
	}
	
	private List<Ingredient> buildIngListFromModelList(List<Model> modelList)
	{
		List<Ingredient> ingList = new ArrayList<>();
		for (Model item : modelList)
		{
			if (item instanceof Ingredient)
			{
				ingList.add((Ingredient) item);
			}
			else throw new IllegalArgumentException("Item in the list is not an ingredient!");
		}
		return ingList;
	}
	
	private void dumpIngredientsAtRestaurant()
	{
		synchronized(drone.manager.getIngredients())
		{
			for (Ingredient ing: buildIngListFromModelList(drone.getCarriedItems()))
			{
				drone.manager.increaseStock(ing, ing.getRestockAmount());
			}
		}
		drone.setCarriedItems(new ArrayList<>());
		drone.setWeightCarried(0);
	}
	
	
	public void pickupIngredientBatch(Ingredient ing)
	{
		System.out.println(drone.getName() + ": will pick up ingredient batch " + ing.getName());
		synchronized(drone.manager.getIngredients())
		{
			ing.setRestockNeed(false);
		}
		System.out.println(drone.getName() + ": Picking up " + ing.getName());
		flyFullTrip(ing.getSupplier().getPostcode());
		carryItem(ing);
	}
	
	private void addOrderWeight(Order order)
	{
		drone.setWeightCarried( drone.getWeightCarried().intValue() + order.getWeight().intValue());
	}
	
	private void addIngredientBatchWeight(Ingredient ing)
	{
		drone.setWeightCarried(drone.getWeightCarried().intValue() +  ing.getWeight().intValue() *  ing.getRestockAmount().intValue());
	}
	
	private void removeOrderWeight(Order order)
	{
		drone.setWeightCarried(drone.getWeightCarried().intValue() - order.getWeight().intValue());
	}
	
	void returnToBase()
	{
		System.out.println("Returning to base");
		flyFullTrip(drone.server.getRestaurantPostcode());
		dumpIngredientsAtRestaurant();
	}
	
	private void flyFullTrip(Postcode destination)
	{
		beginNewTrip(destination);
		while (drone.getProgress().intValue() < 100)
		{
			travel(1);
		}
		drone.setStatus("Idle");
		drone.setProgress(null);
	}
	
	private void beginNewTrip(Postcode destination)
	{
		//  previous destination now the source
		drone.setSource(drone.getDestination());
		
		//  new destination set
		drone.setDestination(destination);
		
		//  reset progress
		drone.setProgress(0);
		
		//setting status
		drone.setStatus("Flying from " + drone.getSource().getName() + " to " + destination.getName());
		
		drone.setTripTotal(drone.getSource().calculateDistanceToPostcode(drone.getDestination())); 
	}
	
	private void travel(int seconds)
	{
		try {
			Thread.sleep(1000 * seconds);
		} catch (InterruptedException e) {}
		increaseProgress(seconds);
	}
	
	private void increaseProgress(int seconds)
	{
		double progressMade = ((drone.getSpeed().doubleValue()*seconds)/drone.getTripTotal().doubleValue())*100;
		double newProgress = drone.getProgress().doubleValue() + progressMade;
		
		if (newProgress >= 100)
		{
			drone.setProgress(100);
		}
		else {
			drone.setProgress(newProgress);
		}
		
	}
	
	private void deliverOrderFromRestaurant(Order order)
	{
		
		order.setDeliveryNeed(false);
		System.out.println(drone.getName() + ": Delivering order " + order.getName());
		carryItem(order);
		order.setStatus("Being delivered");
		flyFullTrip(order.getCustomer().getPostcode());
		order.setStatus("Delivered");
		dumpOrder(order);
	}
	
	private boolean canCarryOrder(Order order)
	{
		if(drone.getCapacity().intValue() >= drone.getWeightCarried().intValue() + order.getWeight().intValue()) return true;
		else return false;
	}
	
	private boolean canCarryBatch(Ingredient ingredient)
	{
		if( drone.getCapacity().intValue() >= drone.getWeightCarried().intValue() + ingredient.getBatchWeight().intValue()) return true;
		else return false;
	}
	
	private Ingredient getIngredientToRestock()
	{
		synchronized(drone.manager.getIngredients())
		{
			for (Ingredient ing : drone.manager.getIngredientsNeedingRestock())
			{
				if (canCarryBatch(ing)) return ing;
			}
			throw new IllegalArgumentException("Currently can't carry a batch of any ingredients needing a restock!");
		}
	}
	
	private boolean canRestockIngredientBatch()
	{
		try {
			getIngredientToRestock();
		} catch (IllegalArgumentException e) {
			return false;
		}
		
		return true;
	}
	
	private boolean canCarryAnOrder()
	{
		try {
			getOrderToDeliver();
		} catch (IllegalArgumentException e) {
			return false;
		}
		
		return true;
	}
	
	private Order getOrderToDeliver()
	{
		for (Order order: drone.server.getPendingOrders())
		{
			if (canCarryOrder(order)) return order;
		}
		throw new IllegalArgumentException("Currently can't carry any of the pending orders!");
	}
	
	void decideNextMove() throws InterruptedException
	{
		if (drone.getProgress() == null)
		{
			if (drone.getDestination().equals(drone.server.getRestaurantPostcode()))
			{
				System.out.println(drone.getName() + ": at restaurant");
				decideNextMoveWhileAtRestaurant();
			}
			else 
			{
				decideNextMoveWhileAway();
			}
		}
		else throw new IllegalArgumentException("This method should not be called unless progress is null!");
	}
	
	private void decideNextMoveWhileAway()
	{
		Ingredient ing = null;
		synchronized(drone.getStockManager().getIngredients())
		{
			if (canRestockIngredientBatch())
			{
				ing = getIngredientToRestock();
				ing.setRestockNeed(false);
			}
		}
		
		if (ing != null)
		{
			pickupIngredientBatch(ing);
		}
		else {
			returnToBase();
		}
	}
	
	private void decideNextMoveWhileAtRestaurant() throws InterruptedException
	{
		if (( Integer.valueOf(drone.getWeightCarried().intValue())).equals(Integer.valueOf(0)))
		{
			System.out.println(drone.getName() + ": no weight being carried");
			makeDecisionFromRestaurant();
		} 
		else throw new IllegalArgumentException("Drone should have dumped all of its contents at the restaurant!");
	}
	
	private void makeDecisionFromRestaurant() throws InterruptedException
	{
		Ingredient ingToRestock = null;
		Order orderToDeliver = null;
		
		synchronized(drone.getStockManager().getIngredients()) {
			
			if (drone.manager.getIngredientsNeedingRestock().size() > 0)
			{
				if (canRestockIngredientBatch())
				{
					System.out.println(drone.getName() + ": Gonna pick up ing batch");
					ingToRestock = getIngredientToRestock();
					ingToRestock.setRestockNeed(false);
				}
			}
		}
		
		if (ingToRestock != null)
		{
			pickupIngredientBatch(ingToRestock);
		}
		
		else 
		{
			synchronized(drone.server.getOrders())
			{
				System.out.println(drone.getName() + ": will check orders");
				orderToDeliver = checkOrderDeliveryFromRestaurant();
			}
			
			if (orderToDeliver != null)
			{
				System.out.println(drone.getName() + ": will deliver order " + orderToDeliver.getName());
				deliverOrderFromRestaurant(orderToDeliver);
			}
			
			else 
			{
				System.out.println(drone.getName() + ": decided nothing lmao");
				synchronized (drone.getStockManager().getIngredients()) {
					drone.getStockManager().getIngredients().wait();
				}
			}
		}
	}
	
	private Order checkOrderDeliveryFromRestaurant() throws InterruptedException
	{
		if (drone.server.getPendingOrders().size() > 0)
		{
			System.out.println(drone.getName() + ": There are pending orders");
			if (canCarryAnOrder())
			{
				Order order = getOrderToDeliver();
				order.setDeliveryNeed(false);
				return order;
			}
		}
		System.out.println(drone.getName() + ": No orders need delivery");
		return null;
	}
}
