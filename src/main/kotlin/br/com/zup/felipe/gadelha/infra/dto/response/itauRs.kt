package br.com.zup.felipe.gadelha.infra.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class AccountItauRs(
    @JsonProperty("tipo") val type: String,
    @JsonProperty("agencia") val agency: String,
    @JsonProperty("numero") val number: String,
    @JsonProperty("instituicao") val institution: InstitutionItauRs,
    @JsonProperty("titular") val holder: HolderItauRs,
)

data class InstitutionItauRs(
    @JsonProperty("nome") val name: String,
    val ispb: String
)
data class HolderItauRs(
    val id: String,
    @JsonProperty("nome") val name: String,
    val cpf: String
)

data class ClientItauRs(
    val id: String,
    val cpf: String,
    @JsonProperty("nome") val name: String,
    @JsonProperty("instituicao") val institution: InstitutionItauRs,
)