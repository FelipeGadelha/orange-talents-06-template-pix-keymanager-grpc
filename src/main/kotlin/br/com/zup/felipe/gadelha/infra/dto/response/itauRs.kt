package br.com.zup.felipe.gadelha.infra.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class AccountItauRs(
    @JsonProperty("tipo") val type: String,
    @JsonProperty("agencia") val agency: String,
    @JsonProperty("numero") val number: String,
    @JsonProperty("instituicao") val institution: InstitutionItau,
    @JsonProperty("titular") val holder: HolderItau,
)

data class InstitutionItau(
    @JsonProperty("nome") val name: String,
    val ispb: String
)
data class HolderItau(
    val id: String,
    @JsonProperty("nome") val name: String,
    val cpf: String
)
