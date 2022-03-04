package fr.enedis.teme.jquery.web;

import lombok.NonNull;

@SuppressWarnings("serial")
public final class ResourceNotFoundException extends JQueryRuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}
	
	public static ResourceNotFoundException tableNotFoundException(@NonNull String name) {
		return new ResourceNotFoundException("'"+ name + "' table not found");
	}
	
	public static ResourceNotFoundException columnNotFoundException(@NonNull String name) {
		return new ResourceNotFoundException("'"+ name + "' column not found");
	}

}
