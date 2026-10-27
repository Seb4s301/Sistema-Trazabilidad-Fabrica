package com.latin.dream.erp.application.service;

import com.latin.dream.erp.domain.repository.OrdenRepository;
import com.latin.dream.erp.crosscutting.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación: genera el correlativo de órdenes OP-AAAA-NNNN.
 */
@Service
public class NumeroOrdenService {

    private static final String PREFIJO = "OP";
    private static final int LONGITUD_SEC = 4;

    private final OrdenRepository ordenRepository;

    public NumeroOrdenService(OrdenRepository ordenRepository) {
        this.ordenRepository = ordenRepository;
    }

    @Transactional
    public String generarSiguiente() {
        String anio = String.valueOf(java.time.Year.now().getValue());
        Integer maximo = ordenRepository.findMaxSecuenciaPorAnio(anio);
        int siguiente = (maximo == null ? 0 : maximo) + 1;
        String secuencial = String.format("%0" + LONGITUD_SEC + "d", siguiente);
        String numero = PREFIJO + "-" + anio + "-" + secuencial;

        if (ordenRepository.existsByNumero(numero)) {
            throw new BusinessException("NUMERO_DUPLICADO", "El número de orden " + numero + " ya existe");
        }
        return numero;
    }
}
