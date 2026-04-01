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

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import java.nio.file.Paths
import java.io.ByteArrayOutputStream

// Exercise 5: Data class for JSON serialization
@Serializable
data class Stock(val symbol: String, val price: Double)

// Exercise 3: Simulated database
val grades = mapOf("123" to 95, "456" to 82)

// QR Code generation function (file-based)
fun saveQRCode(content: String, fileName: String, size: Int = 300) {
    val qrCodeWriter = QRCodeWriter()
    val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size)
    val path = Paths.get(fileName)
    MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path)
    println("QR code saved to: $fileName")
}

// QR Code generation function (stream-based for web servers)
fun generateQRCodeStream(content: String, size: Int = 300): ByteArrayOutputStream {
    val qrCodeWriter = QRCodeWriter()
    val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size)
    val outputStream = ByteArrayOutputStream()
    MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream)
    return outputStream
}

fun main() {
    // Generate QR code with Lehman email
    saveQRCode("your.email@lc.cuny.edu", "my_email.png")
    
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
            
            // QR Code generation endpoint
            get("/qr") {
                val text = call.request.queryParameters["text"]
                if (text != null) {
                    val qrStream = generateQRCodeStream(text)
                    call.response.header(HttpHeaders.ContentType, "image/png")
                    call.respondBytes(qrStream.toByteArray())
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Missing 'text' query parameter")
                }
            }
        }
    }.start(wait = true)
}
