public class Main {
    public static void main(String[] args) {
        String binaryString = args[0];
        try {
            Long v = Long.parseLong(binaryString, 2);
            System.out.println(v.intValue());
        } catch (Exception e) {
            System.out.println("Invalid value");
        }

    }
}