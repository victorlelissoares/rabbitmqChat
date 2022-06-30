import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Recv{
    private final static String QUEUE_NAME = "hello";

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