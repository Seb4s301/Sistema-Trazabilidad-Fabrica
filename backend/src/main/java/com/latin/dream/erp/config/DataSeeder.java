package com.latin.dream.erp.config;

import com.latin.dream.erp.domain.entity.*;
import com.latin.dream.erp.domain.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final ModeloRepository modeloRepository;
    private final OrdenRepository ordenRepository;
    private final LoteRepository loteRepository;
    private final DespachoRepository despachoRepository;
    private final PrecioComisionRepository precioComisionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeedProperties seedProperties;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      ModeloRepository modeloRepository,
                      OrdenRepository ordenRepository,
                      LoteRepository loteRepository,
                      DespachoRepository despachoRepository,
                      PrecioComisionRepository precioComisionRepository,
                      PasswordEncoder passwordEncoder,
                      SeedProperties seedProperties) {
        this.usuarioRepository = usuarioRepository;
        this.modeloRepository = modeloRepository;
        this.ordenRepository = ordenRepository;
        this.loteRepository = loteRepository;
        this.despachoRepository = despachoRepository;
        this.precioComisionRepository = precioComisionRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedProperties = seedProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedUsuarios();
        seedModelos();
        seedPreciosComision();
        seedOrdenesYLotes();
        seedDespachos();
    }

    private void seedUsuarios() {
        if (usuarioRepository.count() > 0) {
            log.info("La tabla usuarios ya tiene datos; seed omitido.");
            return;
        }

        String passwordEncriptada = passwordEncoder.encode(seedProperties.getPassword());

        for (SeedProperties.UsuarioSemilla semilla : seedProperties.getUsuarios()) {
            Usuario usuario = Usuario.builder()
                .nombre(semilla.getNombre())
                .correo(semilla.getCorreo())
                .password(passwordEncriptada)
                .rol(semilla.getRol())
                .turno(semilla.getTurno())
                .activo(true)
                .build();
            usuarioRepository.save(usuario);
        }
        log.info("Seed usuarios completado.");
    }

    private void seedModelos() {
        if (modeloRepository.count() > 0) return;

        List<Modelo> modelos = List.of(
            Modelo.builder().codigo("CP-001").nombre("Comfort Plus").categoria("Queen").dimensiones("160 × 200 cm · Altura: 30 cm").relleno("Resortes Ensacados").activo(true).build(),
            Modelo.builder().codigo("DR-002").nombre("Dream Rest").categoria("King").dimensiones("180 × 200 cm · Altura: 30 cm").relleno("Memory Foam").activo(true).build(),
            Modelo.builder().codigo("ES-003").nombre("Eco Sleep").categoria("2 Plazas").dimensiones("140 × 190 cm · Altura: 26 cm").relleno("Resortes Ensacados").activo(true).build(),
            Modelo.builder().codigo("PR-004").nombre("Premium Rest").categoria("1.5 Plazas").dimensiones("120 × 190 cm · Altura: 26 cm").relleno("Memory Foam").activo(true).build()
        );
        modeloRepository.saveAll(modelos);
        log.info("Seed modelos completado: 4 modelos.");
    }

    private void seedPreciosComision() {
        if (precioComisionRepository.count() > 0) return;

        List<Modelo> modelos = modeloRepository.findAll();
        if (modelos.isEmpty()) return;

        List<PrecioComision> precios = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        for (Modelo m : modelos) {
            BigDecimal base = switch (m.getCodigo()) {
                case "CP-001" -> new BigDecimal("400.00");
                case "DR-002" -> new BigDecimal("500.00");
                case "ES-003" -> new BigDecimal("250.00");
                default -> new BigDecimal("200.00");
            };
            BigDecimal pct = switch (m.getCodigo()) {
                case "CP-001" -> new BigDecimal("6.00");
                case "DR-002" -> new BigDecimal("5.00");
                case "ES-003" -> new BigDecimal("4.00");
                default -> new BigDecimal("4.00");
            };

            precios.add(PrecioComision.builder()
                .modelo(m)
                .categoria(m.getCategoria())
                .precioBase(base)
                .porcentaje(pct)
                .vigenteDesde(hoy.minusMonths(3))
                .build());
        }

        precioComisionRepository.saveAll(precios);
        log.info("Seed precios comision completado.");
    }

    private void seedOrdenesYLotes() {
        if (ordenRepository.count() > 0) return;

        List<Modelo> modelos = modeloRepository.findAll();
        List<Usuario> usuarios = usuarioRepository.findAll();
        if (modelos.isEmpty() || usuarios.isEmpty()) return;

        Modelo cp = modelos.stream().filter(m -> "CP-001".equals(m.getCodigo())).findFirst().orElse(modelos.get(0));
        Modelo dr = modelos.stream().filter(m -> "DR-002".equals(m.getCodigo())).findFirst().orElse(modelos.get(0));
        Modelo es = modelos.stream().filter(m -> "ES-003".equals(m.getCodigo())).findFirst().orElse(modelos.get(0));
        Modelo pr = modelos.stream().filter(m -> "PR-004".equals(m.getCodigo())).findFirst().orElse(modelos.get(0));

        Usuario u1 = usuarios.get(0);
        Usuario u2 = usuarios.size() > 1 ? usuarios.get(1) : u1;
        LocalDate hoy = LocalDate.now();

        Object[][] ordenesData = {
            {"OP-2026-0420", cp, 120, "Alta", hoy.plusDays(4), "pendiente_corte", "Corte", "LT-2610-0420", false},
            {"OP-2026-0419", es, 60, "Normal", hoy.plusDays(2), "en_proceso", "Corte", "LT-2610-0419", false},
            {"OP-2026-0418", cp, 180, "Alta", hoy.minusDays(1), "en_proceso", "Ensamblaje", "LT-2610-0418", true},
            {"OP-2026-0417", dr, 80, "Normal", hoy.plusDays(3), "en_proceso", "Ensamblaje", "LT-2610-0417", false},
            {"OP-2026-0416", pr, 110, "Baja", hoy.plusDays(5), "en_proceso", "Tapizado", "LT-2610-0416", false},
            {"OP-2026-0415", cp, 160, "Alta", hoy.minusDays(2), "en_proceso", "Tapizado", "LT-2610-0415", true},
            {"OP-2026-0414", es, 110, "Normal", hoy.plusDays(1), "calidad", "Calidad", "LT-2610-0414", false},
            {"OP-2026-0413", pr, 80, "Normal", hoy.minusDays(3), "listo_despacho", "Listo", "LT-2610-0413", false}
        };

        for (Object[] d : ordenesData) {
            String num = (String) d[0];
            Modelo m = (Modelo) d[1];
            int cant = (int) d[2];
            String prior = (String) d[3];
            LocalDate fEnt = (LocalDate) d[4];
            String est = (String) d[5];
            String area = (String) d[6];
            String codLote = (String) d[7];
            boolean retraso = (boolean) d[8];

            Orden orden = Orden.builder()
                .numero(num)
                .modelo(m)
                .cantidad(cant)
                .prioridad(prior)
                .fechaEntrega(fEnt)
                .estado(est)
                .registradoPor(u1)
                .turno("Turno A")
                .build();
            Orden ordGuardada = ordenRepository.save(orden);

            Lote lote = Lote.builder()
                .codigo(codLote)
                .orden(ordGuardada)
                .unidades(cant)
                .areaActual(area)
                .fechaEntrega(fEnt)
                .retrasado(retraso)
                .build();
            loteRepository.save(lote);
        }

        log.info("Seed ordenes y lotes completado: 8 órdenes con sus lotes.");
    }

    private void seedDespachos() {
        if (despachoRepository.count() > 0) return;

        LocalDate hoy = LocalDate.now();

        List<Despacho> despachos = List.of(
            Despacho.builder()
                .codigo("DS-1048")
                .cliente("Comercial Andina")
                .destino("Lima Centro")
                .unidades(80)
                .estado("programado")
                .fechaSalida(hoy)
                .fechaEntregaEstimada(hoy)
                .vehiculo("LD-201")
                .conductor("Diego Ruiz")
                .build(),
            Despacho.builder()
                .codigo("DS-1047")
                .cliente("Casa Norte")
                .destino("Lima Norte")
                .unidades(60)
                .estado("transito")
                .fechaSalida(hoy)
                .fechaEntregaEstimada(hoy)
                .vehiculo("LD-202")
                .conductor("Carla Vega")
                .build(),
            Despacho.builder()
                .codigo("DS-1046")
                .cliente("Muebles del Sur")
                .destino("Lima Sur")
                .unidades(40)
                .estado("entregado")
                .fechaSalida(hoy)
                .fechaEntregaEstimada(hoy)
                .vehiculo("LD-203")
                .conductor("Marco Silva")
                .build()
        );

        despachoRepository.saveAll(despachos);
        log.info("Seed despachos completado: 3 despachos.");
    }
}
