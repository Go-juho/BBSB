package com.rhwngh.board.domain.repository;

import com.rhwngh.board.domain.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    Page<BoardEntity> findByTitleContaining(String keyword, Pageable pageable);
    Long countByTitleContaining(String keyword);
}
