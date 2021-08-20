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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BCBCreatePixKeyRq

        if (owner != other.owner) return false

        return true
    }
    override fun hashCode(): Int {
        return key.hashCode()
    }
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BCBBankAccount

        if (participant != other.participant) return false
        if (branch != other.branch) return false
        if (accountNumber != other.accountNumber) return false
        if (accountType != other.accountType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = participant.hashCode()
        result = 31 * result + branch.hashCode()
        result = 31 * result + accountNumber.hashCode()
        result = 31 * result + accountType.hashCode()
        return result
    }


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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BCBOwner

        if (type != other.type) return false
        if (name != other.name) return false
        if (taxIdNumber != other.taxIdNumber) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + taxIdNumber.hashCode()
        return result
    }

}