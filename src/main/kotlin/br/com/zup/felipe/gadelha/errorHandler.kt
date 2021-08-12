package br.com.zup.felipe.gadelha

import com.google.protobuf.Timestamp
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.StatusRuntimeException
import java.time.LocalDateTime
import java.time.ZoneId

fun invalidArgumentErrorHandler(description: String): StatusRuntimeException =
    Status.INVALID_ARGUMENT
    .withDescription(description)
    .asRuntimeException()

fun invalidArgument(errors: MutableIterable<ValidationExceptionDetails.Errors>): ValidationExceptionDetails {
    val instant = LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()
    return ValidationExceptionDetails.newBuilder()
        .setTitle(Code.INVALID_ARGUMENT.name)
        .setCode(1)
        .setMessage("Check the error field(s)")
        .setStatus(Code.INVALID_ARGUMENT)
        .setTimestamp(Timestamp.newBuilder()
            .setSeconds(instant.epochSecond)
            .setNanos(instant.nano)
            .build())
        .addAllErrors(errors)
        .build()
}

