import java.io.File;
import java.util.Scanner;

public class Module7{
    public static void func(int n, int v){
        if(n < v){
            func(n, v-1);
        }
    }

    public static int readFile(String path){
        int returnVal = 0;
        try {
            File myObj = new File(path+"/Instructor_Solution/Module7/config.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
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

    public static void main(String[] args) {
        int n = readFile(args[1]);
        int v = Integer.parseInt(args[0]);
        func(n,v);
    }
}