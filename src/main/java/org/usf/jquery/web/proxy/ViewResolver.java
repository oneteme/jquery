package org.usf.jquery.web.proxy;

import static org.usf.jquery.web.proxy.JQueryResource.getSchema;

import org.usf.jquery.core.QueryComposer;

public class ViewResolver {
	
	
	public static void main(String[] args) throws InterruptedException {
		var schm = getSchema(SchemaResource.class);
//		System.out.println(schm.toString());
//		System.out.println(schm.mainSession());
//		System.out.println(schm.dashboard());
//		System.out.println(schm.getValue());
		
//		System.out.println(schm
//				.equals(getSchema(SchemaResource.class)));
		
//		var ses = schm.mainSession();
//		var qry = new QueryComposer()
//				.columns(ses.location(), ses.host(), ses.elapsedtime())
//				.filters(ses.elapsedtime().gt(10));
//		
//		System.out.println(qry);
	}

}
