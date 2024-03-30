import java.io.File;
import java.util.Scanner;

public class Main{
    public static int readFile(String path){
        int returnVal = 0;
        try {
            File myObj = new File(path+"/Instructor_Solution/Module4/config.txt");
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
        String path = args[1];
        Integer[] arr = new Integer[readFile(path)];
        for(int i=0; i < arr.length; i++){
            arr[i] = i;
        }
        int index = Integer.parseInt(args[0]);
        System.out.println(arr[index%arr.length]);

    }
}