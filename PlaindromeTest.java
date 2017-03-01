import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Set;

import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;  

public class PlaindromeTest {

	public static void main(String[] args) {
		
		//data could be taken from args
		long min=0;
		long max=100000000;
		int timeToRun=30 * 1000; 
		int nThread = 8;
		
		Manager mgr = new Manager(min, max, nThread, timeToRun);
		mgr.start();
	}
}


class Manager implements Runnable{
	final long min, max;
	final int nthreads;
	final int timeToRun;
	
	Manager(long min, long max, int nthreads, int timeToRun){
		this.min=min;
		this.max=max;
		this.nthreads=nthreads;
		this.timeToRun=timeToRun;
	}
	void start(){
		new Thread(this).start();
	}
	public void run(){
		long block = max-min;
		long incr = block / nthreads;
		long tmin=0;
		long tmax=0;
		
		PlaindromeMiner workers[] = new PlaindromeMiner[nthreads];
		  long runtimes[] = new long[nthreads];
		  
	       ExecutorService executor = Executors.newFixedThreadPool(nthreads);  
	        tmin=min;
	        for (int i = 0; i < nthreads; i++) {  
	        	tmax = tmin+incr;
	            workers[i] = new PlaindromeMiner(tmin, tmax);  
	            executor.execute(workers[i]);  
	            tmin=tmax+1;
	          }  
	        try {
	        	Thread.sleep(timeToRun);
		        executor.shutdown();  
	        }catch (Exception ee){        	
	        }
	        while (!executor.isTerminated()) {   }  
	        Results.printResults();
	        
	        int total = calculateRuntimes(workers, runtimes);
	        
	        System.out.printf("Performance (millis): max: %d, mean: %d %n", runtimes[nthreads-1],
	        		total/nthreads);
	        		
	        System.out.printf("Palindromes computed: %d%n",Results.plainDromeCnt());
	        System.out.printf("Tasks run: %d%n", nthreads);
	}
	private static int calculateRuntimes(PlaindromeMiner workers[], long times[]){
		int total=0;
		for(int i=0; i < workers.length; i++){
			times[i] = workers[i].computeTime();
			total += times[i];
		}
		 java.util.Arrays.sort(times); 
		 return total;
	}
}


class Results{
	private static Map map = new TreeMap();
	
	static synchronized void addData(Object key, String val){
		map.put(key, val);
	}
	
	static synchronized void printResults(){
		   System.out.println("...plaindromes...");
		   Set keys = map.keySet();
		   for (Iterator i = keys.iterator(); i.hasNext();) {
		     Object key =  i.next();
		     Object value = map.get(key);
		     System.out.println(key + " = " + value);
		   }
	}
	static synchronized int plainDromeCnt(){
		return map.size();
	}
}


class PlaindromeMiner implements Runnable{
	final private long min, max;
	private StringBuilder strbld = new StringBuilder();
	private long starttime;
	private long endtime;
	
	PlaindromeMiner(long min, long max){
		this.min=min;
		this.max=max;
	}
	public void run(){
		String  ibuf = "";
		String  bbuf = "";
		starttime = System.currentTimeMillis();
		for(long cnt=min; cnt <= max; cnt++){
			ibuf=Long.toString(cnt);
			if (plaindrome(ibuf)){
				bbuf=Long.toBinaryString(cnt);
				if (plaindrome(bbuf)){
					Results.addData(new Long(cnt), bbuf);
				}
			}
		}
		endtime= System.currentTimeMillis();
		
	}
	private boolean plaindrome(String istr) {
		strbld.setLength(0);
		strbld.append(istr);
		return strbld.reverse().toString().equals(istr);
	}
	long computeTime(){
		return endtime-starttime;
	}
}
