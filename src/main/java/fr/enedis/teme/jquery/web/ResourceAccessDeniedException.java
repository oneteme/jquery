package fr.enedis.teme.jquery.web;

import lombok.NonNull;

@SuppressWarnings("serial")
public final class ResourceAccessDeniedException extends JQueryRuntimeException {
	
	public ResourceAccessDeniedException(String message) {
		super(message);
	}
	
	static ResourceNotFoundException tableAccessDeniedException(@NonNull String name) {
		return new ResourceNotFoundException("access denied for table '" + name + "'");
	}
	
	static ResourceNotFoundException columnAccessDeniedException(@NonNull String name) {
		return new ResourceNotFoundException("access denied for column '" + name + "'");
	}
	
}
