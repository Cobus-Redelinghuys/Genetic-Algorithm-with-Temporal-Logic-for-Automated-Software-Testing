import java.io.File;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Module8{
    public static void main(String[] args) {
        try{
            ExmpleThread.set(args[1]);
            Integer[] fileContents = ExmpleThread.readFile(args[1]);
            ExmpleThread et1 = new ExmpleThread(Integer.parseInt(args[0]), fileContents[1]);
            ExmpleThread et2 = new ExmpleThread(fileContents[1], fileContents[2]);
            et1.start();
            et2.start();
            et1.join();
            et2.join();
            for(int i=0; i < ExmpleThread.resources.length; i++){
                System.out.print(ExmpleThread.resources[i]);    
            }
            System.out.println();
        }catch(Exception e){
            e.printStackTrace();
        }
    }  
}

class ExmpleThread extends Thread{
    int index;
    int nIndex;
    static Boolean[] resources;
    static Lock[] locks;

    ExmpleThread(int index, int nIndex){
        this.index = Math.abs(index);
        this.nIndex = Math.abs(nIndex);
    }

    static void set(String path){
        Integer[] fileContents = readFile(path);
        resources = new Boolean[fileContents[0]];
        locks = new ReentrantLock[fileContents[0]];
        for(int i=0; i < resources.length; i++){
            resources[i] = (i%2 == 0);
            locks[i] = new ReentrantLock();
        }
    }

    public static Integer[] readFile(String path){
        Integer[] returnVal = new Integer[3];
        try {
            File myObj = new File(path+"/Instructor_Solution/Module8/config.txt");
            //File myObj = new File("./config.txt");
            Scanner myReader = new Scanner(myObj);
            int i = 0;
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                returnVal[i%3] = Integer.parseInt(data);
                if(i >= 2)
                    break;
                i++;
            }
            myReader.close();
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return returnVal;
    }

    @Override
    public void run() {
        if(index == nIndex){
            return;
        }
        for(int i=0; i < 100; i++){
            try{
                locks[index].lock();
                locks[nIndex].lock();
                resources[index] = !resources[nIndex];
                locks[nIndex].unlock();
                locks[index].unlock();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}