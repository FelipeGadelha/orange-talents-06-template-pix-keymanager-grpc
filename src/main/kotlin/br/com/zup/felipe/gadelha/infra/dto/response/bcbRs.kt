package br.com.zup.felipe.gadelha.infra.dto.response

data class BCBCreatePixKeyRs(
    val keyType: String,
    val key: String,
    val createdAt: String
)

data class BCBDeletePixKeyRs(
    val key: String,
    val participant: String,
    val deletedAt: String
)