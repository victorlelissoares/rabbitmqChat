import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;


public class Send {
    //nome da fila
    private static final String EXCHANGE_NAME = "broadcast";

    public static void main(String[] argv) throws Exception {

        Client cl = new Client("teste");
        cl.prepareBroadcast();
        cl.menuDisplay();
    }
}