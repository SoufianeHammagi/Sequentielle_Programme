import java.util.Scanner;

public class Aufgabe_07 {
    public static void main(String[] args) {

        final double Pi =  3.1415927;
        double Radius,Flaeche,Umfang;
        Scanner sc = new Scanner(System.in);
        System.out.print("Geben Sie den Radius des Kreises ein: ");
        Radius = sc.nextDouble();
        Flaeche = Pi*(Math.pow(Radius,2));
        Umfang = 2*Pi*Radius;
        System.out.println("Fläche : "+ Flaeche);
        System.out.println("Umfang : "+ Umfang);




    }
}
