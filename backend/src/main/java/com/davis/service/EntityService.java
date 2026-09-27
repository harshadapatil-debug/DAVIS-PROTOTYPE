package com.davis.service;

import com.davis.model.Entity;
import com.davis.repository.EntityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EntityService {

    private final EntityRepository entityRepository;

    public EntityService(EntityRepository entityRepository) {
        this.entityRepository = entityRepository;
    }

    public List<Entity> getAllEntities() {
        return entityRepository.findAll();
    }

    public Optional<Entity> getEntityById(Long entityId) {
        return entityRepository.findById(entityId);
    }

    public List<Entity> getEntitiesByCaseId(Long caseId) {
        return entityRepository.findEntitiesByCaseId(caseId);
    }

    public Entity saveEntity(Entity entity) {
        return entityRepository.save(entity);
    }

    public void deleteEntity(Long entityId) {
        entityRepository.deleteById(entityId);
    }
}