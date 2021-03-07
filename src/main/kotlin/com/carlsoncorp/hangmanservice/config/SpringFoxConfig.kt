package com.carlsoncorp.hangmanservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors

import springfox.documentation.builders.RequestHandlerSelectors

import springfox.documentation.spi.DocumentationType

import springfox.documentation.spring.web.plugins.Docket

import springfox.documentation.swagger2.annotations.EnableSwagger2
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact


@Configuration
@EnableSwagger2
class SpringFoxConfig {
    @Bean
    fun apiDocket(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.carlsoncorp"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(getApiInfo())
    }

    private fun getApiInfo(): ApiInfo? {
        return ApiInfo(
            "Hangman Service",
            "Backend representation of Hangman",
            "v1beta",
            "TERMS OF SERVICE URL",
            Contact("Carlson Tse", "https://github.com/carlsontse", "EMAIL"),
            "LICENSE",
            "LICENSE URL",
            emptyList()
        )
    }
}