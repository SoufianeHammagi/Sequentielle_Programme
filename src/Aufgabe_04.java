import java.util.Scanner;

public class Aufgabe_04 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
    double celsius,fahrenheit;

        System.out.print("Geben Sie die Temperatur in Celsius ein: ");
        celsius = sc.nextDouble();

        fahrenheit = ((double) 9 /5)*celsius+32;

        System.out.println(celsius+" Grad Celsius sind "+fahrenheit+" Grad Fahrenheit");
        sc.close();

    }
}
