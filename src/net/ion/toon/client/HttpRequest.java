package net.ion.toon.client;

import java.util.concurrent.Future;

import net.ion.radon.aclient.AsyncHandler;
import net.ion.radon.aclient.Cookie;
import net.ion.radon.aclient.NewClient.BoundRequestBuilder;
import net.ion.radon.aclient.Request;

public class HttpRequest {

	private ToonClient client;
	private BoundRequestBuilder builder;

	public HttpRequest(ToonClient client, BoundRequestBuilder builder) {
		this.client = client ;
		this.builder = builder ;
	}
	
	public Request build(){
		return builder.build() ;
	}
	
	public HttpRequest addParam(String name, String value){
		builder.addParameter(name, value) ;
		return this ;
	}
	
	public HttpRequest addCookie(Cookie cookie){
		builder.addCookie(cookie) ;
		return this ;
	}
	
	
	
	public HttpRequest addQueryParam(String name, String value){
		builder.addQueryParameter(name, value) ;
		return this ;
	}

	
	public <T> T execute(UIHandler<T> handler){
		return client.execute(this, handler) ;
	}
	

	public <T> T execute(AsyncHandler<T> handler) {
		return client.execute(this, handler) ;
	}
	
	
	

}
