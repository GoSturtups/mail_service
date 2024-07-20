
package io.grpc.examples.helloworld

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import mail_service.MailServiceGrpcKt
import mail_service.MailServiceGrpcKt.MailServiceCoroutineStub
import mail_service.mailRequest
import java.io.Closeable
import java.util.concurrent.TimeUnit

class HelloWorldClient(private val channel: ManagedChannel) : Closeable {
    private val stub: MailServiceGrpcKt.MailServiceCoroutineStub = MailServiceCoroutineStub(channel)
    suspend fun sendMail() {
        val request = mailRequest {
            fromName = "Gleb"
            fromAddress = "no-replay@jomo.events"
            toAddress = "klgleb@gmail.com"
            subject = "Test email"
            body = "<b>This is test</b> email"

        }
        val response = stub.sendMail(request)
        println("Received: ${response.message}")
    }

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}


suspend fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 50051

    val channel = ManagedChannelBuilder.forAddress("mail.jomo.events", port).usePlaintext().build()

    val client = HelloWorldClient(channel)

    client.sendMail()
}
