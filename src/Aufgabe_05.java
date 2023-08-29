import java.util.Scanner;

public class Aufgabe_05 {
    public static void main(String[] args) {
        double liter, preis,Nettobetrag,Umsatzsteuer,Bruttobetrag;
        Scanner sc = new Scanner(System.in);
        //System.out.print("Bitte Getankte Liter eintragen!: ");
        liter = 45;//sc.nextDouble();
        //System.out.print("Bitte geben Sie den Nettopreis pro Liter in EUR an!: ");
        preis = 1.50;//sc.nextDouble();
        Nettobetrag = liter * preis;
        Umsatzsteuer = Nettobetrag * 0.19;
        Bruttobetrag = Umsatzsteuer + Nettobetrag;
        System.out.println("Getankte Liter : "+liter);
        System.out.println("Nettopreis je Liter in EUR : "+preis);
        System.out.println("Nettobetrag : "+Nettobetrag);
        System.out.println("Umsatzsteuer : "+Umsatzsteuer);
        System.out.println("Bruttobetrag : "+Bruttobetrag);


    }
}
