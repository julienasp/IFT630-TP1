
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

public class StartPoint {
    /************************************/
    /****  PUBLIC STATIC ATTRIBUTES *****/
    /************************************/
    public static final String FILESPATH = "files/";
    public static final String RESULTPATH = "results/";
    public static final int WORDLENGTHCRTERIA = 7;
    public static final int POOLSIZE = Runtime.getRuntime().availableProcessors();
    public static ExecutorService executor = Executors.newCachedThreadPool();
    //public static ExecutorService executor = Executors.newSingleThreadExecutor();
    
    
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
        
        //Sequential process executed
        result = StartPoint.sequential(listOfFiles,0,listOfFiles.length-1);
        
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
        System.out.printf(Thread.currentThread().getName() + ": Sequential: " + listOfFiles.length + " files, " + result.size() +" words matching the length criteria, tasks took %.3f ms%n", senquentialTime/1e6);
        int nbOccurenceOfMinutes = ( result.get("passage") == null ) ? 0 : result.get("passage");
        System.out.println(Thread.currentThread().getName() + ": Nb occurrences of the word \"passage\": " + nbOccurenceOfMinutes);
        System.out.println(Thread.currentThread().getName() + ": Detailed results can be found in \"results\\out-sequential.txt\"");

        //Parallel execution
        HashMap<String, Integer> parallelResult = new HashMap();
        try {
            int loopCount = 0;
            for (int threshold = 256; threshold <= 4096; threshold *= 2)
            {               
                long parallelStart = System.nanoTime();
                parallelResult = StartPoint.parallel(listOfFiles, 0, listOfFiles.length-1, threshold);
                long parallelTime = System.nanoTime() - parallelStart;
                
                try{
                    //Writing down the result
                    File file=new File( RESULTPATH + "out-parallel-"+loopCount+".txt");
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
                System.out.printf(Thread.currentThread().getName() + ": Parallel: " + listOfFiles.length + " files, " + result.size() +" words matching the length criteria, tasks took %.3f ms (Sequential threshold: %d%n)", parallelTime/1e6, threshold);
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
    
    public static HashMap<String, Integer> mergeResult(HashMap<String,Integer> hm1, HashMap<String,Integer> hm2){
        Log.log(Thread.currentThread().getName() + ": mergeResult(): is being executed...");
        HashMap<String, Integer> mergeResult = new HashMap();
              
        for (Map.Entry<String, Integer> e : hm2.entrySet()){
            mergeResult.merge(e.getKey(), e.getValue(),(x, y) -> {return x + y;});
        }
        Log.log(Thread.currentThread().getName() + ": mergeResult(): has been executed.");
        return mergeResult;
    }
    
    public static HashMap<String, Integer> parallel(File[] filesToProcess,Integer start,Integer end,Integer threshold) throws InterruptedException, ExecutionException{
        Log.log(Thread.currentThread().getName() + ": parallel(): is being executed...");
        Log.log(Thread.currentThread().getName() + ": parallel(): Threashold is : " + threshold);
        Log.log(Thread.currentThread().getName() + ": parallel(): start index is: " + start);
        Log.log(Thread.currentThread().getName() + ": parallel(): end index is: " + end);
        HashMap<String, Integer> result = new HashMap();
        Integer n = (end + 1) - start; // length
        if (n <= threshold){
            return sequential(filesToProcess, start, end);
        }        
        Future<HashMap<String, Integer>> f0 = executor.submit(
          new Callable<HashMap<String, Integer>>() {
              @Override
              public HashMap<String, Integer> call() throws InterruptedException, ExecutionException {                  
                  Log.log(Thread.currentThread().getName() + ": f0 call(): is being executed...");
                  return StartPoint.parallel(filesToProcess,start,end/2,threshold);
              }
          });
        Future<HashMap<String, Integer>> f1 = executor.submit(
          new Callable<HashMap<String, Integer>>() {
              @Override
              public HashMap<String, Integer> call() throws InterruptedException, ExecutionException{
                  Log.log(Thread.currentThread().getName() + ": f1 call(): is being executed...");
                  return StartPoint.parallel(filesToProcess,((end/2)+1),end,threshold);
              }
          });
        Log.log(Thread.currentThread().getName() + ": parallel(): has been executed.");        
        return mergeResult(f0.get(),f1.get());
    }
    
    public static HashMap<String, Integer> sequential(File[] filesToProcess, Integer start,Integer end){
        HashMap<String, Integer> result = new HashMap();
        Scanner scan = null;
        
        //Iterate through all the files        
        for (Integer i = start; i <= end; i++){
            if (filesToProcess[i].isFile()) {
                try {   
                    try {
                        scan = new Scanner(filesToProcess[i]);
                        
                        //Read everyword of a file
                        while(scan.hasNext()){
                            String currentWord = scan.next();

                            //If the word is big enough we process it
                            if(currentWord.length() == StartPoint.WORDLENGTHCRTERIA){

                                //If the word is already in the hashtable we incremente it
                                if(result.containsKey(currentWord)){
                                    result.replace(currentWord, result.get(currentWord) + 1);
                                }
                                //Otherwise we add it
                                else{
                                    result.put(currentWord, 1);
                                }
                            }
                        }
                    } finally{
                        if (scan != null) scan.close(); 
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(StartPoint.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }      
        return result;
    }
}
