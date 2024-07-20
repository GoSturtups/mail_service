package io.grpc.examples.mail

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.MethodDescriptor
import mail_service.Mail
import mail_service.MailServiceGrpcKt.MailServiceCoroutineStub
import java.io.Closeable
import java.util.concurrent.TimeUnit

class SendMailClient(
    private val apiSecret: String,
    private val port: Int,
): Closeable {
    private val channel: ManagedChannel = ManagedChannelBuilder.forAddress("mail.jomo.events", port).build()

    private val stub: MailServiceCoroutineStub =
        MailServiceCoroutineStub(channel).withInterceptors(AuthClientInterceptor(apiSecret))

    suspend fun sendEmail(request: Mail.MailRequest): Mail.MailResponse = stub.sendMail(request)

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}

class AuthClientInterceptor(private val apiKey: String) : ClientInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT?, RespT?>?,
        callOptions: CallOptions?,
        next: Channel?
    ): ClientCall<ReqT?, RespT?>? {
        return object :
            ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next!!.newCall(method, callOptions)) {
            override fun start(responseListener: Listener<RespT>?, headers: io.grpc.Metadata?) {
                val apiKeyHeader = io.grpc.Metadata.Key.of("api-key", io.grpc.Metadata.ASCII_STRING_MARSHALLER)
                headers?.put(apiKeyHeader, apiKey)
                super.start(responseListener, headers)
            }
        }
    }
}