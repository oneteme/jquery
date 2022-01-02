package fr.enedis.teme.jquery.web;

@SuppressWarnings("serial")
public final class ResourceAccessDeniedException extends JQueryRuntimeException {
	
	public ResourceAccessDeniedException(String message) {
		super(message);
	}
	
	static ResourceNotFoundException tableAccessDeniedException(String name) {
		return new ResourceNotFoundException("access denied for table '" + name + "'");
	}
	
	static ResourceNotFoundException columnAccessDeniedException(String name) {
		return new ResourceNotFoundException("access denied for column '" + name + "'");
	}
	
}
