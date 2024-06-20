public class Module9{
    public static void main(String[] args) {
        Number n1 = new Number(true);
        Number.loop = Integer.parseInt(args[0]) * 100;
        Number n2 = new Number(false);
        n1.start();
        n2.start();
        try{
            n1.join();
            n2.join();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}

class Number extends Thread{
    static int number = 0;
    boolean increment;
    static int loop;

    static int numIncrements = 0;
    static int numDecrements = 0;

    public Number(boolean type){
        increment = type;
    }

    @Override
    public void run() {
        for(int i=0; i < loop; i++){
            if(increment)
                inc();
            else 
                dec();
            assert number == (numIncrements - numDecrements) : "RaceCondition occurred";
        }
    }

    private void inc(){
        number++;
        numIncrements++;
    }

    private void dec(){
        number--;
        numDecrements++;
    }
}