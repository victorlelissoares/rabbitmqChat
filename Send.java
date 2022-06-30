import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;


public class Send {
    //nome da fila
    private static final String EXCHANGE_NAME = "broadcast";

    public static void main(String[] argv) throws Exception {
        Scanner scan = new Scanner(System.in);
        System.out.println("Seu Nome? ");
        String name = scan.nextLine();

        Client cl = new Client(name);
        cl.prepareBroadcast();
        cl.prepareDirectMessage();
        cl.menuDisplay();
    }
}