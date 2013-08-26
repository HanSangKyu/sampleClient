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

import org.apache.commons.io.IOUtils;
import org.apache.ecs.xhtml.li;

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

	public <T> T execute(final HttpRequest request, final UIHandler<T> handler) {
		Response response = null;
		try {
			response = eservice.submit(new Callable<Response>() {
				@Override
				public Response call() throws Exception {
					return newClient.prepareRequest(request.build()).execute().get();
				}
			}).get();

			return handler.handle(response);

		} catch (InterruptedException e) {
			ehandler.handle(e, log);
		} catch (ExecutionException e) {
			ehandler.handle(e, log);
		} catch (Exception e) {
			ehandler.handle(e, log);
		} finally {
			closeQuietly(response);
		}
		return null;
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

	public <T> T execute(final HttpRequest request, final AsyncHandler<T> handler) {
		try {
			return eservice.submit(new Callable<T>() {
				@Override
				public T call() throws Exception {
					return newClient.prepareRequest(request.build()).execute(handler).get();
				}
			}).get();
		} catch (InterruptedException e) {
			ehandler.handle(e, log);
		} catch (ExecutionException e) {
			ehandler.handle(e, log);
		}
		return null;
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

}
