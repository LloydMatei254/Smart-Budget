package com.example.smartbudget.domain.model

/**
 * Supported currencies in the Smart Budget application.
 * Each currency has a code, symbol, and display name.
 */
enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String
) {
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "British Pound"),
    KES("KES", "KSh", "Kenyan Shilling"),
    NGN("NGN", "₦", "Nigerian Naira"),
    ZAR("ZAR", "R", "South African Rand"),
    INR("INR", "₹", "Indian Rupee"),
    JPY("JPY", "¥", "Japanese Yen"),
    CNY("CNY", "¥", "Chinese Yuan"),
    CAD("CAD", "C$", "Canadian Dollar"),
    AUD("AUD", "A$", "Australian Dollar"),
    CHF("CHF", "CHF", "Swiss Franc"),
    BRL("BRL", "R$", "Brazilian Real"),
    MXN("MXN", "MX$", "Mexican Peso");

    companion object {
        fun fromCode(code: String): Currency {
            return values().find { it.code.equals(code, ignoreCase = true) } ?: USD
        }

        fun getAll(): List<Currency> = values().toList()
    }

    override fun toString(): String = code
}
