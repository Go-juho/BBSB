package com.rhwngh.board.domain.repository;

import com.rhwngh.board.domain.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, Long>{
}
