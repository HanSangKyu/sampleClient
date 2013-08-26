package net.ion.toon.client;

import net.ion.radon.aclient.Response;

public interface UIHandler<T> {

	public T handle(Response response) throws Exception ;
}
