/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.Log;


/**
 *
 * @author JUASP-G73-Android
 */
public class FilesHandling  {
    public static final int WORDLENGTHCRTERIA = 7;
    private ExecutorService executor;
    
    public FilesHandling(ExecutorService ex) {
        executor = ex;
    }
    
    public HashMap<String, Integer> mergeResult(HashMap<String,Integer> hm1, HashMap<String,Integer> hm2){
        HashMap<String, Integer> mergeResult = new HashMap(hm1);
        
        for (Map.Entry<String, Integer> e : hm2.entrySet()){
            mergeResult.merge(e.getKey(), e.getValue(),(x, y) -> {return x + y;});
        }
        
        return mergeResult;
     }

     public HashMap<String, Integer> parallel(File[] filesToProcess,Integer start,Integer end,Integer threshold) throws InterruptedException, ExecutionException{
        Integer n = (end + 1) - start; // length condition
        Integer midIndex = start + (end-start) / 2; // index for the array split    
         
        if (n <= threshold){
            return sequential(filesToProcess, start, end);
        }

        Future<HashMap<String, Integer>> f0 = executor.submit(
          new Callable<HashMap<String, Integer>>() {
              @Override
              public HashMap<String, Integer> call() throws InterruptedException, ExecutionException { 
                  return parallel(filesToProcess,start,midIndex,threshold);
              }
          });
        
        Future<HashMap<String, Integer>> f1 = executor.submit(
          new Callable<HashMap<String, Integer>>() {
              @Override
              public HashMap<String, Integer> call() throws InterruptedException, ExecutionException{
                  return parallel(filesToProcess,midIndex+1,end,threshold);
              }
          });

        return mergeResult(f0.get(),f1.get());
     }

     public HashMap<String, Integer> sequential(File[] filesToProcess, Integer start,Integer end){
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
                            if(currentWord.length() == WORDLENGTHCRTERIA){

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

                }
            }
        }      
        return result;
    }
}
