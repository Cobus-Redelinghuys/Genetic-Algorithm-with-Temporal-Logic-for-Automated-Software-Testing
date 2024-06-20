import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.System;

public class Module10 {
    public static void main(String[] args) {
        StarvationExample starvationExample = new StarvationExample(Math.abs(Integer.parseInt(args[0])), args[1]);
        starvationExample.run();
    }
}

class StarvationExample {
    static HashMap<Integer, ThreadExecutor> executorOrder = new HashMap<>();
    static HashMap<Integer, AtomicBoolean> executed = new HashMap<>();
    static ArrayList<Integer> order;
    static int example;

    public StarvationExample(int example, String path){
        int size = readFile(path);
        for(int i=0; i <= size; i++){
            executorOrder.put(i, new ThreadExecutor(i));
        }
        executorOrder.put(example, new ThreadExecutor(example));
        order = new ArrayList<>(executorOrder.keySet());
        Collections.sort(order);
        StarvationExample.example = example;
        for(Integer i : order){
            executed.put(i, new AtomicBoolean(false));
        }
    }
    
    public static Integer readFile(String path){
        Integer returnVal = 0;
        try {
            File myObj = new File(path+"/Instructor_Solution/Module10/config.txt");
            //File myObj = new File("./Config.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                // returnVal[i%3] = Integer.parseInt(data);
                // if(i >= 0)
                //     break;
                // i++;
                returnVal = Integer.parseInt(data);
                break;
            }
            myReader.close();
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return returnVal;
    }

    public void run(){
        while(true){
            for(int i=0; i < order.size(); i++){
                System.out.println(i);
                if(!executed.get(i).get()){
                    executed.replace(i, new AtomicBoolean(true));
                    executorOrder.get(i).start();
                    executorOrder.replace(i, new ThreadExecutor(i));
                    i=-1;
                }
                if(i == example){
                    java.lang.System.exit(0);
                }
            }
        }
    }
}

class ThreadExecutor extends Thread{
    public int priority;

    public ThreadExecutor(int p){
        priority = p;
    }

    @Override
    public void run() {
        System.out.println("Thread with Priority " + priority + " executed");
        try{
            Thread.sleep(20);
        }catch(Exception e){
            e.printStackTrace();
        }
        StarvationExample.executed.replace(priority, new AtomicBoolean(false));
    }
}