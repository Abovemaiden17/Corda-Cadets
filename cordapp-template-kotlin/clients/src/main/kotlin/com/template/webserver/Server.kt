package com.template.webserver

import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType.SERVLET
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import springfox.documentation.swagger2.annotations.EnableSwagger2
import com.fasterxml.jackson.databind.ObjectMapper
import net.corda.client.jackson.JacksonSupport
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Our Spring Boot application.
 */
@SpringBootApplication
@EnableSwagger2
@ComponentScan("com.template")
private open class Starter

/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    val app = SpringApplication(Starter::class.java)
    app.setBannerMode(Banner.Mode.OFF)
    app.webApplicationType = SERVLET
    app.run(*args)
}