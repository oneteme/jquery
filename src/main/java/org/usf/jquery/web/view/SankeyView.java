package org.usf.jquery.web.view;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;
import static org.usf.jquery.web.view.WebViewMapper.TableColumn.columns;
import static org.usf.jquery.web.view.WebViewMapper.WebType.NUMBER;
import static org.usf.jquery.web.view.WebViewMapper.WebType.STRING;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import org.usf.jquery.core.MappingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 * @see <a href="https://developers.google.com/chart/interactive/docs/gallery/treemap?hl=fr#data-format">treemap</a>
 *
 */
@Slf4j
@RequiredArgsConstructor
public class SankeyView implements WebViewMapper {

	private static final String COLS = "$columns";
	private static final String DATA = "$data";

    private final Writer writer;

	@Override
	public Void map(ResultSet rs) throws SQLException {
    	if(rs.getMetaData().getColumnCount() != 3) {
        	throw new IllegalArgumentException("require [STRING, STRING, NUMBER] columns");
    	}
		log.debug("mapping results...");
		var bg = currentTimeMillis();
        var rw = 0;
        var cols = columns(rs.getMetaData());
        var xAxis = Stream.of(cols).filter(c-> c.getType() == STRING).toList();
        if(xAxis.size() != 2) {
        	throw new IllegalArgumentException("require [STRING, STRING, NUMBER] columns");
        }
		var yAxis = Stream.of(cols).filter(c-> c.getType() == NUMBER).findAny().orElseThrow(()-> new IllegalArgumentException("require number column"));
		var sb1 = new StringBuilder();
		for(var c : xAxis) {
			sb1.append("[")
			.append(STRING.format(c.getType().typeName())).append(",")
			.append(STRING.format(c.getName())).append("],");
		}
		sb1.append("[")
		.append(STRING.format(yAxis.getType().typeName())).append(",")
		.append(STRING.format(yAxis.getName())).append("]");

		var sb2 = new StringBuilder();
		while(rs.next()) {
			sb2.append("[");
			for(var c : xAxis) {
				sb2.append(c.getType().format(rs.getObject(c.getName()))).append(",");
			}
			sb2.append(yAxis.getType().format(rs.getObject(yAxis.getName()))).append("],");
		}
		if(!sb2.isEmpty()) { //no data
			sb2.deleteCharAt(sb2.length()-1); //dirty but less code
		}
		try {
			writer.write(readString(Paths.get(getClass().getResource("./sankey.google.html").toURI()))
					.replace(COLS, sb1.toString()) //TD optim this
					.replace(DATA, sb2.toString())
					.replace(lineSeparator(), ""));
		} catch (IOException | URISyntaxException e) {
			throw new MappingException("error mapping results", e);
		}
		log.info("{} rows mapped in {} ms", rw, currentTimeMillis() - bg);
		return null;
	}

}
