package com.latin.dream.erp;

import com.latin.dream.erp.application.service.*;
import com.latin.dream.erp.config.SecurityConfig;
import com.latin.dream.erp.domain.repository.UsuarioRepository;
import com.latin.dream.erp.infrastructure.security.*;
import com.latin.dream.erp.presentation.controller.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(controllers = {OrdenController.class, ComisionesController.class, ProduccionController.class, DespachoController.class, AdministracionController.class}, properties = {"api.prefix=/api/prueba"})
@Import({SecurityConfig.class, JwtAuthFilter.class})
class SeguridadApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean JwtService jwtService;
    @MockitoBean UsuarioRepository usuarios;
    @MockitoBean OrdenService ordenes;
    @MockitoBean HistorialService historial;
    @MockitoBean CatalogoGestionService catalogos;
    @MockitoBean ComisionesService comisiones;
    @MockitoBean ReporteExcelService reportes;
    @MockitoBean ProduccionService produccion;
    @MockitoBean DespachoService despachos;

    @Test void anonimoNoLeeNiModificaDatos() throws Exception {
        mvc.perform(get("/api/prueba/ordenes")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/prueba/ordenes/borrador").contentType("application/json").content("{}"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/prueba/comisiones").header("Authorization", "Bearer invalido"))
            .andExpect(status().isUnauthorized());
    }
    @Test void vendedorNoConsultaComisionesNiCambiaDespachos() throws Exception {
        mvc.perform(get("/api/prueba/comisiones").with(user("venta").authorities(() -> "vendedora")))
            .andExpect(status().isForbidden());
        mvc.perform(patch("/api/prueba/despachos/DS-1/estado").with(user("venta").authorities(() -> "vendedora"))
            .contentType("application/json").content("{\"estado\":\"entregado\"}"))
            .andExpect(status().isForbidden());
    }
    @Test void gerenteConsultaComisionesConPrefijoConfigurable() throws Exception {
        mvc.perform(get("/api/prueba/comisiones").with(user("gerente").authorities(() -> "gerente")))
            .andExpect(status().isOk());
    }
    @Test void jwtValidoInyectaElUsuarioRealParaAuditoria() throws Exception {
        var usuario = com.latin.dream.erp.domain.entity.Usuario.builder()
            .correo("venta@ejemplo.test").nombre("Venta").rol("vendedora").activo(true).build();
        when(jwtService.extraerUsername("valido")).thenReturn(usuario.getCorreo());
        when(usuarios.findByCorreo(usuario.getCorreo())).thenReturn(java.util.Optional.of(usuario));
        when(jwtService.esValido("valido", usuario)).thenReturn(true);
        mvc.perform(post("/api/prueba/ordenes/borrador").header("Authorization", "Bearer valido")
            .contentType("application/json").content("{\"modeloId\":\"modelo\",\"cantidad\":2,\"prioridad\":\"Alta\",\"fechaEntrega\":\"2099-01-01\"}"))
            .andExpect(status().isOk());
        verify(ordenes).guardarBorrador(any(), same(usuario));
        usuario.setActivo(false);
        mvc.perform(get("/api/prueba/ordenes").header("Authorization", "Bearer valido"))
            .andExpect(status().isUnauthorized());
    }

    @Test void avanceIncompletoSeRechazaComoPeticionInvalida() throws Exception {
        mvc.perform(post("/api/prueba/produccion/avances").with(user("planta").authorities(() -> "encargado_area"))
            .contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest());
    }
    @Test void rolesDeLecturaNoPuedenModificarCatalogosNiBorradores() throws Exception {
        mvc.perform(put("/api/prueba/ordenes/id/borrador").with(user("chofer").authorities(() -> "conductor"))
            .contentType("application/json").content("{}")) .andExpect(status().isForbidden());
        mvc.perform(delete("/api/prueba/ordenes/id/borrador").with(user("chofer").authorities(() -> "conductor")))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/prueba/administracion").with(user("conta").authorities(() -> "contabilidad")))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/prueba/despachos").with(user("venta").authorities(() -> "vendedora"))
            .contentType("application/json").content("{}")) .andExpect(status().isForbidden());
        mvc.perform(post("/api/prueba/ordenes").with(user("chofer").authorities(() -> "conductor"))
            .contentType("application/json").content("{}")) .andExpect(status().isForbidden());
    }
    @Test void gerenteAdministraYContabilidadRegistraLiquidacionesYPagos() throws Exception {
        mvc.perform(get("/api/prueba/administracion").with(user("gerente").authorities(() -> "gerente")))
            .andExpect(status().isOk());
        mvc.perform(post("/api/prueba/comisiones/ordenes/id/liquidar").with(user("conta").authorities(() -> "contabilidad")))
            .andExpect(status().isOk());
        mvc.perform(post("/api/prueba/comisiones/liquidaciones/id/pago").with(user("conta").authorities(() -> "contabilidad"))
            .contentType("application/json").content("{\"referencia\":\"OPERACION-1\"}")) .andExpect(status().isOk());
    }
}
