package com.nitaudit.controller;

import com.nitaudit.model.RawMaterial;
import com.nitaudit.repository.RawMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/raw-materials")
@RequiredArgsConstructor
public class RawMaterialController {
    private final RawMaterialRepository repo;

    @GetMapping
    public List<RawMaterial> getAll() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<RawMaterial> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public RawMaterial create(@RequestBody RawMaterial r) { return repo.save(r); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRODUCTION')")
    public ResponseEntity<RawMaterial> update(@PathVariable Long id, @RequestBody RawMaterial r) {
        return repo.findById(id).map(existing -> {
            existing.setName(r.getName()); existing.setUnit(r.getUnit());
            existing.setCurrentStock(r.getCurrentStock());
            existing.setReorderLevel(r.getReorderLevel());
            existing.setCostPerUnit(r.getCostPerUnit());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
