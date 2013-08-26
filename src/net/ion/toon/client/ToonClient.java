package net.ion.toon.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import net.ion.framework.logging.LogBroker;
import net.ion.radon.aclient.AsyncHandler;
import net.ion.radon.aclient.NewClient;
import net.ion.radon.aclient.Response;
import net.ion.radon.aclient.websocket.WebSocket;
import net.ion.radon.aclient.websocket.WebSocketTextListener;

public class ToonClient implements Closeable {

	private final ExecutorService eservice;
	private final NewClient newClient;
	private final String hostUrl;
	private ExceptionHandler ehandler = ExceptionHandler.Default;
	private Logger log = LogBroker.getLogger(ToonClient.class);

	public ToonClient(String hostUrl) {
		this.hostUrl = hostUrl;
		this.eservice = Executors.newCachedThreadPool();
		this.newClient = NewClient.create();
	}

	public HttpRequest createGet(String subPath) {
		return new HttpRequest(this, newClient.prepareGet(makeUrl(subPath)));
	}

	public HttpRequest createPost(String subPath) {
		return new HttpRequest(this, newClient.preparePost(makeUrl(subPath)));
	}

	public HttpRequest createPut(String subPath) {
		return new HttpRequest(this, newClient.preparePut(makeUrl(subPath)));
	}

	public HttpRequest createDel(String subPath) {
		return new HttpRequest(this, newClient.prepareDelete(makeUrl(subPath)));
	}

	private String makeUrl(String subPath) {
		return hostUrl + subPath;
	}

	public <T> Future<T> execute(final HttpRequest request, final UIHandler<T> handler) {
		return eservice.submit(new Callable<T>() {
			@Override
			public T call() throws Exception {
				Response response = newClient.prepareRequest(request.build()).execute().get();
				try {
					T result = handler.handle(response) ;
					return result ;
				} finally {
					closeQuietly(response) ;
				}
			}
		});
	}

	private void closeQuietly(Response response) {
		try {
			if (response == null)
				return;
			response.getBodyAsStream().close();
		} catch (IOException ignore) {
			ignore.printStackTrace();
		}
	}

	public <T> Future<T> execute(final HttpRequest request, final AsyncHandler<T> handler) {
		return eservice.submit(new Callable<T>() {
			@Override
			public T call() throws Exception {
				return newClient.prepareRequest(request.build()).execute(handler).get();
			}
		});
	}

	@Override
	public void close() {
		eservice.shutdown();
		newClient.close();
	}

	public WebSocket createWebsocket(final String webSocketUri, final WebSocketTextListener listener) {
		final CountDownLatch latch = new CountDownLatch(1);
		try {
			
			WebSocket wsocket = eservice.submit(new Callable<WebSocket>() {
				@Override
				public WebSocket call() throws Exception {
					return newClient.createWebSocket(webSocketUri, new WebSocketTextListener() {
						@Override
						public void onClose(WebSocket wsocket) {
							listener.onClose(wsocket);
						}
						@Override
						public void onError(Throwable ex) {
							listener.onError(ex);
						}
						@Override
						public void onOpen(WebSocket wsocket) {
							listener.onOpen(wsocket);
							latch.countDown();
						}
						@Override
						public void onFragment(String received, boolean last) {
							listener.onFragment(received, last);
						}
						@Override
						public void onMessage(String received) {
							listener.onMessage(received);
						}
					});
				}
			}).get() ;
			
			latch.await();
			return wsocket ;
		} catch (InterruptedException e) {
			ehandler.handle(e, log);
		} catch (ExecutionException e) {
			ehandler.handle(e, log);
		}
		return null;
	}

	public ExceptionHandler ehandler() {
		return ehandler ;
	}

}
