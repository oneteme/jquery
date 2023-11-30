package org.usf.jquery.web.view;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;
import static java.util.stream.Collectors.toList;
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
public final class TimelineChartView implements WebViewMapper {

	private static final String COLS = "$columns";
	private static final String DATA = "$data";

    private final Writer writer;
    
    public Void map(ResultSet rs) throws SQLException {
    	if(rs.getMetaData().getColumnCount() < 2) {
    		throw new IllegalArgumentException("require 2 columns [DATE, NUMBER]");
    	}
		log.debug("mapping results...");
		var bg = currentTimeMillis();
        var rw = 0;
        var cols = columns(rs.getMetaData());
        var numb = Stream.of(cols).filter(c-> c.getType() == NUMBER).collect(toList());
        if(numb.isEmpty()) {
            numb = Stream.of(cols).filter(c-> c.getType().isDate()).collect(toList());
        }
        if(numb.isEmpty() || numb.size() != 2) {
        	throw new IllegalArgumentException("require NUMBER or DATE columns");
        }
        var yAxis = numb;
        var xAxis = Stream.of(cols).filter(c-> yAxis.stream().noneMatch(v-> v.getName().equals(c.getName()))).collect(toList());
        if(xAxis.isEmpty()) {
        	
        }
        if(numb.size() > 2) {
        	throw new IllegalArgumentException("too many columns");
        }
		var sb1 = new StringBuilder(100);
		for(var c : xAxis) {
			sb1.append("[")
			.append(STRING.format(c.getType().typeName())).append(",")
			.append(STRING.format(c.getName())).append("],");
		}
		for(var c : yAxis) {
			sb1.append("[")
			.append(STRING.format(c.getType().typeName())).append(",")
			.append(STRING.format(c.getName())).append("],");
		}
		sb1.deleteCharAt(sb1.length()-1); //dirty but less code
		var sb2 = new StringBuilder(100);
		while(rs.next()) {
			sb2.append("[");
			for(var c : xAxis) {
				sb2.append(c.getType().format(rs.getObject(c.getName()))).append(",");
			}
			for(var c : yAxis) {
				sb2.append(c.getType().format(rs.getObject(c.getName()))).append(",");
			}
			sb2.deleteCharAt(sb2.length()-1); //dirty but less code
			sb2.append("],");
		}
		if(!sb2.isEmpty()) { //no data
			sb2.deleteCharAt(sb2.length()-1); //dirty but less code
		}
		try {
			writer.write(readString(Paths.get(getClass().getResource("./timeline.google.html").toURI()))
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
