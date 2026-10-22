package com.example.thrivya.repository;

import com.example.thrivya.model.ProjectStatus;
import com.example.thrivya.model.StudentProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentProjectRepository
        extends JpaRepository<StudentProject, Long> {

    List<StudentProject>
    findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<StudentProject>
    findAllByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            ProjectStatus status
    );
}