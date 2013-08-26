package net.ion.toon.main;

import net.ion.radon.aclient.AsyncCompletionHandler;
import net.ion.radon.aclient.Response;
import net.ion.radon.aclient.websocket.WebSocket;
import net.ion.radon.aclient.websocket.WebSocketTextListener;
import net.ion.toon.client.ToonClient;
import net.ion.toon.client.UIHandler;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	private ToonClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final TextView tv = new TextView(this);
		tv.setText("Hello World");
		setContentView(tv);

		try {
			client = new ToonClient("http://61.250.201.157:9000");

			// handling logic
			client.createGet("/hello").execute(new AsyncCompletionHandler<Void>() {
				@Override
				public Void onCompleted(Response response) throws Exception {
					System.out.println(response.getTextBody());
					return null;
				}
			}).get(); // not get


			// handling ui
			client.createGet("/hello").execute(new UIHandler<Void>() {
				@Override
				public Void handle(Response response) throws Exception {
					tv.setText("Mod Status :" + response.getStatusCode());
					return null;
				}
			}) ;

			// websocket
			WebSocket wsocket = client.createWebsocket("ws://61.250.201.157:9000/websocket/echo", new WebSocketTextListener() {
				@Override
				public void onOpen(WebSocket wsocket) {
				}

				@Override
				public void onError(Throwable ex) {
				}

				@Override
				public void onClose(WebSocket wsocket) {
				}

				@Override
				public void onMessage(String received) {
					System.out.println(received);
					tv.setText("Status :" + received);
				}

				@Override
				public void onFragment(String received, boolean last) {
				}
			});

			for (int i = 0; i < 10; i++) {
				wsocket.sendTextMessage("Hello WebSocekt " + i);
			}

		} catch (Throwable ex) {
			client.ehandler().handle(ex) ;
		}

	}

	@Override
	protected void onDestroy() {
		IOUtils.closeQuietly(client);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
