package comp1206.sushi.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import comp1206.sushi.common.Postcode;
import comp1206.sushi.server.Server;

public class Postcode extends Model {

	private String name;
	private Map<String,Double> latLong;
	private Number distance;

	public Postcode(String code) {
		this.name = code;
		calculateLatLong();
		this.distance = 0;
	}
	
	public Postcode(String code, Restaurant restaurant) {
		this.name = code;
		calculateLatLong();
		calculateDistance(restaurant);
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Number getDistance() {
		return this.distance;
	}

	public Map<String,Double> getLatLong() {
		return this.latLong;
	}
	
	public Double getLat()
	{
		return getLatLong().get("lat");
	}
	
	public Double getLong()
	{
		return getLatLong().get("long");
	}
	
	public void calculateDistance(Restaurant restaurant)
	{
		this.distance = calculateDistance(getLat(),restaurant.getLocation().getLat(),getLong(),restaurant.getLocation().getLong(),0,0);
	}
	
	/**
	 * Calculate distance between two points in latitude and longitude taking
	 * into account height difference. If you are not interested in height
	 * difference pass 0.0. Uses Haversine method as its base.
	 * 
	 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
	 * el2 End altitude in meters
	 * @returns Distance in Meters
	 */
	public static double calculateDistance(double lat1, double lat2, double lon1,
	        double lon2, double el1, double el2) {

	    final int R = 6371; // Radius of the earth

	    double latDistance = Math.toRadians(lat2 - lat1);
	    double lonDistance = Math.toRadians(lon2 - lon1);
	    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
	            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
	            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    double distance = R * c * 1000; // convert to meters

	    double height = el1 - el2;

	    distance = Math.pow(distance, 2) + Math.pow(height, 2);

	    return Math.sqrt(distance);
	}
	
	protected void calculateLatLong() {
		
		try {
            String postcodePart = this.name.replace(" ", "%20");
            URL url = new URL("https://www.southampton.ac.uk/~ob1a12/postcode/postcode.php?postcode=" + postcodePart);
            latLong = new HashMap<>();
            
            // read text returned by server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
             
            String line;
            
            while ((line = in.readLine()) != null) {
            	String[] parts = line.split(":");
            	Double lat = Double.valueOf(parts[2].split(",")[0].substring(1, parts[2].split(",")[0].length() - 1));
            	
            	Double longitude = Double.valueOf(parts[3].substring(1,(parts[3].length()-2)));
            	Double long2 = new Double(longitude);

            	latLong.put("lat", lat);
            	latLong.put("long", long2);
            	
            }
            
            in.close();
             
        }
        catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
		
	}
	
	public double calculateDistanceToPostcode(Postcode pc)
	{
		return calculateDistance(getLat(),pc.getLat(),getLong(),pc.getLong(),0,0);
	}
	
}
