package br.com.zup.felipe.gadelha.infra.dto.response

data class BCBCreatePixKeyRs(
    val keyType: String,
    val key: String,
    val createdAt: String
)

//<CreatePixKeyResponse>
//<keyType>CPF</keyType>
//<key>string</key>
//<bankAccount>
//  <participant>string</participant>
//  <branch>string</branch>
//  <accountNumber>string</accountNumber>
//  <accountType>CACC</accountType>
//</bankAccount>
//<owner>
//  <type>NATURAL_PERSON</type>
//  <name>string</name>
//  <taxIdNumber>string</taxIdNumber>
//</owner>
//<createdAt>2021-08-18T17:27:10.382Z</createdAt>
//</CreatePixKeyResponse>