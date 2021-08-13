package br.com.zup.felipe.gadelha.infra.dto.response

import javax.validation.constraints.NotBlank

data class ClientRs(

    @field:NotBlank val id: String,
//    val name: String,
//    val cpf: String,
)