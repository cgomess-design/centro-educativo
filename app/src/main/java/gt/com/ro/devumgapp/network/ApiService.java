package gt.com.ro.devumgapp.network;

import com.google.gson.JsonElement;

import gt.com.ro.devumgapp.network.model.CursoRequest;
import gt.com.ro.devumgapp.network.model.DocenteRequest;
import gt.com.ro.devumgapp.network.model.EstudianteRequest;
import gt.com.ro.devumgapp.network.model.NotaRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/** Endpoints protegidos publicados por el backend SGAU. */
public interface ApiService extends AuthService {

    @GET("estudiantes")
    Call<JsonElement> obtenerEstudiantes();

    @GET("estudiantes/{id}")
    Call<JsonElement> obtenerEstudiante(@Path("id") long id);

    @POST("estudiantes")
    Call<JsonElement> crearEstudiante(@Body EstudianteRequest request);

    @PUT("estudiantes/{id}")
    Call<JsonElement> actualizarEstudiante(@Path("id") long id, @Body EstudianteRequest request);

    @DELETE("estudiantes/{id}")
    Call<JsonElement> eliminarEstudiante(@Path("id") long id);


    /** CURSOS */

    @GET("cursos") Call<JsonElement> obtenerCursos();

    @GET("cursos/{id}") Call<JsonElement> obtenerCurso(@Path("id") long id);

    @GET("cursos/carrera/{carreraId}") Call<JsonElement> obtenerCursosPorCarrera(@Path("carreraId") long carreraId);

    @POST("cursos") Call<JsonElement> crearCurso(@Body CursoRequest request);

    @PUT("cursos/{id}") Call<JsonElement> actualizarCurso(@Path("id") long id, @Body CursoRequest request);

    @DELETE("cursos/{id}") Call<JsonElement> eliminarCurso(@Path("id") long id);

    @PUT("cursos/{id}/restaurar") Call<JsonElement> restaurarCurso(@Path("id") long id);

    /** DOCENTES */

    @GET("docentes")
    Call<JsonElement> obtenerDocentes();

    @GET("docentes/{id}")
    Call<JsonElement> obtenerDocente(@Path("id") long id);

    @POST("docentes")
    Call<JsonElement> crearDocente(@Body DocenteRequest request);

    @PUT("docentes/{id}")
    Call<JsonElement> actualizarDocente(@Path("id") long id, @Body DocenteRequest request);

    @DELETE("docentes/{id}")
    Call<JsonElement> eliminarDocente(@Path("id") long id);

    /** NOTAS */

    @GET("notas")
    Call<JsonElement> obtenerNotas();

    @GET("notas/{id}")
    Call<JsonElement> obtenerNota(@Path("id") long id);

    @POST("notas")
    Call<JsonElement> crearNota(@Body NotaRequest request);

    @PUT("notas/{id}")
    Call<JsonElement> actualizarNota(@Path("id") long id, @Body NotaRequest request);

    @DELETE("notas/{id}")
    Call<JsonElement> eliminarNota(@Path("id") long id);
}



