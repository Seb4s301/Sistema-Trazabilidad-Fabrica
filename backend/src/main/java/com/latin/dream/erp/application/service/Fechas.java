package com.latin.dream.erp.application.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class Fechas {
    private static final DateTimeFormatter CORTA = DateTimeFormatter.ofPattern("d MMM uuuu", Locale.forLanguageTag("es-PE"));
    private Fechas() {}
    static String corta(LocalDate fecha) { return fecha == null ? "—" : fecha.format(CORTA); }
}
