# rabbitmqChat

Matheus Vieira de Souza, 
Victor Lelis Soares



Chat para comunicação em Grupo e Ponto-a-Ponto.

Instalação: Primeiramente, precisamos instalar o rabbitMQ, que pode ser feito através do docker, usando:

docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.10-management

Ou instalado nativamente, segue o link para o tutorial: https://www.rabbitmq.com/download.html

Após isso, é necessário baixar o client java do rabbitMQ:
https://search.maven.org/search?q=g:com.rabbitmq%20AND%20a:amqp-client

E os arquivos de biblioteca, SLF4J API and SLF4J Simple:
https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.26/slf4j-api-1.7.26.jar

https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.26/slf4j-simple-1.7.26.jar

Copie esses arquivos para o diretório que tenha os arquivos java que vão utilizar o rabbitMQ.

Para compilar, use:

javac -cp amqp-client-5.15.0.jar Send.java Recv.java Client.java

e para executar:

java -cp .:amqp-client-5.15.0.jar:slf4j-api-1.7.26.jar:slf4j-simple-1.7.26.jar Recv



