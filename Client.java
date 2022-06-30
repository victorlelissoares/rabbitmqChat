import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/*pegar PID*/
import java.io.*;

//ler do teclado
import java.util.Scanner;


public class Client {
    //private final long PID;//apenas para fins de nomeação
    private final String PID; //o pid por enquanto vai ser usado como um nome/identificador
    private final Channel channel;//faz conexão do broker

    private final File history;

    Client(String name) throws Exception {
        /*RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        String jvmName = bean.getName();//formato: 35656@Krakatau
        this.PID = Long.parseLong(jvmName.split("@")[0]);
        System.out.println("PID  = " + this.PID);*/
        this.PID = name;
        this.channel = connectServer("localhost"); //inicializa a conexão e conecta com o broker

        history = new File(this.PID+"History.txt");
        //caso o arquivo não exista, cria ele
        if(!history.exists()){
            history.createNewFile();
        }
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
//            System.out.println(" [x] Received '" + message + "'");

            try {
                //escreve num arquivo
                FileWriter fw = new FileWriter(this.history, false);
                fw.write("Broadcast: " + message + "\n");
                fw.flush();
                fw.close();
            }
            catch (IOException ioException){
                ioException.printStackTrace();
            }
        };

        //falta fazer receber mensagens quando o send iniciar antes

        //vai consumir as mensagens de forma assíncrona
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    /*checar passive declaration, que verifica se algo existe ou não*/

    public void prepareDirectMessage() throws Exception{
        //declara fila só para ele, para receber mensagens diretamente
        this.channel.queueDeclare(this.PID, false, false, false, null);


        //função que executara para consumir a mensagem
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
//            System.out.println(" [x] Received direct '" + message + "'");
            try {
                //escreve num arquivo
                FileWriter fw = new FileWriter(this.history, false);
                fw.write(message + "\n");
                fw.flush();
                fw.close();
            }
            catch (IOException ioException){
                ioException.printStackTrace();
            }
        };


        //vai consumir as mensagens de forma assíncrona
        channel.basicConsume(this.PID, true, deliverCallback, consumerTag -> { });
    }

    public void sendDirectMessage(String user, String messagem) throws Exception {
        String QUEUE_NAME = user;
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        String message = "Direct(" + this.PID + "): " + messagem;
        //envia diretamente para a fila
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
    }

    public void displayMessages() throws Exception{
        FileReader fr = new FileReader(this.history);
        BufferedReader br = new BufferedReader( fr );

        //enquanto houver mais linhas
        while( br.ready() ){
            //lê a proxima linha
            String linha = br.readLine();
            System.out.println(linha);
            //faz algo com a linha
        }

        //fecha arquivo
        br.close();
        fr.close();
    }

    public void prepareTopic(String topics[]) throws Exception{
        final String EXCHANGE_NAME = "topic";
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        /*Quando inicia send primeiro
        erro : channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no exchange 'broadcast' in vhost '/', class-id=50, method-id=20)*/
        //preparar uma fila para receber o broadcast
        String queueName = PID + "topic";
        //declara a fila
        this.channel.queueDeclare(queueName, false, false, false, null);

        //anexa a fila a exchange "topic"
        for (String topic : topics)
            this.channel.queueBind(queueName, EXCHANGE_NAME, topic);

        //função que executara para consumir a mensagem
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
//            System.out.println(" [x] Received '" + message + "'");

            try {
                //escreve num arquivo
                FileWriter fw = new FileWriter(this.history, false);
                fw.write("Topic: " + delivery.getEnvelope().getRoutingKey() + ":" + message + "\n");
                fw.flush();
                fw.close();
            }
            catch (IOException ioException){
                ioException.printStackTrace();
            }
        };

        //falta fazer receber mensagens quando o send iniciar antes

        //vai consumir as mensagens de forma assíncrona
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });

    }

    public void sendTopicMessage(String topic, String message) throws Exception{
        String EXCHANGE_NAME = "topic";
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        String severity = topic;

        channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes("UTF-8"));
        System.out.println(" [x] Sent '" + severity + "':'" + message + "'");
    }

    public void unsubscribeTopic(String topic) throws Exception{
        try {
            channel.exchangeDeclarePassive("topic");
            channel.queueUnbind(PID+"topic", "topic", topic);
        }catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    public void menuDisplay() throws Exception{
        Scanner ler = new Scanner(System.in);
        int op;
        String user;
        String message;
        String topic;

        while(true) {
            System.out.println("Use CTRL+C para sair.");
            System.out.println("1.Enviar mensagem a todos os usuários.");
            System.out.println("2.Enviar mensagem a um usuário.");
            System.out.println("3.Enviar mensagem de um tópico específico.");
            System.out.println("4.Inscrever-se em tópicos de interesse.");
            System.out.println("5.Desmarcar interesse em tópicos.");
            System.out.println("6.Mostrar Histórico de mensagens.");


            op = ler.nextInt();
            //não le o \n, então consome o \n logo depois
            ler.nextLine();

            switch (op) {
                case 1:
                    System.out.print("Digite a mensagem: ");
                    message = ler.nextLine();
                    sendBroadcastMessage(message);
                    break;

                case 2:
                    System.out.print("Digite o nome do usuário: ");
                    user = ler.nextLine();
                    System.out.print("Digite a mensagem: ");
                    message = ler.nextLine();
                    sendDirectMessage(user, message);
                    break;

                case 3:
                    System.out.print("Digite o nome do tópico: ");
                    user = ler.nextLine();
                    System.out.print("Digite a mensagem: ");
                    message = ler.nextLine();
                    sendTopicMessage(user, message);
                    break;

                case 4:
                    System.out.println("Digite os tópicos de interesse, separados por \";\".");
                    topic = ler.nextLine();
                    String topics [] = topic.split(";");
                    for (String pr : topics)
                        System.out.println(pr);
                    prepareTopic(topics);
                    break;

                case 5:
                    System.out.print("Digite os tópicos de interesse: ");
                    topic = ler.nextLine();
                    unsubscribeTopic(topic);
                    break;

                case 6:
                    displayMessages();
                    break;

                default:
                    System.out.println("Opção Invalida!");
                    break;
            }
        }
    }


}
