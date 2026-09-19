package gt.com.ro.devumgapp.network;

import com.google.gson.JsonElement;

import gt.com.ro.devumgapp.network.model.CursoRequest;
import gt.com.ro.devumgapp.network.model.EstudianteRequest;
import gt.com.ro.devumgapp.network.model.InscripcionRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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


    /** INSCRIPCIONES */

    @GET("inscripciones") Call<JsonElement> obtenerInscripciones();

    @GET("inscripciones/{id}") Call<JsonElement> obtenerInscripcion(@Path("id") long id);

    @GET("inscripciones/estudiante/{estudianteId}") Call<JsonElement> obtenerInscripcionesPorEstudiante(@Path("estudianteId") long estudianteId);

    @GET("inscripciones/curso/{cursoId}") Call<JsonElement> obtenerInscripcionesPorCurso(@Path("cursoId") long cursoId);

    @GET("inscripciones/paginadas") Call<JsonElement> obtenerInscripcionesPaginadas(@Query("pagina") int pagina, @Query("tamanio") int tamanio);

    @POST("inscripciones") Call<JsonElement> crearInscripcion(@Body InscripcionRequest request);

    @PUT("inscripciones/{id}") Call<JsonElement> actualizarInscripcion(@Path("id") long id, @Body InscripcionRequest request);

    @DELETE("inscripciones/{id}") Call<JsonElement> eliminarInscripcion(@Path("id") long id);
}



