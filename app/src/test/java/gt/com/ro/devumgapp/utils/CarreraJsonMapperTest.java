package gt.com.ro.devumgapp.utils;

import com.google.gson.JsonParser;

import org.junit.Test;

import java.util.List;

import gt.com.ro.devumgapp.network.model.Carrera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CarreraJsonMapperTest {

    @Test
    public void convierteUnaListaDirectaDeCarreras() {
        List<Carrera> carreras = CarreraJsonMapper.toList(JsonParser.parseString(
                "[{\"id\":1,\"codigo\":\"ISC\",\"nombre\":\"Ingenieria\",\"activo\":true}]"));

        assertEquals(1, carreras.size());
        assertEquals(1L, carreras.get(0).getId());
        assertEquals("ISC", carreras.get(0).getCodigo());
        assertEquals("Ingenieria", carreras.get(0).getNombre());
        assertTrue(carreras.get(0).getActivo());
    }

    @Test
    public void convierteUnaListaEnvueltaEnData() {
        List<Carrera> carreras = CarreraJsonMapper.toList(JsonParser.parseString(
                "{\"data\":[{\"id\":2,\"codigo\":\"ADM\",\"nombre\":\"Administracion\","
                        + "\"activo\":false}]}"));

        assertEquals(1, carreras.size());
        assertEquals(2L, carreras.get(0).getId());
        assertFalse(carreras.get(0).getActivo());
    }

    @Test
    public void convierteElDetalleEnvueltoEnData() {
        Carrera carrera = CarreraJsonMapper.toCarrera(JsonParser.parseString(
                "{\"data\":{\"id\":3,\"codigo\":\"DER\",\"nombre\":\"Derecho\","
                        + "\"activo\":true}}"));

        assertEquals(3L, carrera.getId());
        assertEquals("DER", carrera.getCodigo());
        assertEquals("Derecho", carrera.getNombre());
        assertTrue(carrera.getActivo());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rechazaUnaRespuestaDeListaInvalida() {
        CarreraJsonMapper.toList(JsonParser.parseString("{\"data\":{}}"));
    }
}
