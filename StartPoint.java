
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
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
    
    /***************************************/
    /*************  MAIN *******************/
    /***************************************/
    public static void main(String[] args) {
        Log.log(Thread.currentThread().getName() + ": Prime thread is runnning...");
        Log.log(Thread.currentThread().getName() + ": Starting all the services...");        

        File folder = new File(FILESPATH);
        File[] listOfFiles = folder.listFiles();
       
        
        Log.log(Thread.currentThread().getName() + ": We will process: " + listOfFiles.length + " files.");
        HashMap<String, Integer> result = new HashMap();
        
        //Chrono start
        long start = System.nanoTime();
        
        //Sequential process executed
        result = StartPoint.sequential(listOfFiles);
        
        //Chrono end!
        long time = System.nanoTime() - start;
        
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
        System.out.printf(Thread.currentThread().getName() + ": Sequential: " + listOfFiles.length + " files, " + result.size() +" words matching the length criteria, tasks took %.3f ms%n", time/1e6);
        int nbOccurenceOfMinutes = ( result.get("passage") == null ) ? 0 : result.get("passage");
        System.out.println(Thread.currentThread().getName() + ": Nb occurrences of the word \"passage\": " + nbOccurenceOfMinutes);
        System.out.println(Thread.currentThread().getName() + ": Detailed results can be found in \"results\\out-sequential.txt\"");


        
       /*
        
        
        
        
        
        ExecutorService executor = Executors.newFixedThreadPool(POOLSIZE);
        
        
    Callable<Integer> callable = new Callable<Integer>() {
        @Override
        public Integer call() {
            return 2;
        }
    };
    Future<Integer> future = executor.submit(callable);
    // future.get() returns 2 or raises an exception if the thread dies, so safer
    executor.shutdown();

        //Chrono start
        long start = System.nanoTime();
       

        try {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS); // or longer.    
        } catch (InterruptedException ex) {
            Log.log(Thread.currentThread().getName() + ": Prime thread: InterruptedException:" + ex.getMessage());
        }
        //Chrono end!
        long time = System.nanoTime() - start;
        System.out.printf(Thread.currentThread().getName() + ": Prime thread: Tasks took %.3f ms to run%n", time/1e6);*/
    }
    public static HashMap<String, Integer> mergeResult(HashMap<String,Integer> hm1, HashMap<String,Integer> hm2){    
        HashMap<String, Integer> mergeResult = new HashMap();
              
        for (Map.Entry<String, Integer> e : hm2.entrySet()){
            mergeResult.merge(e.getKey(), e.getValue(),(x, y) -> {return x + y;});
        }
        return mergeResult;
    }
    
    public static HashMap<String, Integer> sequential(File[] filesToProcess){
        HashMap<String, Integer> result = new HashMap();
        Scanner scan = null;
        
        //Iterate through all the files
        for (File file : filesToProcess) {
            if (file.isFile()) {
                try {   
                    try {
                        scan = new Scanner(file);
                        
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
