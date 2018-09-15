package daggerok

import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.*
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.toMono
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.io.ClassPathResource
import org.springframework.web.reactive.function.server.RenderingResponse.create
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@SpringBootApplication
class App {

  @Bean
  fun routes() = router {
    resources("/**", ClassPathResource("/static"))
    ("/").nest {
      contentType(TEXT_HTML)
      GET("/") {
        create("index")
            .modelAttribute("message", "ololo trololo")
            .build() as Mono<ServerResponse>
      }
      contentType(APPLICATION_JSON_UTF8)
      GET("/**") {
        ok().body(
            mapOf("hello" to "world").toMono()
        )
      }
    }
  }
}

fun main(args: Array<String>) {
  runApplication<App>(*args)
}
