import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

// Exercise 5: Data class for JSON serialization
@Serializable
data class Stock(val symbol: String, val price: Double)

// Exercise 3: Simulated database
val grades = mapOf("123" to 95, "456" to 82)

fun main() {
    embeddedServer(Netty, port = 8080) {
        // Exercise 5: Install ContentNegotiation plugin
        install(ContentNegotiation) {
            json()
        }
        
        routing {
            // Exercise 1: Root route
            get("/") {
                call.respondText("Server is online at Lehman College.")
            }
            
            // Exercise 2: Dynamic routing with path parameter
            get("/greet/{name}") {
                val name = call.parameters["name"]
                call.respondText("Hello, $name! Welcome to CMP 269.")
            }
            
            // Exercise 3: Grade search with null handling
            get("/grade/{studentId}") {
                val studentId = call.parameters["studentId"]
                val grade = grades[studentId]
                
                if (grade != null) {
                    call.respondText("Student $studentId has grade: $grade")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Student not found")
                }
            }
            
            // Exercise 4: Serve static content
            staticResources("/static", "static")
            
            // Exercise 5: JSON API endpoint
            get("/api/stock/{symbol}") {
                val symbol = call.parameters["symbol"] ?: "UNKNOWN"
                val stock = Stock(symbol, 150.25)
                call.respond(stock)
            }
        }
    }.start(wait = true)
}
