package com.rhwngh.board.service;

import com.rhwngh.board.domain.entity.BoardEntity;
import com.rhwngh.board.dto.BoardDto;
import com.rhwngh.board.domain.repository.BoardRepository;
import com.rhwngh.board.dto.FileDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class BoardService {
    private BoardRepository boardRepository;

//    @Transactional
//    public List<BoardDto> getBoardlist() {
//        List<BoardEntity> boardEntities = boardRepository.findAll();
//        List<BoardDto> boardDtoList = new ArrayList<>();
//
//        for ( BoardEntity boardEntity : boardEntities) {
//            BoardDto boardDTO = BoardDto.builder()
//                    .id(boardEntity.getId())
//                    .title(boardEntity.getTitle())
//                    .content(boardEntity.getContent())
//                    .writer(boardEntity.getWriter())
//                    .createdDate(boardEntity.getCreatedDate())
//                    .build();
//
//            boardDtoList.add(boardDTO);
//        }
//
//        return boardDtoList;
//    }

    @Transactional
    public BoardDto getPost(Long id) {
        Optional<BoardEntity> boardEntityWrapper = boardRepository.findById(id);
        BoardEntity boardEntity = boardEntityWrapper.get();

        BoardDto boardDTO = BoardDto.builder()
                .id(boardEntity.getId())
                .title(boardEntity.getTitle())
                .content(boardEntity.getContent())
                .writer(boardEntity.getWriter())
                .fileId(boardEntity.getFileId())
                .createdDate(boardEntity.getCreatedDate())
                .build();
        return boardDTO;
    }

    @Transactional
    public Long savePost(BoardDto boardDto) {
        return boardRepository.save(boardDto.toEntity()).getId();
    }

    @Transactional
    public void deletePost(Long id) {
        boardRepository.deleteById(id);
    }

    private BoardDto convertEntityToDto(BoardEntity boardEntity) {
        return BoardDto.builder()
                .id(boardEntity.getId())
                .title(boardEntity.getTitle())
                .content(boardEntity.getContent())
                .writer(boardEntity.getWriter())
                .createdDate(boardEntity.getCreatedDate())
                .build();
    }

//    @Transactional
//    public List<BoardDto> searchPosts(String keyword) {
//        List<BoardEntity> boardEntities = boardRepository.findByTitleContaining(keyword);
//        List<BoardDto> boardDtoList = new ArrayList<>();
//
//        if (boardEntities.isEmpty()) return boardDtoList;
//
//        for (BoardEntity boardEntity : boardEntities) {
//            boardDtoList.add(this.convertEntityToDto(boardEntity));
//        }
//
//        return boardDtoList;
//    }


    private static final int BLOCK_PAGE_NUM_COUNT = 5;  // ????????? ???????????? ????????? ?????? ???
    private static final int PAGE_POST_COUNT = 1;       // ??? ???????????? ???????????? ????????? ???

    @Transactional
    public List<BoardDto> getBoardlist(Integer pageNum, String keyword) {
        Page<BoardEntity> page;
        if (!keyword.isEmpty() && keyword != "") {
            page = boardRepository.findByTitleContaining(keyword, PageRequest.of(pageNum - 1, PAGE_POST_COUNT, Sort.by(Sort.Direction.ASC, "createdDate")));
        } else {
            page = boardRepository.findAll(PageRequest.of(pageNum - 1, PAGE_POST_COUNT, Sort.by(Sort.Direction.ASC, "createdDate")));
        }

        List<BoardEntity> boardEntities = page.getContent();
        List<BoardDto> boardDtoList = new ArrayList<>();

        if (boardEntities.isEmpty()) return boardDtoList;

        for (BoardEntity boardEntity : boardEntities) {
            boardDtoList.add(this.convertEntityToDto(boardEntity));
        }

        return boardDtoList;
    }

    @Transactional
    public Long getBoardCount() {
        return boardRepository.count();
    }

    @Transactional
    public Long getSearchCount(String keyword) {
        return boardRepository.countByTitleContaining(keyword);
    }

    public Integer[] getPageList(Integer curPageNum, String keyword) {
        Integer[] pageList = new Integer[BLOCK_PAGE_NUM_COUNT];

        // ??? ????????? ??????
        Double postsTotalCount;
        if (!keyword.isEmpty() && keyword != "") {
            postsTotalCount = Double.valueOf(this.getSearchCount(keyword));
        } else {
            postsTotalCount = Double.valueOf(this.getBoardCount());
        }

        // ??? ????????? ???????????? ????????? ????????? ????????? ?????? ?????? (???????????? ??????)
        Integer totalLastPageNum = (int)(Math.ceil((postsTotalCount/PAGE_POST_COUNT)));

        Integer blockStartPageNum = (curPageNum <= BLOCK_PAGE_NUM_COUNT / 2)
                ? 1
                : curPageNum - BLOCK_PAGE_NUM_COUNT / 2;

        // ?????? ???????????? ???????????? ????????? ????????? ????????? ?????? ??????
        //Integer blockLastPageNum = (totalLastPageNum > curPageNum + BLOCK_PAGE_NUM_COUNT)
        //        ? curPageNum + BLOCK_PAGE_NUM_COUNT
        //        : totalLastPageNum;
        Integer blockLastPageNum =
                (totalLastPageNum > blockStartPageNum + BLOCK_PAGE_NUM_COUNT - 1 )
                        ? blockStartPageNum + BLOCK_PAGE_NUM_COUNT - 1
                        : totalLastPageNum;

        // ????????? ?????? ?????? ??????
        curPageNum = (curPageNum <= 3) ? 1 : curPageNum - 2;

        // ????????? ?????? ??????
        for (int val = curPageNum, idx = 0; val <= blockLastPageNum; val++, idx++) {
            pageList[idx] = val;
            //System.out.println("????????? : " + pageList[idx]);
        }

        System.out.println("??? ????????? ?????? : " + postsTotalCount);
        //System.out.println("??? ????????? ?????? ????????? ????????? ?????? : " + totalLastPageNum);
        //System.out.println("?????? ????????? ?????? ????????? ????????? ?????? : " + blockLastPageNum);
        //System.out.println("????????? ?????? ?????? : " + curPageNum);


        return pageList;
    }
}
