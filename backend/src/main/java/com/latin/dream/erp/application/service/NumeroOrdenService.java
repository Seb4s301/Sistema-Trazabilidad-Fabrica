package com.latin.dream.erp.application.service;

import com.latin.dream.erp.domain.repository.OrdenRepository;
import com.latin.dream.erp.crosscutting.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación: Genera el correlativo de órdenes con formato OP-AAAA-NNNN.
 * Utiliza Apache Commons (lang3) StringUtils para formateo y padding seguro.
 */
@Service
public class NumeroOrdenService {

    private static final Logger log = LoggerFactory.getLogger(NumeroOrdenService.class);
    private static final String PREFIJO = "OP";
    private static final int LONGITUD_SEC = 4;

    private final OrdenRepository ordenRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public NumeroOrdenService(OrdenRepository ordenRepository, org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.ordenRepository = ordenRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void bloquearNumeracion() {
        jdbcTemplate.execute("SELECT pg_advisory_xact_lock(127954, 1)");
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public String generarSiguiente() {
        bloquearNumeracion();
        String anio = String.valueOf(java.time.Year.now().getValue());
        Integer maximo = ordenRepository.findMaxSecuenciaPorAnio(anio);
        int siguiente = (maximo == null ? 0 : maximo) + 1;
        String secuencial = StringUtils.leftPad(String.valueOf(siguiente), LONGITUD_SEC, '0');
        String numero = PREFIJO + "-" + anio + "-" + secuencial;

        if (ordenRepository.existsByNumero(numero)) {
            log.error("Conflicto al generar número de orden: {} ya existe", numero);
            throw new BusinessException("NUMERO_DUPLICADO", "El número de orden " + numero + " ya existe");
        }

        log.info("Nuevo número correlativo de orden generado: {}", numero);
        return numero;
    }
}
