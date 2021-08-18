package br.com.zup.felipe.gadelha.api.handler

import br.com.zup.felipe.gadelha.ExceptionDetails
import com.google.protobuf.Any
import com.google.protobuf.Timestamp
import com.google.rpc.BadRequest
import com.google.rpc.Code
import java.time.LocalDateTime
import java.time.ZoneId
import javax.validation.ConstraintViolationException

fun invalidArgumentHandler(e: ConstraintViolationException): com.google.rpc.Status =
com.google.rpc.Status.newBuilder()
        .setCode(Code.INVALID_ARGUMENT.number)
        .setMessage("Dados inválidos")
        .addDetails(Any.pack(BadRequest.newBuilder()
            .addAllFieldViolations(e.constraintViolations.map {
                BadRequest.FieldViolation.newBuilder()
                    .setField(it.propertyPath.last().name ?: "?? key ??") // still thinking how to solve this case
                    .setDescription(it.message)
                    .build()
            })
            .build()))
        .build()

fun alreadyExistsHandler(message: String?): com.google.rpc.Status {
    val instant = LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()
    return com.google.rpc.Status.newBuilder()
        .setCode(Code.ALREADY_EXISTS.number)
        .setMessage(message)
        .addDetails(Any.pack(ExceptionDetails.newBuilder()
            .setTitle(Code.ALREADY_EXISTS.name)
            .setCode(Code.ALREADY_EXISTS.number)
            .setMessage(message)
            .setTimestamp(Timestamp.newBuilder()
                .setSeconds(instant.epochSecond)
                .setNanos(instant.nano)
                .build())
            .build()))
        .build()
}
fun notFoundHandler(message: String?): com.google.rpc.Status {
    val instant = LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()
    return com.google.rpc.Status.newBuilder()
        .setCode(Code.NOT_FOUND.number)
        .setMessage(message)
        .addDetails(Any.pack(ExceptionDetails.newBuilder()
            .setTitle(Code.NOT_FOUND.name)
            .setCode(Code.NOT_FOUND.number)
            .setMessage(message)
            .setTimestamp(Timestamp.newBuilder()
                .setSeconds(instant.epochSecond)
                .setNanos(instant.nano)
                .build())
            .build()))
        .build()
}
fun permissionDeniedHandler(message: String?): com.google.rpc.Status {
    val instant = LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()
    return com.google.rpc.Status.newBuilder()
        .setCode(Code.PERMISSION_DENIED.number)
        .setMessage(message)
        .addDetails(Any.pack(ExceptionDetails.newBuilder()
            .setTitle(Code.PERMISSION_DENIED.name)
            .setCode(Code.PERMISSION_DENIED.number)
            .setMessage(message)
            .setTimestamp(Timestamp.newBuilder()
                .setSeconds(instant.epochSecond)
                .setNanos(instant.nano)
                .build())
            .build()))
        .build()
}
fun failedPreconditionHandler(message: String?): com.google.rpc.Status {
    val instant = LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()
//    "Falha no procedimento"
    return com.google.rpc.Status.newBuilder()
        .setCode(Code.FAILED_PRECONDITION.number)
        .setMessage(message)
        .addDetails(Any.pack(ExceptionDetails.newBuilder()
                .setTitle(Code.FAILED_PRECONDITION.name)
                .setCode(Code.FAILED_PRECONDITION.number)
                .setMessage(message)
                .setTimestamp(Timestamp.newBuilder()
                    .setSeconds(instant.epochSecond)
                    .setNanos(instant.nano)
                    .build())
                .build()))
        .build()
}

fun defaultHandler(message: String?):
        com.google.rpc.Status = com.google.rpc.Status.newBuilder()
        .setCode(Code.UNKNOWN.number)
        .setMessage(message)
        .build()

