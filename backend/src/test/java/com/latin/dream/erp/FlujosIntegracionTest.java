package com.latin.dream.erp;

import com.latin.dream.erp.application.dto.*;
import com.latin.dream.erp.application.service.*;
import com.latin.dream.erp.domain.entity.*;
import com.latin.dream.erp.domain.repository.*;
import com.latin.dream.erp.crosscutting.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={
    "spring.datasource.url=jdbc:h2:mem:flujos;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.show-sql=false", "spring.sql.init.mode=never", "app.seed.enabled=false",
    "jwt.secret=clave-de-prueba-aislada-de-mas-de-32-bytes", "api.prefix=/api/v1"
})
@Transactional
class FlujosIntegracionTest {
    @Autowired OrdenService ordenes;
    @Autowired ProduccionService produccion;
    @Autowired DespachoService despachos;
    @Autowired ComisionesService comisiones;
    @Autowired CatalogoGestionService catalogos;
    @Autowired HistorialService historial;
    @Autowired OrdenRepository ordenRepo;
    @Autowired ModeloRepository modeloRepo;
    @Autowired UsuarioRepository usuarios;
    @Autowired LoteRepository lotes;
    @Autowired PrecioComisionRepository tarifas;
    @Autowired LiquidacionComisionRepository liquidaciones;
    @Autowired DespachoLoteRepository asignaciones;
    @Autowired DespachoRepository despachoRepo;
    @Autowired LoteAvanceRepository avances;
    @Autowired HistorialOrdenRepository eventos;
    // Solo se sustituye el bloqueo asesor específico de PostgreSQL. Repositorios y transacciones son reales.
    @MockitoBean JdbcTemplate jdbc;
    Usuario usuario; Modelo modelo;

