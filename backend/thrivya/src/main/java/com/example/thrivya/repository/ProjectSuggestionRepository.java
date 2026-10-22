package com.example.thrivya.repository;

import com.example.thrivya.model.ProjectSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectSuggestionRepository
        extends JpaRepository<ProjectSuggestion, Long> {

    List<ProjectSuggestion> findAllByRoleIdAndActiveTrue(
            Long roleId
    );

    List<ProjectSuggestion> findAllByActiveTrue();
}