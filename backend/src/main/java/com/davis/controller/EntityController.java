package com.davis.controller;

import com.davis.model.Entity;
import com.davis.service.EntityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entities")
@CrossOrigin(origins = "*")
public class EntityController {

    private final EntityService entityService;

    public EntityController(EntityService entityService) {
        this.entityService = entityService;
    }

    @GetMapping
    public List<Entity> getAllEntities() {
        return entityService.getAllEntities();
    }

    @GetMapping("/{entityId}")
    public ResponseEntity<Entity> getEntityById(
            @PathVariable Long entityId) {

        return entityService.getEntityById(entityId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Entity> createEntity(
            @RequestBody Entity entity) {

        Entity savedEntity = entityService.saveEntity(entity);
        return ResponseEntity.ok(savedEntity);
    }

    @DeleteMapping("/{entityId}")
    public ResponseEntity<Void> deleteEntity(
            @PathVariable Long entityId) {

        if (entityService.getEntityById(entityId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        entityService.deleteEntity(entityId);
        return ResponseEntity.noContent().build();
    }
}