    @BeforeEach void datos() {
        usuario=usuarios.saveAndFlush(Usuario.builder().nombre("Responsable").correo(UUID.randomUUID()+"@test.local")
            .password("hash-de-prueba").rol("supervisora").turno("Turno A").build());
        modelo=modeloRepo.saveAndFlush(Modelo.builder().codigo("M-"+UUID.randomUUID().toString().substring(0,8))
            .nombre("Modelo prueba").categoria("Queen").dimensiones("160x200").relleno("Espuma").build());
        tarifas.saveAndFlush(PrecioComision.builder().modelo(modelo).categoria(modelo.getCategoria()).precioBase(new BigDecimal("123.45"))
            .porcentaje(new BigDecimal("4.25")).vigenteDesde(LocalDate.now().minusMonths(1)).build());
    }
    @AfterEach void limpiarPruebasSinTransaccionAmbiental() {
        if (!org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) {
            asignaciones.deleteAll(); liquidaciones.deleteAll(); eventos.deleteAll(); avances.deleteAll();
            despachoRepo.deleteAll(); lotes.deleteAll(); ordenRepo.deleteAll(); tarifas.deleteAll(); modeloRepo.deleteAll(); usuarios.deleteAll();
        }
    }
    OrdenRequest request(int cantidad) {
        OrdenRequest r=new OrdenRequest(); r.setModeloId(modelo.getId()); r.setCantidad(cantidad); r.setPrioridad("Alta");
        r.setFechaEntrega(LocalDate.now().plusDays(5)); r.setObservaciones("Usar tela azul"); r.setSolicitudId(UUID.randomUUID().toString()); return r;
    }
    Lote aprobar(OrdenResponse o) {
        Lote l=lotes.findByOrdenNumero(o.getNumero()).get(0);
        for(String destino:List.of("Ensamblaje","Tapizado","Calidad","Listo"))
            produccion.registrarAvance(new AvanceRequest(l.getId(),destino,"Listo".equals(destino)?"aprobado":"","Avance"),usuario);
        return l;
    }
    DespachoRequest despacho(Lote lote,int cantidad) {
        return new DespachoRequest(UUID.randomUUID().toString(),"Cliente","Lima","ABC-123","Conductor",LocalDate.now(),LocalDate.now(),
            List.of(new DespachoRequest.Asignacion(lote.getId(),cantidad)));
    }
    @Test void recorridoCompletoConDosEntregasLiquidacionYPago() {
        OrdenRequest r=request(10);
        OrdenResponse o=ordenes.crear(r,usuario);
        assertEquals(o.getId(),ordenes.crear(r,usuario).getId());
        assertEquals(1,ordenRepo.count());
        Lote lote=aprobar(o);
        assertEquals("Usar tela azul",produccion.getLotes().get(0).get("observaciones"));
        assertEquals(4,produccion.historialLote(lote.getId()).size());
        DespachoRequest parcial=despacho(lote,4);
        var d1=despachos.crear(parcial,usuario);
        assertEquals(d1.get("codigo"),despachos.crear(parcial,usuario).get("codigo"));
        var d2=despachos.crear(despacho(lote,6),usuario);
        assertTrue(despachos.disponibles().isEmpty());
        String c1=(String)d1.get("codigo"), c2=(String)d2.get("codigo");
        despachos.actualizarEstado(c1,"transito",usuario); despachos.actualizarEstado(c1,"entregado",usuario);
        assertEquals("listo_despacho",ordenRepo.findById(o.getId()).orElseThrow().getEstado());
        despachos.actualizarEstado(c2,"transito",usuario); despachos.actualizarEstado(c2,"entregado",usuario);
        assertEquals("completado",ordenRepo.findById(o.getId()).orElseThrow().getEstado());
        assertEquals(0,produccion.getProduccionData().get("totalLotes"));
        var fila=comisiones.liquidar(o.getId(),usuario);
        assertEquals("Pendiente",fila.get("estado"));
        assertEquals(new BigDecimal("52.47"),fila.get("monto"));
        String id=(String)fila.get("liquidacionId");
        assertEquals(id,comisiones.liquidar(o.getId(),usuario).get("liquidacionId"));
        assertEquals(1,liquidaciones.count());
        PrecioComision t=tarifas.findAll().stream().filter(tar -> tar.getModelo().getId().equals(modelo.getId())).findFirst().orElseThrow(); t.setPrecioBase(new BigDecimal("999.00")); tarifas.saveAndFlush(t);
        assertEquals(new BigDecimal("52.47"),comisiones.calcularFilas().get(0).get("monto"));
        comisiones.pagar(id,"OPERACION-123",usuario); comisiones.pagar(id,"OPERACION-123",usuario);
        assertEquals("Pagado",comisiones.calcularFilas().get(0).get("estado"));
        assertTrue(historial.consultar(o.getId()).stream().anyMatch(h -> "completado".equals(h.get("estado"))));
    }
    @Test void borradorParcialSePuedeRetomarCorregirYEliminar() {
        var vacio=new OrdenRequest(); vacio.setObservaciones("Pendiente de definir producto");
        var b=ordenes.guardarBorrador(vacio,usuario);
        assertNull(b.getCantidad()); assertNull(b.getFechaEntrega());
        assertTrue(ordenes.listar(null,"Alta").isEmpty());
        OrdenRequest datos=request(2); datos.setFechaEntrega(LocalDate.now().minusDays(1));
        ordenes.editarBorrador(b.getId(),datos,usuario);
        datos.setFechaEntrega(LocalDate.now().plusDays(2));
        var corregido=ordenes.editarBorrador(b.getId(),datos,usuario);
        assertEquals(datos.getFechaEntrega(),corregido.getFechaEntrega());
        ordenes.eliminarBorrador(b.getId()); assertFalse(ordenRepo.existsById(b.getId()));
    }
    @Test @Transactional(propagation=org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void reintentoFallidoNoDejaBorradorPersistidoEnCreacionDirecta() {
        var r=request(2); r.setFechaEntrega(LocalDate.now().minusDays(1));
        long antes=ordenRepo.count();
        assertThrows(BusinessException.class,()->ordenes.crear(r,usuario));
        assertEquals(antes,ordenRepo.count());
    }
    @Test void reservaImpideSobreasignacion() {
        var lote=aprobar(ordenes.crear(request(5),usuario));
        despachos.crear(despacho(lote,4),usuario);
        assertThrows(BusinessException.class,()->despachos.crear(despacho(lote,2),usuario));
    }
    @Test void cancelarLiberaReservaYLoteCompletoSePuedeReasignar() {
        var lote=aprobar(ordenes.crear(request(5),usuario));
        var d=despachos.crear(despacho(lote,5),usuario);
        despachos.actualizarEstado((String)d.get("codigo"),"cancelado",usuario);
        assertEquals(5L,((Number)despachos.disponibles().get(0).get("disponibles")).longValue());
        var nuevo=despachos.crear(despacho(lote,5),usuario);
        assertEquals(5,nuevo.get("unidades"));
    }
    @Test void noSePuedeReabrirUnaEntrega() {
        var lote=aprobar(ordenes.crear(request(1),usuario));
        var d=despachos.crear(despacho(lote,1),usuario); String codigo=(String)d.get("codigo");
        despachos.actualizarEstado(codigo,"transito",usuario); despachos.actualizarEstado(codigo,"entregado",usuario);
        assertThrows(BusinessException.class,()->despachos.actualizarEstado(codigo,"programado",usuario));
    }
    @Test void liquidacionSeBloqueaAntesDeEntregaTotal() {
        var o=ordenes.crear(request(5),usuario); aprobar(o);
        assertThrows(BusinessException.class,()->comisiones.liquidar(o.getId(),usuario));
    }
    @Test void nuevaTarifaCierraAnteriorYModeloUsadoSoloPermiteDisponibilidad() {
        var o=ordenes.crear(request(2),usuario);
        var fecha=LocalDate.now().plusDays(1);
        catalogos.tarifa(null,new TarifaRequest(modelo.getId(),new BigDecimal("200.00"),new BigDecimal("5.00"),fecha,null));
        assertTrue(tarifas.findAll().stream().anyMatch(t -> fecha.minusDays(1).equals(t.getVigenteHasta())));
        catalogos.modelo(modelo.getId(),new ModeloRequest(modelo.getCodigo(),modelo.getNombre(),modelo.getCategoria(),modelo.getDimensiones(),modelo.getRelleno(),false));
        assertEquals(modelo.getId(),ordenes.buscarPorNumero(o.getNumero()).getModeloId());
        assertThrows(BusinessException.class,()->catalogos.modelo(modelo.getId(),new ModeloRequest(modelo.getCodigo(),"Otro",modelo.getCategoria(),modelo.getDimensiones(),modelo.getRelleno(),true)));
    }
    @Test @Transactional(propagation=org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void dosReservasSimultaneasNoPuedenConsumirElMismoSaldo() throws Exception {
        var lote=aprobar(ordenes.crear(request(5),usuario));
        var inicio=new java.util.concurrent.CountDownLatch(1);
        var pool=java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.Callable<Boolean> reservar=() -> {
                inicio.await();
                try { despachos.crear(despacho(lote,4),usuario); return true; }
                catch (BusinessException e) { return false; }
            };
            var primera=pool.submit(reservar); var segunda=pool.submit(reservar); inicio.countDown();
            boolean a=primera.get(15,java.util.concurrent.TimeUnit.SECONDS),c=segunda.get(15,java.util.concurrent.TimeUnit.SECONDS);
            assertNotEquals(a,c,"Solo una reserva debe confirmar");
            assertEquals(1L,((Number)despachos.disponibles().get(0).get("disponibles")).longValue());
        } finally { pool.shutdownNow(); }
    }
    @Test void unBorradorConservaSuCreadorAunqueLoConfirmeOtraPersona() {
        var otra=usuarios.saveAndFlush(Usuario.builder().nombre("Otra supervisora").correo(UUID.randomUUID()+"@test.local").password("hash").rol("supervisora").build());
        var borrador=ordenes.guardarBorrador(request(2),usuario);
        var orden=ordenes.crearOrden(borrador.getId(),otra);
        assertEquals(usuario.getNombre(),orden.getRegistradoPor());
        assertEquals(otra.getNombre(),historial.consultar(orden.getId()).get(0).get("usuario"));
    }
}
