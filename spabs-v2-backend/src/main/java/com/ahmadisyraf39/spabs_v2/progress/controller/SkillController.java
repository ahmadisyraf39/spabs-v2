package com.ahmadisyraf39.spabs_v2.progress.controller;

import com.ahmadisyraf39.spabs_v2.progress.dto.request.SkillRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.SkillResponse;
import com.ahmadisyraf39.spabs_v2.progress.service.SkillService;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.AgeGroup;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.TeamCategory;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    public ResponseEntity<SkillResponse> create(@Valid @RequestBody SkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(skillService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<SkillResponse>> getAll() {
        return ResponseEntity.ok(skillService.getAll());
    }

    @GetMapping(params = {"ageGroup", "category"})
    public ResponseEntity<List<SkillResponse>> getByAgeGroupAndCategory(
            @RequestParam AgeGroup ageGroup, @RequestParam TeamCategory category) {
        return ResponseEntity.ok(skillService.getByAgeGroupAndCategory(ageGroup, category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SkillResponse> update(@PathVariable Long id, @Valid @RequestBody SkillRequest request) {
        return ResponseEntity.ok(skillService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        skillService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
