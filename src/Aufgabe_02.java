import java.util.Scanner;

public class Aufgabe_02 {
    public static void main(String[] args) {



        double zahl1, zahl2;
        Scanner sc = new Scanner(System.in);



        System.out.print("Geben Sie die erste Variable ein!: ");
        zahl1 = sc.nextDouble();
        System.out.print("Geben Sie die zweite Variable ein!: ");
        zahl2 = sc.nextDouble();

        double summe = zahl1+zahl2;
        double differenz = zahl1-zahl2;

        System.out.println("die Summe der beiden Variablen ist: "+summe);
        System.out.println("Differenz der beiden Variablen ist:: "+ differenz);
        sc.close();
    }
}
