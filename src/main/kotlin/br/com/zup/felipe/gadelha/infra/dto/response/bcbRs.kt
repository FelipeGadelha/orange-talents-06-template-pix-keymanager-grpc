package br.com.zup.felipe.gadelha.infra.dto.response

import java.time.LocalDateTime

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

data class BCBDetailsPixKeyRs(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccountRs,
    val owner: OnwerRs,
    val createdAt: LocalDateTime
)
data class BankAccountRs(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)

data class OnwerRs(
    val type: String,
    val name: String,
    val taxIdNumber: String
)

enum class AccountType(val type: String){
    CACC("CURRENT"),
    SVGS("SAVING")
}
