package fr.enedis.teme.jquery.reflect;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DatabaseScanner {
	
	private static DatabaseScanner instance;
	
	private final DataSource ds;
	private final Future<?> sf;
	
	
	private TablePartitionReflection tpScanner;
	
	DatabaseScanner(DataSource ds, int refreshDelay) {
		this.ds = ds;
		this.sf = newScheduledThreadPool(1)
				.scheduleWithFixedDelay(this::refreshAll, 1, refreshDelay, SECONDS); //fetch every hour
	}
	
	private void refreshAll() {
//		for(var o : actions) {
//			synchronized (o) {
//				o.fetch(ds);
//			}
//		}
	}
	
	private void getPartitionScanner(){
		
	}
	
	public static final DatabaseScanner configure(DataSource ds, int refreshDelay) {
		if(instance != null) {
			throw new RuntimeException("already configured");
		}
		instance = new DatabaseScanner(ds, refreshDelay);
		return instance;
	}

	public static final DatabaseScanner instance() {
		return instance;
	}
	

}
