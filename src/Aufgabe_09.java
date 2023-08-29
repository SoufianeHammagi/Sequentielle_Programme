import java.util.Scanner;

public class Aufgabe_09 {
    public static void main(String[] args) {
        double endgeschwindigkeit, kmh;
        //Fallzeit t
        double t;
        //Fallhöhe s
        double s;
        //Erdbeschleunigung g
        final double  G = 9.81;
        Scanner sc = new Scanner(System.in);

        System.out.print("Fallhoehe: ");
        s = sc.nextDouble();
        t=Math.sqrt((2*s/G));
        endgeschwindigkeit = t*G;
        kmh = endgeschwindigkeit*3.6;

        System.out.println("Endgeschwindigkeit in m/s: "+endgeschwindigkeit);
        System.out.println("Endgschwindigkeit in km/h: "+kmh);
        sc.close();

    }
}
