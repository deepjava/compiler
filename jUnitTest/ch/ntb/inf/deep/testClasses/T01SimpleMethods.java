package ch.ntb.inf.deep.testClasses;

public class T01SimpleMethods {
    public static void emptyMethodStatic() {
    }
    
    public void emptyMethod() {
    }

    public static void assignment1() {
        int x = 1;
    }

    public static void simple1(int y) {
        int x = 0;
        x++;
        x += 3;
        x--;  
        x = y + 1;
    }

    public static void simple2() {
        
        int a = 1;
        int b = 2;
        a = 1 + 2;
        a = b; 
        a = a + b;
    }
    
    public static void simple3() {
        int x = 1;
        int y = 2;
        x = x + y;
        y = x + x;        
    }

}
