import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import java.nio.charset.StandardCharsets

fun sendMessage(message : String){
    val uri = System.getenv("RABBIT_URI")
    val factory = ConnectionFactory()
    factory.setUri(uri)
    factory.newConnection().use { connection ->
        connection.createChannel().use {
                channel ->
            channel.queueDeclare("t_queue", false, false, false, null)
            channel.basicPublish(
                "",
                "t_queue",
                null,
                message.toByteArray(StandardCharsets.UTF_8)
            )
            ("[x] Sent '$message'")
        }
    }
}

fun listener(){
    val uri = System.getenv("RABBIT_URI")
    val factory = ConnectionFactory()
    factory.setUri(uri)
    val connection = factory.newConnection()
    val channel = connection.createChannel()
    val consumerTag = "SimpleConsumer"

    channel.queueDeclare("t_queue", false, false, false, null)

    println("[$consumerTag] Waiting for messages...")
    val deliverCallback = DeliverCallback{ consumerTag: String?, delivery: Delivery ->
        val message = String(delivery.body, StandardCharsets.UTF_8)
        println("[$consumerTag] Received message: '$message'")
    }
    val cancelCallback = CancelCallback { consumerTag: String ->
        println("[$consumerTag] was canceled")
    }
    channel.basicConsume("t_queue", true, consumerTag, deliverCallback, cancelCallback)
}

fun main() {
    for (i in 1..10){
        sendMessage("Hello World in $i time!")
    }

    listener()
}