package com.latin.dream.erp.presentation.controller;

import com.latin.dream.erp.domain.entity.Modelo;
import com.latin.dream.erp.domain.repository.ModeloRepository;
import com.latin.dream.erp.domain.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("${api.prefix}/catalogos")
public class CatalogoController {

    private final ModeloRepository modeloRepository;
    private final UsuarioRepository usuarioRepository;

    public CatalogoController(ModeloRepository modeloRepository, UsuarioRepository usuarioRepository) {
        this.modeloRepository = modeloRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCatalogos() {
        List<Modelo> modelos = modeloRepository.findAll();

        List<Map<String, Object>> dimensiones = List.of(
            Map.of("id", "1.5", "nombre", "1.5 Plazas", "ancho_cm", 120, "largo_cm", 190, "altura_cm", 26, "medidas", "1.5 Plazas: 120 × 190 cm · Altura: 26 cm"),
            Map.of("id", "2", "nombre", "2 Plazas", "ancho_cm", 140, "largo_cm", 190, "altura_cm", 26, "medidas", "2 Plazas: 140 × 190 cm · Altura: 26 cm"),
            Map.of("id", "queen", "nombre", "Queen", "ancho_cm", 160, "largo_cm", 200, "altura_cm", 30, "medidas", "Queen: 160 × 200 cm · Altura: 30 cm"),
            Map.of("id", "king", "nombre", "King", "ancho_cm", 180, "largo_cm", 200, "altura_cm", 30, "medidas", "King: 180 × 200 cm · Altura: 30 cm")
        );

        List<Map<String, Object>> rellenos = List.of(
            Map.of("id", "resortes", "nombre", "Resortes Ensacados", "descripcion", "Núcleo de resortes individuales envueltos en bolsas de tela para soporte independiente."),
            Map.of("id", "memory", "nombre", "Memory Foam", "descripcion", "Espuma viscoelástica que se adapta al cuerpo y alivia la presión.")
        );

        List<Map<String, Object>> usuarios = usuarioRepository.findAll().stream()
            .map(u -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", u.getId());
                map.put("nombre", u.getNombre());
                map.put("rol", u.getRol());
                map.put("correo", u.getCorreo());
                return map;
            })
            .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("modelos", modelos);
        response.put("dimensiones", dimensiones);
        response.put("rellenos", rellenos);
        response.put("usuarios", usuarios);

        return ResponseEntity.ok(response);
    }
}
