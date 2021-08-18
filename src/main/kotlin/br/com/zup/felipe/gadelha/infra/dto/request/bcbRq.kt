package br.com.zup.felipe.gadelha.infra.dto.request

import br.com.zup.felipe.gadelha.domain.entity.Pix
import br.com.zup.felipe.gadelha.infra.dto.response.AccountItauRs


data class BCBCreatePixKeyRq(
    val keyType: String,
    val key: String,
    val bankAccount: BCBBankAccount,
    val owner: BCBOwner
) {
    constructor(account: AccountItauRs, pix: Pix) : this(
        keyType = pix.typeKey.toString(),
        key = pix.value,
        bankAccount = BCBBankAccount(account),
        owner = BCBOwner(account)
    )
}

data class BCBBankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: String,
) {
    constructor(account: AccountItauRs): this(
        participant = account.institution.ispb,
        branch = account.agency,
        accountNumber = account.number,
    accountType = if (account.type == "CONTA_CORRENTE") "CACC" else "SVGS"
    )
}
data class BCBOwner(
    val type: String,
    val name: String,
    val taxIdNumber: String,
) {
    constructor(account: AccountItauRs): this(
        type = "NATURAL_PERSON",
        name = account.holder.name,
        taxIdNumber = account.holder.cpf
    )
}