import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/*pegar PID*/
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

//ler do teclado
import java.util.Scanner;


public class Client {
    //private final long PID;//apenas para fins de nomeação
    private final String PID; //o pid por enquanto vai ser usado como um nome/identificador
    private Channel channel;//faz conexão do broker

    Client(String name) throws Exception {
        /*RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        String jvmName = bean.getName();//formato: 35656@Krakatau
        this.PID = Long.parseLong(jvmName.split("@")[0]);
        System.out.println("PID  = " + this.PID);*/
        this.PID = name;
        this.channel = connectServer("localhost"); //inicializa a conexão e conecta com o broker

    }

    public Channel connectServer(String server) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(server);
        Connection connection = factory.newConnection();
        /*Channel channel = connection.createChannel();*/

        return connection.createChannel();
    }

    public void sendBroadcastMessage(String message) throws Exception{
        final String EXCHANGE_NAME = "broadcast";
       /* //envia mensagem broadcast
        //this.channel.queueDeclare( String.valueOf(this.PID) + "broadcast", false, false, false, null);

        //declara o exchange
        channel.exchangeDeclare("broadcast", "fanout");
        //declara a fila para recebimento de mensagens broadcast
        this.channel.queueDeclare(this.PID + "broadcast", false, false, false, null);
        //liga o exchange a fila
        this.channel.exchangeBind(this.PID + "broadcast", "broadcast", "");*/
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        //envia mensagem para a exchange
        channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
        System.out.println(" [x] Sent '" + message + "'");
    }

    public void prepareBroadcast() throws Exception{
        final String EXCHANGE_NAME = "broadcast";
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        /*Quando inicia send primeiro
        erro : channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no exchange 'broadcast' in vhost '/', class-id=50, method-id=20)*/
        //preparar uma fila para receber o broadcast
        String queueName = PID + "broadcast";
        //declara a fila
        this.channel.queueDeclare(queueName, false, false, false, null);
        //anexa a filaa exchange "broadcast"
        this.channel.queueBind(queueName, "broadcast", "");

        //função que executara para consumir a mensagem
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        };

        //falta fazer receber mensagens quando o send iniciar antes

        //vai consumir as mensagens de forma assíncrona
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    /*checar passive declaration, que verifica se algo existe ou não*/

    public void prepareDirectMessage() throws  Exception{
        //declara fila só para ele, para receber mensagens diretamente
        this.channel.queueDeclare(this.PID, false, false, false, null);


        //função que executara para consumir a mensagem
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received direct '" + message + "'");
        };


        //vai consumir as mensagens de forma assíncrona
        channel.basicConsume(this.PID, true, deliverCallback, consumerTag -> { });
    }

    public void sendDirectMessage(String user, String messagem) throws Exception {
        String QUEUE_NAME = user;
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        String message = messagem;
        //envia diretamente para a fila
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
    }


    public void menuDisplay() throws Exception{
        Scanner ler = new Scanner(System.in);
        int op;
        String user;
        String message;

        while(true) {
            System.out.println("1.Enviar mensagem a todos os usuários");
            System.out.println("2.Enviar mensagem de um tópico específico");
            System.out.println("3.Enviar mensagem a um usuário");

            op = ler.nextInt();
            //não le o \n, então consome o \n logo depois
            ler.nextLine();

            switch (op) {
                case 1:
                    System.out.print("Digite a mensagem: ");
                    message = ler.nextLine();
                    sendBroadcastMessage(message);
                    break;
                case 3:
                    System.out.print("Digite o nome do usuário: ");

                    user = ler.nextLine();
                    System.out.print("Digite a mensagem: ");
                    message = ler.nextLine();
                    sendDirectMessage(user, message);
                    break;
                default:
                    System.out.println("Opção Invalida!");
                    break;
            }
        }
    }


}
