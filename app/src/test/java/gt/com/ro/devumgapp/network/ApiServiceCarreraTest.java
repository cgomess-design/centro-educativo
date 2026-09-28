package gt.com.ro.devumgapp.network;

import org.junit.Test;

import java.lang.reflect.Method;

import gt.com.ro.devumgapp.network.model.CarreraRequest;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

import static org.junit.Assert.assertEquals;

public class ApiServiceCarreraTest {

    @Test
    public void exponeTodosLosEndpointsCrudDeCarrera() throws Exception {
        Method listar = ApiService.class.getMethod("obtenerCarreras");
        Method obtener = ApiService.class.getMethod("obtenerCarrera", long.class);
        Method crear = ApiService.class.getMethod("crearCarrera", CarreraRequest.class);
        Method actualizar = ApiService.class.getMethod(
                "actualizarCarrera", long.class, CarreraRequest.class);
        Method eliminar = ApiService.class.getMethod("eliminarCarrera", long.class);

        assertEquals("carreras", listar.getAnnotation(GET.class).value());
        assertEquals("carreras/{id}", obtener.getAnnotation(GET.class).value());
        assertEquals("carreras", crear.getAnnotation(POST.class).value());
        assertEquals("carreras/{id}", actualizar.getAnnotation(PUT.class).value());
        assertEquals("carreras/{id}", eliminar.getAnnotation(DELETE.class).value());
    }
}
