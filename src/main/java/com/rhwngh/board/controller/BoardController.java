package com.rhwngh.board.controller;

import com.rhwngh.board.UserCustom;
import com.rhwngh.board.domain.entity.FileEntity;
import com.rhwngh.board.domain.entity.MemberEntity;
import com.rhwngh.board.dto.BoardDto;
import com.rhwngh.board.dto.FileDto;
import com.rhwngh.board.dto.MemberDto;
import com.rhwngh.board.service.BoardService;
import com.rhwngh.board.service.FileService;
import com.rhwngh.board.service.MemberService;
import com.rhwngh.board.utill.MD5Generator;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.io.File;

@Controller
@RequestMapping(value="/board")
//@AllArgsConstructor
public class BoardController {
    private BoardService boardService;
    private FileService fileService;

    public BoardController(BoardService boardService, FileService fileService) {
        this.boardService = boardService;
        this.fileService = fileService;
    }

//    @GetMapping
//    public String list(Model model, @RequestParam(value="page", defaultValue = "1") Integer pageNum) {
//        List<BoardDto> boardList = boardService.getBoardlist(pageNum);
//        Integer[] pageList = boardService.getPageList(pageNum);
//
//        //System.out.println(pageList.getClass().getName());
//        model.addAttribute("boardList", boardList);
//        model.addAttribute("pageList", pageList);
//
//        return "board/list.html";
//    }

    @GetMapping
    public String list(Model model, @RequestParam(value="page", defaultValue = "1") Integer pageNum, @RequestParam(value="keyword", defaultValue = "") String keyword) {
        List<BoardDto> boardList = boardService.getBoardlist(pageNum, keyword);
        Integer[] pageList = boardService.getPageList(pageNum, keyword);

        //System.out.println(pageList.getClass().getName());
        model.addAttribute("boardList", boardList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pageList", pageList);

        return "board/list.html";
    }

//    @GetMapping("/search")
//    public String search(@RequestParam(value="keyword") String keyword, Model model) {
//        List<BoardDto> boardDtoList = boardService.searchPosts(keyword);
//
//        model.addAttribute("boardList", boardDtoList);
//        model.addAttribute("keyword", keyword);
//        //System.out.println("keyword : " + keyword);
//
//        return "board/list.html";
//    }

    @GetMapping("/post/{no}")
    public String detail(@PathVariable("no") Long no, Model model) {
        BoardDto boardDTO = boardService.getPost(no);
        Long fileId = boardDTO.getFileId();

        FileDto fileDto = fileService.getFile(fileId);

        model.addAttribute("boardDto", boardDTO);
        model.addAttribute("fileName", fileDto.getOrigFilename());
        return "board/detail.html";
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> fileDownload(@PathVariable("fileId") Long fileId) throws IOException {
        FileDto fileDto = fileService.getFile(fileId);
        Path path = Paths.get(fileDto.getFilePath());
        Resource resource = new InputStreamResource(Files.newInputStream(path));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDto.getOrigFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/post/edit/{no}")
    public String edit(@PathVariable("no") Long no, Model model) {
        BoardDto boardDTO = boardService.getPost(no);


        model.addAttribute("boardDto", boardDTO);

        return "board/update.html";
    }

    @PutMapping("/post/edit/{no}")
    public String update(@RequestParam("file")MultipartFile files, BoardDto boardDto) {

        System.out.println("boardDTO : " + boardDto);
        System.out.println("boardDTO_info : " + boardDto.getClass().getName());

        try {
            String origFilename = files.getOriginalFilename();
            if (!origFilename.isEmpty()) {
                String fileName = new MD5Generator(origFilename).toString();
                /* 실행되는 위치의 'files' 폴더에 파일이 저장됩니다. */
                String savePath = System.getProperty("user.dir") + "\\files";
                /* 파일이 저장되는 폴더가 없으면 폴더를 생성합니다. */
                if (!new File(savePath).exists()) {
                    try {
                        new File(savePath).mkdir();
                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                }
                String filePath = savePath + "\\" + fileName;
                files.transferTo(new File(filePath));

                FileDto fileDto = new FileDto();
                fileDto.setOrigFilename(origFilename);
                fileDto.setFileName(fileName);
                fileDto.setFilePath(filePath);

                Long fileId = fileService.saveFile(fileDto);

                //FileEntity fileEntity = new FileEntity(fileId,origFilename,fileName,filePath);

                boardDto.setFileId(fileId);

                //System.out.println(fileId);
                //System.out.println("----------------------------------------------------");
                //System.out.println(boardDto.getId());
                //System.out.println(boardDto.getFileId());
            }
            boardService.savePost(boardDto);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return "redirect:/board";
    }

    @DeleteMapping("/post/{no}")
    public String delete(@PathVariable("no") Long no) {
        boardService.deletePost(no);

        return "redirect:/board";
    }

    @GetMapping("/post")
    public String write(Model model, @AuthenticationPrincipal UserCustom user) {
        //System.out.println(user);

        model.addAttribute("user_name", user.getName());

        return "board/write.html";
    }

    @PostMapping("/post")
    public String write(@RequestParam("file")MultipartFile files, BoardDto boardDto) {
        try {
            String origFilename = files.getOriginalFilename();
            if (!origFilename.isEmpty()) {
                String fileName = new MD5Generator(origFilename).toString();
                /* 실행되는 위치의 'files' 폴더에 파일이 저장됩니다. */
                String savePath = System.getProperty("user.dir") + "\\files";
                /* 파일이 저장되는 폴더가 없으면 폴더를 생성합니다. */
                if (!new File(savePath).exists()) {
                    try {
                        new File(savePath).mkdir();
                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                }
                String filePath = savePath + "\\" + fileName;
                files.transferTo(new File(filePath));

                FileDto fileDto = new FileDto();
                fileDto.setOrigFilename(origFilename);
                fileDto.setFileName(fileName);
                fileDto.setFilePath(filePath);

                Long fileId = fileService.saveFile(fileDto);

                //FileEntity fileEntity = new FileEntity(fileId,origFilename,fileName,filePath);

                boardDto.setFileId(fileId);

                //System.out.println(fileId);
                //System.out.println("----------------------------------------------------");
                //System.out.println(boardDto.getId());
                //System.out.println(boardDto.getFileId());


            }
            boardService.savePost(boardDto);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return "redirect:/board";
    }
}
