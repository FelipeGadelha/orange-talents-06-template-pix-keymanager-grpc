package br.com.zup.felipe.gadelha.api.validation

enum class RegexValidator(private val regex: Regex) {
    CPF("^[0-9]{11}$".toRegex()),
    EMAIL("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+".toRegex()),
    CELL_PHONE("^+[1-9][0-9]\\d{1,14}\$".toRegex()),
    UUID("[a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8}".toRegex());

    fun validate(value: String):Boolean = value.matches(this.regex)
}

