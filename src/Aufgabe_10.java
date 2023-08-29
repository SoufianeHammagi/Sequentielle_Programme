import java.util.Scanner;

public class Aufgabe_10 {
    public static void main(String[] args) {

        int april, mai, juni;
        double durchschnitt;
        Scanner sc = new Scanner(System.in);

        System.out.println("Niederschlag im April :  ");
        april = sc.nextInt();
        System.out.println("Niederschlag im Mai :  ");
        mai = sc.nextInt();
        System.out.println("Niederschlag im juni :  ");
        juni = sc.nextInt();
        durchschnitt = (double)(april + mai + juni)/3;

        System.out.println("Durchschnitt: \t"+durchschnitt);
        sc.close();

    }
}
