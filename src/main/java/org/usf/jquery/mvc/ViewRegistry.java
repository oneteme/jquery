package org.usf.jquery.mvc;

import static java.lang.String.join;
import static java.nio.file.Files.readString;
import static java.util.Collections.synchronizedMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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

import org.usf.jquery.core.DataWriter;
import org.usf.jquery.core.DynamicModel;
import org.usf.jquery.core.QueryExecutor;
import org.usf.jquery.core.ResultSetMapper;

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
	
	private static final Map<String, ResultSetViewer> DEF_VIEWERS = Map.of(
			"map", rsp-> keyValueViewer(),
			"csv", ViewRegistry::csvViewer,
			"ascii", ViewRegistry::asciiViewer,
			"google.v1", rsp-> lasyHtmlViewer(rsp, "static/google.v1.html", keyValueMapper()));
	
	private static final Map<String, MvcRequest> queryQueue = synchronizedMap(new LinkedHashMap<>()); //timeout !?
	
	private Map<String, ResultSetViewer> viewers;
	
	public ViewRegistry register(String id, ResultSetViewer viewer) {
		if(isNull(this.viewers)) {
			this.viewers = new HashMap<>();
		}
		viewers.put(id, viewer);
		return this;
	}
	
	public ResultSetViewer geViewer(String id) {
		var v = nonNull(viewers) ? viewers.get(id) : null;
		return isNull(v) ? DEF_VIEWERS.get(id) : v;
	}
	
	public static MvcRequest callback(String id){
		var exc = queryQueue.remove(id);
		if(nonNull(exc)) {
			return  exc;
		}
		throw new NoSuchElementException(id + " not found");
	}
	
	public static QueryExecutor<List<DynamicModel>> keyValueViewer() {
		return modelViewer(keyValueMapper());
	}
	
	public static <T> QueryExecutor<T> modelViewer(ResultSetMapper<T> mapper) {
		return defaultExecutor(mapper);
	}
	
	public static QueryExecutor<Void> asciiViewer(HttpServletResponse res) {
		return asciiViewer(res, null);
	}

	public static QueryExecutor<Void> asciiViewer(@NonNull HttpServletResponse res, String filename) {
		headers(res, "text/plain; charset=utf-8", filename, "txt");
		return defaultExecutor(asciiWriter(responseWriter(res)));  //filename => force download
	}
	
	public static QueryExecutor<Void> csvViewer(HttpServletResponse res) {
		return csvViewer(res, null);
	}
	
	public static QueryExecutor<Void> csvViewer(@NonNull HttpServletResponse res, String filename) {
		headers(res, "text/plain; charset=utf-8", filename, "csv");
		return defaultExecutor(csvWriter(responseWriter(res)));  //filename => force download
	}
	
	public static QueryExecutor<Void> lasyHtmlViewer(@NonNull HttpServletResponse res, String template, ResultSetMapper<?> mapper) {
		headers(res, "text/html; charset=utf-8", null, null);
		return (qry, str)->{
			try {
				var id = randomUUID().toString();
				var writer = res.getWriter();
				var path = Paths.get(ViewRegistry.class.getClassLoader().getResource(template).toURI());
				writer.write(readString(path).replace("[[${callback}]]", "/callback/"+id)); //TD optim
				queryQueue.put(id, new MvcRequest((StoreResource) str, qry, rsp-> mvcModelMapper(rsp, mapper)));
				return null;
			} catch (IOException | URISyntaxException e) {
				throw new RuntimeException(e); //TODO change this
			}
		};
	}

	public static <T> QueryExecutor<T> mvcModelMapper(@NonNull HttpServletResponse res, ResultSetMapper<T> mapper) {
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
			return mapper.map(rs);
		});
	}
	
	static void headers(HttpServletResponse res, String type, String filename, String ext) {
		res.setHeader("Content-Type", type);
		if(nonNull(filename) && !filename.isEmpty()) {
			res.setHeader("Content-Disposition", "attachement; filename=\""+filename+'.'+ext+"\"");
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
}
