package net.ion.toon.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import junit.framework.TestCase;
import net.ion.framework.util.InfinityThread;
import net.ion.nradon.Radon;
import net.ion.nradon.WebSocketConnection;
import net.ion.nradon.WebSocketHandler;
import net.ion.nradon.config.RadonConfiguration;
import net.ion.nradon.handler.aradon.AradonHandler;
import net.ion.radon.core.Aradon;
import net.ion.radon.impl.let.HelloWorldLet;
import net.ion.radon.util.AradonTester;

public class TestRunWebSocket extends TestCase {
	
	
	public void testRun() throws Exception {
		Aradon aradon = AradonTester.create().register("", "/hello", HelloWorldLet.class).getAradon() ;
		
		final Radon server = RadonConfiguration.newBuilder(9000)
			.add("/hello", AradonHandler.create(aradon)) 
			.add("/websocket/echo", new WebSocketHandler() {
				private List<WebSocketConnection> conns = new ArrayList<WebSocketConnection>() ;
				public void onOpen(WebSocketConnection connection) {
					conns.add(connection) ;
				}

				public void onClose(WebSocketConnection connection) {
					conns.remove(connection) ;
				}

				public void onMessage(WebSocketConnection connection, String message) {
					WebSocketConnection[] ds = conns.toArray(new WebSocketConnection[0]) ;
					for (WebSocketConnection conn : ds) {
						conn.send(message.toUpperCase()); // echo back message in upper
					}
				}

				public void onMessage(WebSocketConnection connection, byte[] message) {
				}

				public void onPong(WebSocketConnection connection, byte[] message) {
				}
				public void onPing(WebSocketConnection connection, byte[] message) {
					connection.pong(message) ;
				}
			}).startRadon();
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				try {
					server.stop().get() ;
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}) ;
		
		new InfinityThread().startNJoin() ;
	}
}

