
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

object UsuariosService extends IOApp {

  case class Usuario(id: Int, nombre: String, email: String)
  case class CrearUsuario(nombre: String, email: String)

  implicit val crearUsuarioDecoder = jsonOf[IO, CrearUsuario]
  implicit val usuarioEncoder = jsonEncoderOf[IO, Usuario]

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
  def obtenerUsuarios(xa: Transactor[IO]): IO[List[Usuario]] =
    sql"SELECT id, nombre, email FROM usuarios"
      .query[Usuario]
      .to[List]
      .transact(xa)

  def crearUsuario(usuario: CrearUsuario, xa: Transactor[IO]): IO[Usuario] =
    sql"INSERT INTO usuarios (nombre, email) VALUES (${usuario.nombre}, ${usuario.email}) RETURNING id, nombre, email"
      .query[Usuario]
      .unique
      .transact(xa)

  // Definición de las rutas
  def routes(xa: Transactor[IO]) = HttpRoutes.of[IO] {
    case GET -> Root / "usuarios" =>
      for {
        usuarios <- obtenerUsuarios(xa)
        resp <- Ok(usuarios)
      } yield resp

    case req @ POST -> Root / "usuarios" =>
      for {
        usuario <- req.as[CrearUsuario]
        creado <- crearUsuario(usuario, xa)
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

