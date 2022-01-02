package fr.enedis.teme.jquery.web;

@SuppressWarnings("serial")
public final class ResourceNotFoundException extends JQueryRuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}
	
	public static ResourceNotFoundException tableNotFoundException(String name) {
		return new ResourceNotFoundException("table not found '"+ name + "'");
	}
	
	public static ResourceNotFoundException columnNotFoundException(String name) {
		return new ResourceNotFoundException("column not found '"+ name + "'");
	}

}
