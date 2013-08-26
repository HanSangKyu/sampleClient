package net.ion.toon.client;

import java.util.logging.Logger;

public interface ExceptionHandler {

	public final static ExceptionHandler Default = new ExceptionHandler(){
	
			@Override
			public void handle(Exception ex, Logger log) {
				ex.printStackTrace() ;
			}
	};
	public void handle(Exception ex, Logger log) ;
}
