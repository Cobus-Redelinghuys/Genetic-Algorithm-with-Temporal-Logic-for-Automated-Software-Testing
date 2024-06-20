import java.io.File;
import java.util.Scanner;

public class Main{
    public static int readFile(String path){
        int returnVal = 0;
        try {
            File myObj = new File(path+"./Student_Solution/Module6/config.txt");
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

        while(n > v){
        }
        System.out.println("Finished Loop");

    }
}