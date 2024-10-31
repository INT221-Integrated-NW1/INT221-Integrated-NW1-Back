package sit.int221.nw1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.exception.AccessDeniedException;
import sit.int221.nw1.models.server.BoardStatus;
import sit.int221.nw1.models.server.Boards;
import sit.int221.nw1.models.server.Statuses;
import sit.int221.nw1.repositories.server.BoardStatusRepository;

import java.util.ArrayList;
import java.util.List;

// BoardStatusService.java
@Service
public class BoardStatusService {
    @Autowired
    private BoardStatusRepository boardStatusRepository;

    @Autowired
    private StatusesService statusesService;

    // Method to create default BoardStatus for a new Board
    public List<BoardStatus> createDefaultBoardStatus(Boards board) {
        List<BoardStatus> boardStatuses = new ArrayList<>();

        Statuses s1 = statusesService.getStatusById("000000000000001");
        BoardStatus bs1 = new BoardStatus(board, s1);
        boardStatuses.add(bs1);

        Statuses s2 = statusesService.getStatusById("000000000000002");
        BoardStatus bs2 = new BoardStatus(board, s2);
        boardStatuses.add(bs2);

        Statuses s3 = statusesService.getStatusById("000000000000003");
        BoardStatus bs3 = new BoardStatus(board, s3);
        boardStatuses.add(bs3);

        Statuses s4 = statusesService.getStatusById("000000000000004");
        BoardStatus bs4 = new BoardStatus(board, s4);
        boardStatuses.add(bs4);

        return boardStatuses;
    }
//    public List<BoardStatus> getAllStatusByBoardId(String boardId) {
//        List<BoardStatus> bs = boardStatusRepository.findBoardStatusesByBoards_BoardId(boardId);
//        return bs;
//    }
    // Method to create a new BoardStatus
    public BoardStatus createBoardStatus(Boards board, Statuses status) {
        BoardStatus bs = new BoardStatus(board, status);
        boardStatusRepository.save(bs);
        return bs;
    }

    // Save default BoardStatus for a new Board
    public void SaveDefaultBoardStatus(List<BoardStatus> boardStatuses) {
        boardStatusRepository.saveAll(boardStatuses);
    }

    public BoardStatus findBoardStatusByBoardIdAndStatusId(String boardId, String statusId) {
        BoardStatus bs = boardStatusRepository.findBoardStatusesByBoards_BoardIdAndStatus_Id(boardId, statusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "BoardStatus not found"));
        return bs;
    }

    public void deleteBoardStatusByBoardStatusId(Integer bsId) {
        System.out.println("Deleting BoardStatus with ID: " + bsId);
        boardStatusRepository.deleteById(bsId);
    }

    // Get all statuses for a specific Board by boardId with visibility check
    public List<BoardStatus> getAllStatusByBoardId(String boardId, String userId) {
        // Use the repository method to get the board
        Boards board = boardStatusRepository.findBoardById(boardId);

        // Check if the board is public or the user is the owner
        if (board.getVisibility().equals("PUBLIC") || board.getUser().getOid().equals(userId)) {
            return boardStatusRepository.findBoardStatusesByBoards_BoardId(boardId);
        }

        // If the board is private and the user is not the owner, throw an exception
        throw new AccessDeniedException("Access denied to board statuses");
    }
    public BoardStatus updateBoardStatusByBoardStatusId(BoardStatus bs) {
        return boardStatusRepository.save(bs);
    }
}
