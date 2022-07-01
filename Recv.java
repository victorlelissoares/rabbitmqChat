
import java.util.Scanner;

public class Recv{

    public static void main(String[] argv) throws Exception {
        Scanner scan = new Scanner(System.in);
        System.out.print("Identificação: ");
        String name = scan.nextLine();

        Client cl = new Client(name);
        cl.prepareBroadcast();
        cl.prepareDirectMessage();
        cl.menuDisplay();

    }
}