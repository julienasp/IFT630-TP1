
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.Log;
import core.FilesHandling;

public class StartPoint {
    /************************************/
    /****  PUBLIC STATIC ATTRIBUTES *****/
    /************************************/
    public static final String FILESPATH = "files/";
    public static final String RESULTPATH = "results/";    
    public static final int POOLSIZE = Runtime.getRuntime().availableProcessors();
    public static ExecutorService executor = Executors.newCachedThreadPool();
    
    
    /***************************************/
    /*************  MAIN *******************/
    /***************************************/
    public static void main(String[] args) {
        //Chrono start
        long globalChronoStart = System.nanoTime();
        Log.log(Thread.currentThread().getName() + ": Prime thread is runnning...");
        Log.log(Thread.currentThread().getName() + ": Starting all the services...");        

        File folder = new File(FILESPATH);
        File[] listOfFiles = folder.listFiles();
       
        
        Log.log(Thread.currentThread().getName() + ": We will process: " + listOfFiles.length + " files.");
        HashMap<String, Integer> result = new HashMap();
        
        //Chrono start
        long senquentialStart = System.nanoTime();
        
        FilesHandling fh = new FilesHandling(executor);
        //Sequential process executed
        result = fh.sequential(listOfFiles,0,listOfFiles.length-1);
        
        //Chrono end!
        long senquentialTime = System.nanoTime() - senquentialStart;
        
        try{
            //Writing down the result
            File file=new File( RESULTPATH + "out-sequential.txt");
            BufferedWriter bw = null;
            try {
                FileWriter fw=new FileWriter(file.getAbsoluteFile());
                bw=new BufferedWriter(fw);
                bw.write(result.toString());    
            } finally{
                if(bw != null) bw.close();            
            }
        }catch(IOException ex){
            Logger.getLogger(StartPoint.class.getName()).log(Level.SEVERE, null, ex);            
        }
                
        //Printing statistics        
        System.out.printf(Thread.currentThread().getName() + ": (Sequential): " + listOfFiles.length + " files, " + result.size() +" words matching the length criteria, tasks took %.3f ms%n", senquentialTime/1e6);
        int nbOccurenceOfMinutes = ( result.get("passage") == null ) ? 0 : result.get("passage");
        System.out.println(Thread.currentThread().getName() + ": Nb occurrences of the word \"passage\": " + nbOccurenceOfMinutes);
        System.out.println(Thread.currentThread().getName() + ": Detailed results can be found in \"results\\out-sequential.txt\"");

        //Parallel execution
        HashMap<String, Integer> parallelResult = new HashMap();
        try {
            int loopCount = 0;
            for (int threshold = 256; threshold <= 8192; threshold *= 2)
            {               
                long parallelStart = System.nanoTime();
                parallelResult = fh.parallel(listOfFiles, 0, listOfFiles.length-1, threshold);
                long parallelTime = System.nanoTime() - parallelStart;
                
                try{
                    //Writing down the result
                    File file=new File( RESULTPATH + "out-parallel-"+loopCount+".txt");
                    BufferedWriter bw = null;
                    try {
                        FileWriter fw=new FileWriter(file.getAbsoluteFile());
                        bw=new BufferedWriter(fw);
                        bw.write(parallelResult.toString());    
                    } finally{
                        if(bw != null) bw.close();            
                    }
                }catch(IOException ex){
                    Logger.getLogger(StartPoint.class.getName()).log(Level.SEVERE, null, ex);            
                }
                
                //Printing statistics        
                System.out.printf("%n"+Thread.currentThread().getName() + ": (Parallel): " + listOfFiles.length + " files, " + result.size() +" words matching the length criteria, tasks took %.3f ms (Sequential threshold: %d)%n", parallelTime/1e6, threshold);
                nbOccurenceOfMinutes = ( parallelResult.get("passage") == null ) ? 0 : parallelResult.get("passage");
                System.out.println(Thread.currentThread().getName() + ": Nb occurrences of the word \"passage\": " + nbOccurenceOfMinutes);
                System.out.println(Thread.currentThread().getName() + ": Detailed results can be found in \"results\\out-parallel-"+loopCount+".txt\"");
                   
                loopCount++;
            } 
        } catch (InterruptedException ex) {
            Logger.getLogger(StartPoint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(StartPoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       try {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS); // or longer.    
        } catch (InterruptedException ex) {
            Log.log(Thread.currentThread().getName() + ": Prime thread: InterruptedException:" + ex.getMessage());
        }
       //Chrono end!
       long globalTime = System.nanoTime() - globalChronoStart;  

        System.out.printf(Thread.currentThread().getName() + ": Prime thread: Tasks took %.3f ms to run%n", globalTime/1e6);
    }
}
