package org.usf.jquery.web.proxy;

import static java.lang.String.join;
import static java.nio.file.Files.readString;
import static java.util.Collections.synchronizedMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.DataWriter.toDataWriter;
import static org.usf.jquery.core.JDBCType.fromDataType;
import static org.usf.jquery.core.Mappers.asciiWriter;
import static org.usf.jquery.core.Mappers.csvWriter;
import static org.usf.jquery.core.Mappers.keyValueMapper;
import static org.usf.jquery.core.QueryExecutor.defaultExecutor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.usf.jquery.core.DataWriter;
import org.usf.jquery.core.DynamicModel;
import org.usf.jquery.core.QueryExecutor;

import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 * 
 */
@Slf4j
public class ViewRegistry {
	
	private static final Map<String, DataViewer> DEF_VIEWERS; 
	private static final Map<String, MvcRequest> queryQueue = synchronizedMap(new LinkedHashMap<>()); //timeout !?
	
	private Map<String, DataViewer> viewers;
	
	public void register(String id, DataViewer viewer) {
		if(isNull(this.viewers)) {
			this.viewers = new HashMap<>();
		}
		viewers.put(id, viewer);
	}
	
	public DataViewer viewer(String id) {
		var v = nonNull(viewers) ? viewers.get(id) : null;
		if(isNull(v)) {
			v = DEF_VIEWERS.get(id);
		}
		return v;
	}
	
	public static MvcRequest callback(String id){
		var exc = queryQueue.remove(id);
		if(nonNull(exc)) {
			return  exc;
		}
		throw new NoSuchElementException(id + " not found");
	}
	
	static {
		var map = new HashMap<String, DataViewer>();
		map.put("json", ViewRegistry::keyValueViewer);
		map.put("csv", ViewRegistry::csvViewer);
		map.put("ascii", ViewRegistry::asciiViewer);
		map.put("google.v1", rsp-> lasyHtmlViewer(rsp, "static/google.v1.html"));
		DEF_VIEWERS = unmodifiableMap(map);
	}
	
	@FunctionalInterface
	public interface DataViewer {
		
		QueryExecutor<?> view(HttpServletResponse res);
	}
	
	public static QueryExecutor<List<DynamicModel>> keyValueViewer(HttpServletResponse res) {
		return defaultExecutor(keyValueMapper());
	}
	
	public static QueryExecutor<Void> asciiViewer(HttpServletResponse res) {
		return asciiViewer(res, empty());
	}

	public static QueryExecutor<Void> asciiViewer(@NonNull HttpServletResponse res, Optional<String> filename) {
		headers(res, "text/plain; charset=utf-8", filename.map(withExtention("txt")));
		return defaultExecutor(asciiWriter(responseWriter(res)));
	}
	
	public static QueryExecutor<Void> csvViewer(HttpServletResponse res) {
		return csvViewer(res, empty());
	}
	
	public static QueryExecutor<Void> csvViewer(@NonNull HttpServletResponse res, Optional<String> filename) {
		headers(res, "text/plain; charset=utf-8", filename.map(withExtention("csv")));
		return defaultExecutor(csvWriter(responseWriter(res)));  //text/csv => force download
	}
	
	public static QueryExecutor<Void> lasyHtmlViewer(@NonNull HttpServletResponse res, String template) {
		headers(res, "text/html; charset=utf-8", empty());
		return (qry, str)->{
			try {
				var id = randomUUID().toString();
				var writer = res.getWriter();
				var path = Paths.get(ViewRegistry.class.getClassLoader().getResource(template).toURI());
				writer.write(readString(path).replace("[[${callback}]]", "/callback/"+id));
				queryQueue.put(id, new MvcRequest((StoreResource) str, qry, rsp-> mvcModelMapper(rsp)));
				return null;
			} catch (IOException | URISyntaxException e) {
				throw new RuntimeException(e); //TODO change this
			}
		};
	}

	public static QueryExecutor<List<DynamicModel>> mvcModelMapper(@NonNull HttpServletResponse res) {
		return defaultExecutor(rs->{
			var md = rs.getMetaData();
			var map = new LinkedHashMap<String, String>(); 
			for(var i=0; i<md.getColumnCount(); i++) {
				map.put(md.getColumnLabel(i+1), 
						fromDataType(md.getColumnType(i+1))
						.map(e-> e.getCorrespondingClass().getSimpleName()).orElse(null));
			}
			res.addHeader("Access-Control-Expose-Headers", "X-JQuery-Metadata");
			res.setHeader("X-JQuery-Metadata", map.entrySet().stream()
					.map(e-> join(":", e.getKey(), e.getValue()))
					.collect(joining(",")));
			return keyValueMapper().map(rs);
		});
	}
	
	static void headers(HttpServletResponse res, String type, Optional<String> filename) {
		res.setHeader("Content-Type", type);
		if(filename.isPresent()) {
			res.setHeader("Content-Disposition", "attachement; filename=\""+filename.get()+"\"");
		}
	}
	
	static DataWriter responseWriter(HttpServletResponse res) {
		try {
			return toDataWriter(res.getOutputStream());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	static UnaryOperator<String> withExtention(String ext){
		return v-> join(".", v, ext);
	}
}
