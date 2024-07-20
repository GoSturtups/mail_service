package io.grpc.examples.mail_server

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import mail_service.Mail
import mail_service.MailServiceGrpcKt
import mail_service.mailResponse
import org.simplejavamail.api.mailer.Mailer
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import java.io.File

lateinit var mailer: Mailer
lateinit var fullChainPath: String
lateinit var privKeyPath: String

class MailServer(private val port: Int, private val validApiKey: String) {
    val server: Server =
        ServerBuilder
            .forPort(port)
            .useTransportSecurity(
                File(fullChainPath),
                File(privKeyPath)
            )
            .addService(MailService())
            .intercept(AuthServerInterceptor(validApiKey))
            .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@MailServer.stop()
                println("*** server shut down")
            },
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    internal class MailService : MailServiceGrpcKt.MailServiceCoroutineImplBase() {
        override suspend fun sendMail(request: Mail.MailRequest): Mail.MailResponse {
            val email = EmailBuilder.startingBlank().apply {
                from(request.fromName, request.fromAddress)
                to(request.toAddress)
                withSubject(request.subject)
                withHTMLText(request.body)
                withHeader("References", request.references ?: getRandomString(16))
                request.attachmentsList.forEach {
                    withAttachment(it.filename, it.content.toByteArray(), it.contentType)
                }
            }.buildEmail()

            mailer.sendMail(email)

            return mailResponse { success = true }
        }
    }

}

fun main() {
    val smtpServer = System.getenv("SMTP_SERVER")!!
    val smtpPort = System.getenv("SMTP_PORT")!!.toInt()
    val login = System.getenv("SMTP_LOGIN")!!
    val password = System.getenv("SMTP_PASSWORD")!!
    val validApiKey = System.getenv("API_KEY")!!

    fullChainPath = System.getenv("FULL_CHAIN")!!
    privKeyPath = System.getenv("PRIV_KEY")!!


    mailer = MailerBuilder
        .withTransportStrategy(TransportStrategy.SMTP_TLS)
        .withDebugLogging(true)
        .withSMTPServer(smtpServer, smtpPort, login, password)
        .buildMailer()

    val port = System.getenv("PORT")?.toInt() ?: 50051
    val server = MailServer(port, validApiKey)
    server.start()
    server.blockUntilShutdown()
}

fun getRandomString(length: Int): String {
    val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

class AuthServerInterceptor(private val validApiKey: String) : ServerInterceptor {
    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>?,
        headers: io.grpc.Metadata?,
        next: ServerCallHandler<ReqT, RespT>?
    ): ServerCall.Listener<ReqT> {
        val apiKeyHeader = io.grpc.Metadata.Key.of("api-key", io.grpc.Metadata.ASCII_STRING_MARSHALLER)
        val apiKey = headers?.get(apiKeyHeader)

        if (validApiKey != apiKey) {
            call?.close(Status.UNAUTHENTICATED.withDescription("Invalid API key"), headers)
            return object : ServerCall.Listener<ReqT>() {}
        }

        return next!!.startCall(call, headers)
    }
}