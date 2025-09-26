package org.usf.jquery.web.mvc;

import static java.lang.String.join;
import static java.nio.file.Files.readString;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.DataWriter.toDataWriter;
import static org.usf.jquery.core.JDBCType.fromDataType;
import static org.usf.jquery.core.Mappers.asciiWriter;
import static org.usf.jquery.core.Mappers.csvWriter;
import static org.usf.jquery.core.Mappers.keyValueMapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.usf.jquery.core.AsciiResultMapper;
import org.usf.jquery.core.CsvResultMapper;
import org.usf.jquery.core.DataWriter;
import org.usf.jquery.core.DynamicModel;
import org.usf.jquery.core.ResultSetMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseMappers {
		
	public static AsciiResultMapper asciiResponseWriter(HttpServletResponse res) {
		return asciiResponseWriter(res, empty());
	}

	public static AsciiResultMapper asciiResponseWriter(HttpServletResponse res, Optional<String> filename) {
		headers(res, "text/plain; charset=utf-8", filename.map(withExtention("txt")));
		return asciiWriter(responseWriter(res));
	}
	
	public static CsvResultMapper csvResponseWriter(HttpServletResponse res) {
		return csvResponseWriter(res, empty());
	}
	
	public static CsvResultMapper csvResponseWriter(HttpServletResponse res, Optional<String> filename) {
		headers(res, "text/plain; charset=utf-8", filename.map(withExtention("csv")));
		return csvWriter(responseWriter(res));  //text/csv => force download
	}
	
	public static ViewBinder<Void> mvcViewBinder(HttpServletResponse res, String filename) {
		headers(res, "text/html; charset=utf-8", empty());
		return id->{
			try {
				var writer = res.getWriter();
				var path = Paths.get(ResponseMappers.class.getClassLoader().getResource(filename).toURI());
				writer.write(readString(path).replace("[[${callback}]]", "/callback/"+id));
				return null;
			} catch (IOException | URISyntaxException e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static ResultSetMapper<List<DynamicModel>> mvcModelMapper(HttpServletResponse res) {
		return rs->{
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
		};
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
