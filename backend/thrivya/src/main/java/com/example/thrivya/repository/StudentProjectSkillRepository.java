package com.example.thrivya.repository;

import com.example.thrivya.model.StudentProjectSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentProjectSkillRepository
        extends JpaRepository<StudentProjectSkill, Long> {

    List<StudentProjectSkill>
    findAllByStudentProjectId(Long studentProjectId);
}