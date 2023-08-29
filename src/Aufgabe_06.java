import java.util.Scanner;


public class Aufgabe_06 {
    public static void main(String[] args) {

        double Kilometer,Liter,durchschnittlichen_Verbrauch;
        Scanner sc = new Scanner(System.in);
        System.out.println("Gefahrene Kilometer? : ");
        Kilometer = sc.nextDouble();
        System.out.println("Verbrauchtes Benzin in Liter? : ");
        Liter = sc.nextDouble();
        durchschnittlichen_Verbrauch = (100*Liter)/Kilometer;
        System.out.println("Sie haben auf 100 km "+durchschnittlichen_Verbrauch+" Liter verbraucht");



    }
}
