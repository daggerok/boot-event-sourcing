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

import javax.persistence.*
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@Entity
class EventStore {
  @Id
  var id: UUID
    protected set
  var aggregateId: UUID
    protected set
  var aggregateType: String
    protected set
  var payLoad: String
    protected set
  var version: Int = 0

  @Basic
  protected var createdAt: Timestamp? = null

  constructor() {
    this.id = UUID.randomUUID()
    this.version = 1
  }

  constructor(id: UUID, aggregateId: UUID, aggregateType: String, payLoad: String, version: Int) {
    this.id = id
    this.aggregateId = aggregateId
    this.aggregateType = aggregateType
    this.payLoad = payLoad
    this.version = version
  }

  fun getCreatedAt(): String {
    return if (createdAt != null) {
      createdAt!!.toString()
    } else createdAt!!.toString()

  }

  @PrePersist
  fun prePersist() {
    this.createdAt = Timestamp.from(Instant.now())
  }
}

@SpringBootApplication
class App {

  @Bean
  fun routes() = router {
    ("/").nest {
      contentType(TEXT_HTML)
      GET("/") {
        //ok().render("index", mapOf("message" to "ololo trololo"))
        create("index")
            .modelAttribute("message", "ololo trololo")
            .build() as Mono<ServerResponse>
      }
      contentType(APPLICATION_JSON_UTF8)
      GET("/api/**") {
        ok().body(
            mapOf("hello" to "world").toMono()
        )
      }
    }
    resources("/**", ClassPathResource("/public"))
  }
}

fun main(args: Array<String>) {
  runApplication<App>(*args)
}