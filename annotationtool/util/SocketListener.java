package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import annotation.IconControllerBox;
import javafx.application.Platform;

public class SocketListener {
	int portNumber;
	IconControllerBox controllerBox;
	
	public SocketListener(IconControllerBox controllerBox, int portNumber) {
		this.controllerBox = controllerBox;
		setPortNumber(portNumber);
		listen();
	}
	
	public SocketListener() {
		listen();
	}
	
	public void listen() {
		try { 
		    ServerSocket serverSocket = new ServerSocket(portNumber);
		    while(controllerBox != null) {
		    	System.out.println("Ｉ ａｍ ｔｈｅ ｌｉｓｔｅｎｅｒ. Ｉ ｌｉｖｅ.");
		    	Socket clientSocket = serverSocket.accept();
		    	PrintWriter out =
		    			new PrintWriter(clientSocket.getOutputStream(), true);
		    	BufferedReader in = new BufferedReader(
		    			new InputStreamReader(clientSocket.getInputStream()));
		    	String message = in.readLine();
		    	if(message.endsWith(".jnote")) {
		    		System.out.println("Message Received");
		    		Platform.runLater(new Runnable() {

						@Override
						public void run() {
							controllerBox.newFile(message);
						}
		    			
		    		});
		    	} else {
		    		System.out.println("Unknown Command: " + message);
		    	}
		    }
		} catch (IOException e) {
			e.printStackTrace();
		} finally {}
	}
	
	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}
	
}
