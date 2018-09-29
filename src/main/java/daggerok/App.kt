package daggerok

import org.hibernate.annotations.GenericGenerator
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.*
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.toMono
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.io.ClassPathResource
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.server.RenderingResponse.create
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.toFlux
import reactor.core.scheduler.Schedulers.elastic

import javax.persistence.*
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@Entity
data class EventStore(
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    var id: String? = null,
    var aggregateId: UUID? = null,
    @Version var version: Int = 0,
    val events: ArrayList<String> = arrayListOf("created"),
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null
) {

  fun update(): EventStore {
    events.add("updated")
    return this
  }

  @PrePersist
  fun prePersist() {
    this.createdAt = Timestamp.from(Instant.now())
    this.createdAt = this.createdAt?.clone() as Timestamp?
  }

  @PreUpdate
  fun preUpdate() {
    this.updatedAt = Timestamp.from(Instant.now())
  }
}

@Repository interface EventStoreRepository : JpaRepository<EventStore, String> {
  fun findFirstByIdOrAggregateId(id: String, aggregateId: UUID): List<EventStore>
}

fun String.toUUID(): UUID = try { UUID.fromString(this) }
catch (e: IllegalArgumentException) { UUID.fromString("00000000-0000-0000-0000-000000000000") }

@SpringBootApplication
class App(val eventStoreRepository: EventStoreRepository) {

  @Bean
  fun routes() = router {
    ("/").nest {
      contentType(TEXT_HTML)
      GET("/") {
        //ok().render("index", mapOf("message" to "ololo trololo"))
        create("index")
            .modelAttribute("message", "ololo trololo")
            .build()
            .cast(ServerResponse::class.java)
      }
      contentType(APPLICATION_JSON_UTF8)
      POST("/api/{uuid}") {
        val id = it.pathVariable("uuid")
        val uuid = id.toUUID()
        ok().body(
            eventStoreRepository
                .findFirstByIdOrAggregateId(id, uuid)
                .toFlux()
                .map { eventStoreRepository.save(it.update()) }
                .subscribeOn(elastic())
        )
      }
      POST("/api/**") {
        ok().body(mapOf("aggregate" to eventStoreRepository
            .save(EventStore(aggregateId = UUID.randomUUID()))).toMono())
      }
      GET("/api/**") {
        ok().body(
            eventStoreRepository
                .findAll()
                .toFlux()
        )
      }
    }
    resources("/**", ClassPathResource("/public"))
  }
}

fun main(args: Array<String>) {
  runApplication<App>(*args)
}
