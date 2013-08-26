package net.ion.toon.client;

import java.util.logging.Logger;

public interface ExceptionHandler {

	public final static ExceptionHandler Default = new ExceptionHandler(){
	
			@Override
			public void handle(Exception ex, Logger log) {
				ex.printStackTrace() ;
			}

			@Override
			public void handle(Throwable ex) {
				ex.printStackTrace() ;
			}
	};
	public void handle(Exception ex, Logger log) ;
	public void handle(Throwable ex);
}
