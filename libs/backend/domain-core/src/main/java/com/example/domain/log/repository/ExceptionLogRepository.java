package com.example.domain.log.repository;

import com.example.domain.log.entity.ExceptionLog;
import com.example.domain.log.entity.ExceptionLogId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExceptionLogRepository extends JpaRepository<ExceptionLog, ExceptionLogId> {
}
