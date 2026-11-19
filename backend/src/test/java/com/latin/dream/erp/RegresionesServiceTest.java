package com.latin.dream.erp;

import com.latin.dream.erp.application.dto.AvanceRequest;
import com.latin.dream.erp.application.service.*;
import com.latin.dream.erp.domain.entity.*;
import com.latin.dream.erp.domain.repository.*;
import com.latin.dream.erp.crosscutting.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class RegresionesServiceTest {
    @Test void comisionUsaCantidadRealTarifaVigenteYCentavosEnPantallaYExcel() throws Exception {
        var precios = mock(PrecioComisionRepository.class);
        var ordenes = mock(OrdenRepository.class);
        var modelo = Modelo.builder().id("modelo").nombre("Modelo").categoria("Queen").relleno("Memory Foam").build();
        var orden = Orden.builder().numero("OP-2026-0001").modelo(modelo).cantidad(3).estado("pendiente_corte").creadoEn(LocalDateTime.now()).build();
        var tarifa = PrecioComision.builder().id("precio").modelo(modelo).categoria("Queen").precioBase(new BigDecimal("123.45"))
            .porcentaje(new BigDecimal("4.25")).vigenteDesde(LocalDate.now().minusDays(1)).build();
        var futura = PrecioComision.builder().id("futura").modelo(modelo).categoria("Queen").precioBase(new BigDecimal("999.00"))
            .porcentaje(new BigDecimal("50.00")).vigenteDesde(LocalDate.now().plusDays(1)).build();
        when(precios.findAll()).thenReturn(List.of(tarifa, futura));
        when(ordenes.findAll()).thenReturn(List.of(orden));
        var servicio = new ComisionesService(precios, ordenes, mock(LiquidacionComisionRepository.class));
        var filas = servicio.calcularFilas();
        assertEquals(3, filas.get(0).get("unidades"));
        assertEquals(new BigDecimal("15.74"), filas.get(0).get("monto"));
        assertEquals("Estimado", filas.get(0).get("estado"));
        byte[] excel = new ReporteExcelService(servicio).exportarReporteComisiones();
        try (var libro = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            assertEquals(15.74, libro.getSheetAt(0).getRow(4).getCell(8).getNumericCellValue(), 0.00001);
        }
        orden.setEstado("borrador");
        assertTrue(servicio.calcularFilas().isEmpty());
        orden.setEstado("pendiente_corte");
        orden.setCreadoEn(LocalDate.now().withDayOfMonth(1).minusDays(1).atStartOfDay());
        assertTrue(servicio.calcularFilas().isEmpty());
    }

    @Test void sinDatosNoSeInventanComisiones() {
        var precios = mock(PrecioComisionRepository.class);
        var ordenes = mock(OrdenRepository.class);
        when(precios.findAll()).thenReturn(List.of());
        when(ordenes.findAll()).thenReturn(List.of());
        var datos = new ComisionesService(precios, ordenes, mock(LiquidacionComisionRepository.class)).getComisionesData();
        assertEquals(List.of(), datos.get("filas"));
        assertEquals(List.of(), datos.get("distribucionModelo"));
    }

    @Test void avanceSincronizaOrdenYExigeCalidad() {
        var lotes = mock(LoteRepository.class);
        var avances = mock(LoteAvanceRepository.class);
        var usuario = Usuario.builder().nombre("Operador").build();
        var orden = Orden.builder().numero("OP-2026-0001").estado("calidad").registradoPor(usuario).build();
        var lote = Lote.builder().id("lote").codigo("LT-2611-0001").orden(orden).areaActual("Calidad")
            .fechaEntrega(LocalDate.now().minusDays(1)).unidades(3).retrasado(true).build();
        when(lotes.findById("lote")).thenReturn(Optional.of(lote));
        var servicio = new ProduccionService(lotes, avances, mock(HistorialService.class), mock(SaldosLoteService.class));
        assertThrows(BusinessException.class, () -> servicio.registrarAvance(new AvanceRequest("lote", "Listo", "rechazado", ""), usuario));
        assertEquals("Calidad", lote.getAreaActual());
        servicio.registrarAvance(new AvanceRequest("lote", "Listo", "aprobado", ""), usuario);
        assertEquals("listo_despacho", orden.getEstado());
        assertFalse(lote.getRetrasado());
        verify(avances).save(argThat(a -> "Calidad".equals(a.getAreaOrigen()) && a.getFin() != null));
        assertThrows(BusinessException.class, () -> servicio.registrarAvance(new AvanceRequest("lote", "Corte", "", ""), usuario));
    }

    @Test void permiteRetrabajoDeCalidadYBloqueaSaltos() {
        var lotes = mock(LoteRepository.class);
        var avances = mock(LoteAvanceRepository.class);
        var orden = Orden.builder().estado("calidad").build();
        var lote = Lote.builder().id("lote").codigo("LT-1").orden(orden).areaActual("Calidad").fechaEntrega(LocalDate.now()).build();
        when(lotes.findById("lote")).thenReturn(Optional.of(lote));
        var servicio = new ProduccionService(lotes, avances, mock(HistorialService.class), mock(SaldosLoteService.class));
        servicio.registrarAvance(new AvanceRequest("lote", "Tapizado", "rechazado", "Rehacer"), new Usuario());
        assertEquals("en_proceso", orden.getEstado());
        assertThrows(BusinessException.class, () -> servicio.registrarAvance(new AvanceRequest("lote", "Listo", "aprobado", ""), new Usuario()));
    }

    @Test void despachoCanceladoNoReapareceProgramadoYMetricasRespetanFecha() {
        var repo = mock(DespachoRepository.class);
        var cancelado = Despacho.builder().codigo("DS-1").cliente("Cliente").destino("Lima").estado("cancelado").unidades(5).fechaSalida(LocalDate.now()).build();
        var anterior = Despacho.builder().codigo("DS-2").cliente("Cliente").destino("Lima").estado("entregado").unidades(20).fechaSalida(LocalDate.now().minusDays(1)).build();
        when(repo.findAllByOrderByCreadoEnDesc()).thenReturn(List.of(cancelado, anterior));
        var servicio = new DespachoService(repo, mock(DespachoLoteRepository.class), mock(LoteRepository.class), mock(HistorialService.class), mock(org.springframework.jdbc.core.JdbcTemplate.class));
        var datos = servicio.getDespachosData();
        var filas = (List<Map<String, Object>>) datos.get("despachos");
        assertEquals("cancelado", filas.get(0).get("estado"));
        var kpis = (List<Map<String, Object>>) datos.get("kpis");
        assertEquals("0", kpis.get(0).get("valor"));
        assertEquals("0", kpis.get(1).get("valor"));
        assertEquals("0", kpis.get(3).get("valor"));
        when(repo.findByCodigo("DS-1")).thenReturn(Optional.of(cancelado));
        assertThrows(BusinessException.class, () -> servicio.actualizarEstado("DS-1", "inexistente", new Usuario()));
        verify(repo, never()).save(any());
    }

    @Test void resumenVacioDevuelveCeros() {
        var ordenes = mock(OrdenRepository.class);
        var lotes = mock(LoteRepository.class);
        var avances = mock(LoteAvanceRepository.class);
        when(ordenes.findAll()).thenReturn(List.of());
        when(lotes.findAll()).thenReturn(List.of());
        when(avances.findAllByOrderByInicioDesc()).thenReturn(List.of());
        var datos = new DashboardService(ordenes, lotes, avances, mock(SaldosLoteService.class)).getDashboardData();
        assertEquals(0, datos.get("totalOrdenesActivas"));
        assertTrue(((List<Map<String, Object>>) datos.get("ritmo")).stream().allMatch(d -> Integer.valueOf(0).equals(d.get("valor"))));
    }

    @Test void listarOrdenesCargaLotesUnaVezSinConsultaPorCadaFila() {
        var ordenes = mock(OrdenRepository.class);
        var modelos = mock(ModeloRepository.class);
        var lotes = mock(LoteRepository.class);
        var numeros = mock(NumeroOrdenService.class);
        var o1 = Orden.builder().id("1").numero("OP-1").estado("borrador").prioridad("Alta").build();
        var o2 = Orden.builder().id("2").numero("OP-2").estado("pendiente_corte").prioridad("Normal").build();
        when(ordenes.findAll()).thenReturn(List.of(o1, o2));
        when(lotes.findAll()).thenReturn(List.of(Lote.builder().orden(o2).codigo("LT-2").build()));
        var filas = new OrdenService(ordenes, modelos, lotes, numeros, mock(HistorialService.class)).listar(null, null);
        assertEquals("—", filas.get(0).getLote());
        assertEquals("LT-2", filas.get(1).getLote());
        verify(lotes, times(1)).findAll();
        verify(lotes, never()).findByOrdenNumero(anyString());
    }

    @Test void numeracionBloqueaAntesDeLeerElMaximoYNoTruncaSecuencia() {
        var repo = mock(OrdenRepository.class);
        var jdbc = mock(org.springframework.jdbc.core.JdbcTemplate.class);
        when(repo.findMaxSecuenciaPorAnio(anyString())).thenReturn(9999);
        var numero = new NumeroOrdenService(repo, jdbc).generarSiguiente();
        assertTrue(numero.endsWith("-10000"));
        var orden = inOrder(jdbc, repo);
        orden.verify(jdbc).execute("SELECT pg_advisory_xact_lock(127954, 1)");
        orden.verify(repo).findMaxSecuenciaPorAnio(anyString());
    }
}
