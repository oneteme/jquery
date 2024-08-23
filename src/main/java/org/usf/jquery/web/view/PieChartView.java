package org.usf.jquery.web.view;

import static java.lang.String.join;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;
import static java.util.stream.Collectors.toList;
import static org.usf.jquery.web.view.WebViewMapper.WebType.NUMBER;
import static org.usf.jquery.web.view.WebViewMapper.WebType.STRING;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.stream.Stream;

import org.usf.jquery.core.MappingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 * @see <a href="https://developers.google.com/chart/interactive/docs/gallery/piechart?hl=fr#data-format">piechart</a>
 *
 */
@Slf4j
@RequiredArgsConstructor
public final class PieChartView implements WebViewMapper {
	
	private static final String DATA = "$data";
	private static final String TYPE = "$chart";

    private final Writer writer;
    
    
    public Void map(ResultSet rs) throws SQLException { //scroll.
		log.debug("mapping results...");
		var bg = currentTimeMillis();
        var rw = 0;
    	var cols = TableColumn.columns(rs.getMetaData());
    	var numb = Stream.of(cols).filter(c-> c.getType() == NUMBER).collect(toList());
    	if(numb.isEmpty()) {
    		throw new IllegalArgumentException("require number column");
    	}
    	var yAxis = numb.get(numb.size()-1); //last one
    	var xAxis = Stream.of(cols).filter(c-> !c.getName().equals(yAxis.getName())).map(TableColumn::getName).collect(toList());
    	if(xAxis.isEmpty()) {
    		xAxis.add(yAxis.getName());
    	}
    	var sb1 = new StringBuilder();
    	var sb2 = new StringBuilder();
    	if(rs.next()) {
    		for(var c : numb) {
    			sb1.append(",[")
    			.append(STRING.format(c.getName())).append(",")
    			.append(NUMBER.format(rs.getObject(c.getName()))).append("]");
    		}
    		do {
    			sb2.append(",[");
    			var xVals= new LinkedList<String>();
    			for(var c : xAxis) {
    				xVals.add(valueOf(rs.getObject(c)));
    			}
    			sb2.append(STRING.format(join("_", xVals))).append(",")
    			.append(NUMBER.format(rs.getObject(yAxis.getName())))
    			.append("]");
    			rw++;
    		}
    		while(rs.next());
    	}
    	StringBuilder sb = new StringBuilder();
    	if(rw == 1 && numb.size() == cols.length) { //one row, only number columns
			sb.append("[")
			.append(STRING.format("column")).append(",")
			.append(STRING.format("value")).append("]")
			.append(sb1);
    	}
    	else {
			sb.append("[")
			.append(STRING.format(join("_", xAxis))).append(",")
			.append(STRING.format(yAxis.getName())).append("]")
			.append(sb2);
    	}
		try {
			writer.write(readString(Paths.get(getClass().getResource("./pie.google.html").toURI()))
					.replace(TYPE, "PieChart")
					.replace(DATA, sb.toString())
					.replace(lineSeparator(), ""));
		} catch (IOException | URISyntaxException e) {
			throw new MappingException("error mapping results", e);
		}
		log.info("{} rows mapped in {} ms", rw, currentTimeMillis() - bg);
		return null;
    }
    
}
