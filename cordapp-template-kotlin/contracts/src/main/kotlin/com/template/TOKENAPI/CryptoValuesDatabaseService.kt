package com.template.TOKENAPI

import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService

@CordaService
class CryptoValuesDatabaseService(services: ServiceHub) : DatabaseService(services) {
    init {
        setUpStorage()
    }

    /**
     * Adds a crypto token and associated value to the table of crypto values.
     */
    fun addTokenValue(token: String, value: Int, linearId: String) {
        val query = "insert into $TABLE_NAME values(?, ?, ?)"

        val params = mapOf(1 to token, 2 to value, 3 to linearId)

        executeUpdate(query, params)
        log.info("Token $token added to crypto_values table.")
    }

    /**
     * Updates the value of a crypto token in the table of crypto values.
     */
    fun updateTokenValue(token: String, value: Int , linearId: String) {
        val query = "update $TABLE_NAME set value = ? where linearId = ?"

        val params = mapOf(1 to value, 2 to token, 3 to linearId)

        executeUpdate(query, params)
        log.info("Token $token updated in crypto_values table.")
    }

    /**
     * Retrieves the value of a crypto token in the table of crypto values.
     */
    fun queryTokenValue(token: String): Int {
        val query = "select value from $TABLE_NAME where token = ?"

        val params = mapOf(1 to token)

        val results = executeQuery(query, params) { it -> it.getInt("value") }

        if (results.isEmpty()) {
            throw IllegalArgumentException("Token $token not present in database.")
        }

        val value = results.single()
        log.info("Token $token read from crypto_values table.")
        return value
    }

    /**
     * Initialises the table of crypto values.
     */
    private fun setUpStorage() {
        val query = """
            create table if not exists $TABLE_NAME(
                token varchar(64),
                value int,
                linearId varchar(64)
            )"""

        executeUpdate(query, emptyMap())
        log.info("Created crypto_values table.")
    }
}