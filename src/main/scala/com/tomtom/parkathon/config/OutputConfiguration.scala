package com.tomtom.parkathon.config

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class OutputConfiguration {

  @Bean
  def jsonWithScalaSupport: Jackson2ObjectMapperBuilderCustomizer =
    builder => {
      builder.modules(DefaultScalaModule, new JavaTimeModule)
      builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}
