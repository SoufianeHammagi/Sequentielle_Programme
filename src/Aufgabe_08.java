import java.util.Scanner;

public class Aufgabe_08 {
    public static void main(String[] args) {

        int zahl = 0,gros,schock,dutzend, stueck,rest;
        boolean Bedingung;

        Scanner sc = new Scanner(System.in);
        Bedingung = false;

        while(!Bedingung){
            System.out.print("Geben Sie eine ganze Zahl größer oder gleich 0 ein! : ");
            zahl = sc.nextInt();
            if(zahl < 0){
                System.out.println("Die von Ihnen eingegebene Zahl ist nicht größer oder gleich Null : ");
            }else{
                Bedingung = true;
            }
        }

        gros= zahl/144;
        rest = zahl%144;

        schock = rest/60;
        rest = rest%60;

        dutzend = rest/12;
        rest = rest%12;
        stueck = rest;

        System.out.println("\n" +gros + " Gros\n" +schock + " Schock\n" + dutzend + " Dutzend\n" + stueck + " Stueck");
        sc.close();



    }
}
