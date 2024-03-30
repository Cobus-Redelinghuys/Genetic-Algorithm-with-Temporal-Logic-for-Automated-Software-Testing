public class Main{
    public static void main(String[] args) {
        String binaryString = args[0];
        Long v = Long.parseLong(binaryString, 2);
        System.out.println(v.intValue());
    }
}