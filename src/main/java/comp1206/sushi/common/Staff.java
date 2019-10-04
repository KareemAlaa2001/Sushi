package comp1206.sushi.common;

import java.util.List;
import java.util.Map;
import java.util.Random;

import comp1206.sushi.server.StockManager;

public class Staff extends Model implements Runnable{

	private String name;
	private String status;
	private Number fatigue;
	private StockManager manager;
	private boolean alive;
	
	public Staff(String name, StockManager manager) {
		this.setName(name);
		this.setFatigue(0);
		this.manager = manager;
		this.setStatus("Idle");
		this.alive = true;
	}

	public String getName() {
		return name;
	}
	
	public void killMe()
	{
		this.alive = false;
	}

	public void setName(String name) {
		notifyUpdate("name",this.name,name);
		this.name = name;
	}

	public Number getFatigue() {
		return fatigue;
	}

	public void setFatigue(Number fatigue) {
		notifyUpdate("fatigue",this.fatigue,fatigue);
		this.fatigue = fatigue;
	}
	
	public void takeBreak()
	{
		this.setStatus("Taking a 5 minute break");
		try {
			Thread.sleep(300000);
		} catch (InterruptedException e) {}
	}
	
	public void increaseFatigue(Number fatigue)
	{
		Integer newFatigue = getFatigue().intValue() + fatigue.intValue();
		
		if (newFatigue >= 100)
		{
			setFatigue(100);
			return;
		}
		else {
			setFatigue(newFatigue);
			return;
		}
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}

	private void restockDish(Dish dish)
	{
		synchronized(dish)
		{
			dish.setRestockNeed(false);
			
			synchronized(manager.getIngredients())
			{
				for (int i = 0; i < (Integer) dish.getRestockAmount(); i++)
					consumeRecipeIngredients(dish.getRecipe());
			}
		}
		
		this.setStatus("Preparing " + dish.getName());	
		
		System.out.println(this.getName() + ": restocking " + dish.getName());
		
		sleepForRestockTime((Integer) dish.getRestockAmount());

		manager.increaseStock(dish, dish.getRestockAmount());
		increaseFatigue(dish.getRestockAmount());
		this.setStatus("Idle");
	}
	
	private void consumeRecipeIngredients(Map<Ingredient, Number> recipe)
	{
		recipe.forEach((ing, num) -> manager.consumeIngredients(ing,num));
	}
	
	@Override
	public void run() {
		while (alive) {
			try { 
				Thread.sleep(100);
				this.monitorStocks();
			} catch (InterruptedException e) { System.err.println("intException"); } catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}

	private void monitorStocks() throws Exception
	{
		synchronized(manager.getDishes()) 
		{
				while(manager.getDishesNeedingRestock().size() == 0)
				{
					System.out.println(this.getName() + ": all above thresholds");
					manager.getDishes().wait();
				}
		}

		if (!manager.allDishesAboveThresholds())
		{
			Dish dishToRestock = null;
			
			synchronized(manager.getDishes())
			{
				List<Dish> lowDishes = manager.getDishesNeedingRestock();

				for (Dish dish: lowDishes) synchronized (dish)
				{
					if (manager.isRestockable(dish) && dish.needsRestock())
					{
						dish.setRestockNeed(false);
						dishToRestock = dish;
						break;
					}
				}
			}
			
			if (dishToRestock != null)
			{
				restockDish(dishToRestock);
				
				if (getFatigue().intValue() == 100)
				{
					takeBreak();
				}
			}
			else 
			{
				synchronized(manager.getDishes())
				{
					System.out.println("Not enough ingredients to restock any dishes! Going idle.");
					manager.getDishes().wait();
				}
			}
		}
		
		else throw new Exception("Something is wrong! Staff notified even tho dishes above thresholds!");

	}

	private void sleepForRestockTime(int numIterations)
	{
		Random rand = new Random();

		for (int i = 0; i < numIterations; i++)
		{
			try {
				 Thread.sleep(20000 + rand.nextInt(40000));
			} catch (InterruptedException e) { return; }
		}
	}
}
