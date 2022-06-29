import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;

public class Recv{
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        Client cl = new Client("roi");
        cl.prepareBroadcast();//recebe broadcast

        while(true){
            ;
        }

    }
}