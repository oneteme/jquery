package org.usf.jquery.web.view;

import static org.usf.jquery.web.view.Chart2DView.areaChart;
import static org.usf.jquery.web.view.Chart2DView.barChart;
import static org.usf.jquery.web.view.Chart2DView.columnChart;
import static org.usf.jquery.web.view.Chart2DView.comboChart;
import static org.usf.jquery.web.view.Chart2DView.lineChart;

import java.io.Writer;

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
		return switch (view) {
		case "table"	: yield new TableView(w);
		case "pie"		: yield new PieChartView(w);
		case "column"	: yield columnChart(w);
		case "bar"		: yield barChart(w);
		case "area"		: yield areaChart(w);
		case "combo"	: yield comboChart(w);
		case "line"		: yield lineChart(w);
		case "timeline"	: yield new TimelineChartView(w);
		case "calendar"	: yield new CalendarView(w);
		case "sankey"	: yield new SankeyView(w);
		default			: throw new IllegalArgumentException(view);
		};
	}

}
