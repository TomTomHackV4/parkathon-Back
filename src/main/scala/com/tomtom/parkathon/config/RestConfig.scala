package com.tomtom.parkathon.config


import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.web.cors.{CorsConfiguration, UrlBasedCorsConfigurationSource}
import org.springframework.web.filter.CorsFilter

@Configuration
class RestConfig {

  @Bean
  def corsFilter(): CorsFilter =  {
    val source = new UrlBasedCorsConfigurationSource()
    val config = new CorsConfiguration()
    config.setAllowCredentials(true)
    config.addAllowedOrigin("*")
    config.addAllowedHeader("*")
    config.addAllowedMethod("OPTIONS")
    config.addAllowedMethod("GET")
    config.addAllowedMethod("POST")
    config.addAllowedMethod("PUT")
    config.addAllowedMethod("DELETE")
    source.registerCorsConfiguration("/**", config)
    new CorsFilter(source)
  }
}