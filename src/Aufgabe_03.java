public class Aufgabe_03 {
    public static void main(String[] args) {

        double x, Ergebnis;

        //x = 12.45;
        x=0;
        while (x <= 4) {

            Ergebnis = 3 * (Math.pow(x, 2)) - 8 * x + 4;
            System.out.println(" Bei x = " + x + " ergibt die Quadratgleichung den Wert: " + Ergebnis);
            x+=2;

        }
    }


}
