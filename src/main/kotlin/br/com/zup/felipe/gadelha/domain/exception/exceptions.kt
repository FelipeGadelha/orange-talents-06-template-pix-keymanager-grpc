package br.com.zup.felipe.gadelha.domain.exception

class AlreadyExistsException(message: String): RuntimeException(message)

class OperationNotAllowedException(message: String): RuntimeException(message)

class EntityNotFountException(message: String): RuntimeException(message)