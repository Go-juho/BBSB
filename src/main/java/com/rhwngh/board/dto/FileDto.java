package com.rhwngh.board.dto;

import com.rhwngh.board.domain.entity.FileEntity;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FileDto {
    private Long id;
    private String origFilename;
    private String fileName;
    private String filePath;

    public FileEntity toEntity() {
        FileEntity fileEntity = FileEntity.builder()
                .id(id)
                .origFilename(origFilename)
                .fileName(fileName)
                .filePath(filePath)
                .build();
            return fileEntity;
    }

    @Builder
    public FileDto(Long id, String origFilename, String fileName, String filePath) {
        this.id = id;
        this.origFilename = origFilename;
        this.fileName = fileName;
        this.filePath = filePath;
    }
}
