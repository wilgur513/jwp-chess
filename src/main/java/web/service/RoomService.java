package web.service;

import chess.Score;
import chess.piece.Color;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.dao.ChessGameDao;
import web.dao.PieceDao;
import web.dao.RoomDao;
import web.dto.ChessGameDto;
import web.dto.GameStatus;
import web.dto.RoomDto;
import web.exception.InvalidRoomRequestException;
import web.exception.IsRunningChessGameException;

@Service
@Transactional
public class RoomService {

    private final RoomDao roomDao;
    private final PieceDao pieceDao;
    private final ChessGameDao chessGameDao;
    private final ChessGameService chessGameService;

    public RoomService(RoomDao roomDao, PieceDao pieceDao, ChessGameDao chessGameDao,
                       ChessGameService chessGameService) {
        this.roomDao = roomDao;
        this.pieceDao = pieceDao;
        this.chessGameDao = chessGameDao;
        this.chessGameService = chessGameService;
    }

    public RoomDto saveRoom(String name, String password) {
        Score initialScore = new Score(new BigDecimal("38.0"));
        int chessGameId = chessGameDao.saveChessGame(GameStatus.READY, Color.WHITE, initialScore, initialScore);
        chessGameService.prepareNewChessGame(chessGameId);
        return roomDao.saveRoom(name, password, chessGameId);
    }

    public void deleteRoom(int id, String password) {
        RoomDto room = roomDao.findById(id);

        if (room == null || !room.getPassword().equals(password)) {
            throw new InvalidRoomRequestException();
        }

        ChessGameDto chessGame = chessGameDao.findById(room.getChessGameId());

        if (chessGame.getStatus() == GameStatus.RUNNING) {
            throw new IsRunningChessGameException();
        }

        roomDao.deleteById(id);
        pieceDao.deleteByChessGameId(room.getChessGameId());
        chessGameDao.deleteById(room.getChessGameId());
    }
}
