
import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.blaze.server._
import org.http4s.circe._
import io.circe.generic.auto._
import doobie._
import doobie.hikari._
import doobie.implicits._
import scala.concurrent.ExecutionContext

object ProductosService extends IOApp {

  case class Producto(id: Int, nombre: String, precio: BigDecimal)
  case class CrearProducto(nombre: String, precio: BigDecimal)

  implicit val crearProductoDecoder = jsonOf[IO, CrearProducto]
  implicit val productoEncoder = jsonEncoderOf[IO, Producto]

  // Configuración de la conexión a la base de datos
  def transactor: Resource[IO, HikariTransactor[IO]] = {
    HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://postgres:5432/mibasedatos",
      "miusuario",
      "mipassword",
      ExecutionContext.global
    )
  }

  // Queries usando Doobie
  def obtenerProductos(xa: Transactor[IO]): IO[List[Producto]] =
    sql"SELECT id, nombre, precio FROM productos"
      .query[Producto]
      .to[List]
      .transact(xa)

  def crearProducto(producto: CrearProducto, xa: Transactor[IO]): IO[Producto] =
    sql"INSERT INTO productos (nombre, precio) VALUES (${producto.nombre}, ${producto.precio}) RETURNING id, nombre, precio"
      .query[Producto]
      .unique
      .transact(xa)

  // Definición de las rutas
  def routes(xa: Transactor[IO]) = HttpRoutes.of[IO] {
    case GET -> Root / "productos" =>
      for {
        productos <- obtenerProductos(xa)
        resp <- Ok(productos)
      } yield resp

    case req @ POST -> Root / "productos" =>
      for {
        producto <- req.as[CrearProducto]
        creado <- crearProducto(producto, xa)
        resp <- Created(creado)
      } yield resp
  }

  // Configuración del servidor
  def run(args: List[String]): IO[ExitCode] = {
    transactor.use { xa =>
      BlazeServerBuilder[IO](ExecutionContext.global)
        .bindHttp(3000, "0.0.0.0")
        .withHttpApp(routes(xa).orNotFound)
        .resource
        .use(_ => IO.never)
        .as(ExitCode.Success)
    }
  }
}

