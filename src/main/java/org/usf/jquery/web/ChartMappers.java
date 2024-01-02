package org.usf.jquery.web;

import static org.usf.jquery.web.view.Chart2DView.areaChart;
import static org.usf.jquery.web.view.Chart2DView.barChart;
import static org.usf.jquery.web.view.Chart2DView.columnChart;
import static org.usf.jquery.web.view.Chart2DView.comboChart;
import static org.usf.jquery.web.view.Chart2DView.lineChart;

import java.io.Writer;

import org.usf.jquery.web.view.CalendarView;
import org.usf.jquery.web.view.PieChartView;
import org.usf.jquery.web.view.SankeyView;
import org.usf.jquery.web.view.TableView;
import org.usf.jquery.web.view.TimelineChartView;
import org.usf.jquery.web.view.WebViewMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChartMappers {
	
	public static WebViewMapper webChart(String view, Writer w) {
		switch (view) {
		case "table"	: return new TableView(w);
		case "pie"		: return new PieChartView(w);
		case "column"	: return columnChart(w);
		case "bar"		: return barChart(w);
		case "area"		: return areaChart(w);
		case "combo"	: return comboChart(w);
		case "line"		: return lineChart(w);
		case "timeline"	: return new TimelineChartView(w);
		case "calendar"	: return new CalendarView(w);
		case "sankey"	: return new SankeyView(w);
		default			: throw new IllegalArgumentException(view);
		}
	}

}
