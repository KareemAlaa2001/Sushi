package comp1206.sushi.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import comp1206.sushi.client.Client;
import comp1206.sushi.server.Server;

public class Comms {
	
	ServerComms serComms;
	Server server;
	Client client;
	ClientComms cliComms;
	
	public Comms(Object src)
	{
		if (src instanceof Server)
		{
			System.out.println("server instance detected in comms");
			this.client = null;
			this.cliComms = null;
			
			Server server = (Server) src;
			this.server = server;
			this.serComms = new ServerComms(server);
			
			new Thread(serComms).start();
		}
		
		else if (src instanceof Client)
		{
			System.out.println("client instance detected in comms");
			this.client = (Client) src;
			this.cliComms = new ClientComms(client);
			System.out.println("clicomms creation complete");
			new Thread(cliComms).start();
			
			this.server = null;
			this.serComms = null;
		}
		
		else throw new IllegalArgumentException("The source attempting to connect is neither a server nor a client!");
		
	}
	
	public void sendMessage(Serializable message)
	{
		if (serComms != null)
		{
			serComms.sendMessage(message);
		}
		else 
		{
			cliComms.sendMessage(message);
		}
	}
}

class ServerComms implements Runnable{	
	
	Server server;
	ServerSocket serverSocket;
	static final int PORT_NUM = 5000;
	List<ClientConnection> clientConnections = new ArrayList<>();
	
	public ServerComms(Server server) 
	{
		try {
			serverSocket = new ServerSocket(PORT_NUM);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.server = server;
	}

	public void sendMessage(Serializable message) {
		if (clientConnections.size() > 0)
		{
			for (ClientConnection cc: clientConnections)
			{
				cc.sendMessage(message);
			}
		}
	}

	@Override
	public void run() {
		while(true)
		{       
			Socket newSock = null;
            try 
            { 
            	try {
	                // socket object to receive incoming client requests 
	                newSock = serverSocket.accept(); 
            	} catch (NullPointerException e) {
            		System.err.println("There is another server instance running! Comms unsuccessful!");
            		return;
            	}
                System.out.println("New client connected : " + newSock); 
                  
                // obtaining input and out streams 
                ObjectInputStream in = new ObjectInputStream(newSock.getInputStream()); 
                ObjectOutputStream out = new ObjectOutputStream(newSock.getOutputStream()); 
                
                initNewClient(newSock,in,out);
                
                ClientConnection connection = new ClientConnection(newSock, in, out,this);
                this.clientConnections.add(connection);

                new Thread(connection).start();
            } catch (Exception e) { 
            	e.printStackTrace();
            } 
		}
	}
	
	//TODO IF THIS DOESNT WORK SEND EACH OBJECT INDIVIDUALLY
	private void initNewClient(Socket socket, ObjectInputStream in, ObjectOutputStream out) throws IOException
	{
		out.writeObject(server.getRestaurant());
		out.writeObject(server.getDishes());
		out.writeObject(server.getUsers());
		out.writeObject(server.getPostcodes());
	}
	
	public void receiveMessage(Object message)
	{
		server.receiveMessage(message);
	}
	
	public void closeConnection(ClientConnection connection)
	{
		this.clientConnections.remove(connection);
	}
}

class ClientConnection implements Runnable {

	final Socket clientSocket;
	final ObjectInputStream in;
	final ObjectOutputStream out;
	ServerComms serComms;

	public ClientConnection(Socket clientSocket, ObjectInputStream in, ObjectOutputStream out, ServerComms sc)
	{
		this.clientSocket = clientSocket;
		this.in = in;
		this.out = out;
		this.serComms = sc;
	}
	
	public ObjectOutputStream getOutStream()
	{
		return this.out;
	}
	
	public ObjectInputStream getInStream()
	{
		return this.in;
	}
	
	public Socket getSocket()
	{
		return this.clientSocket;
	}
	@Override
	public void run() {
		while (true)
		{
			try {
				Object message;
				while ((message = in.readObject()) == null)
				{
					//do nothing
				}
				serComms.receiveMessage(message);
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				serComms.closeConnection(this);
				break;
			} 
		}	
	}
	
	public void sendMessage(Serializable message)
	{
		try {
		out.writeObject(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ClientComms implements Runnable {

	Socket socket;
	static final int PORT_NUM = 5000;
	ObjectInputStream in;
	ObjectOutputStream out;
	Client client;
	
	public ClientComms(Client client) {
		try {
			this.client = client;
			socket = new Socket("localhost",PORT_NUM);
			System.out.println("clicomms socket made");
			
			out = new ObjectOutputStream(socket.getOutputStream());
			System.out.println("Got output");

			in = new ObjectInputStream(socket.getInputStream());
			
			System.out.println("Got input");
			
			System.out.println("Streams obtained in clicomms");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(Serializable message) 
	{
		try {
		out.writeObject(message);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void run() 
	{
		while (true)
		{
			try {
				Object message;
				while ((message = in.readObject()) == null)
				{
					//do nothing
				}
				client.receiveMessage(message);
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}	
	}
	
}