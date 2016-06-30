
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
        result = StartPoint.sequential(listOfFiles);
        
        //Chrono end!
        long time = System.nanoTime() - start;
        
        File file=new File( RESULTPATH + "out-sequential.txt");   
        try {
            FileWriter fw=new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw=new BufferedWriter(fw);
            bw.write(result.toString());
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(StartPoint.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        
        
        Log.log(result.toString());
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
    
    public static HashMap<String, Integer> sequential(File[] filesToProcess){
        HashMap<String, Integer> result = new HashMap();
        
        
        //Iterate through all the files
        for (File file : filesToProcess) {
            if (file.isFile()) {
                try {
                    Scanner scan = new Scanner(file); 
                    
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
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(StartPoint.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }      
        return result;
    }
}
