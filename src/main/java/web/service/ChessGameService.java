package web.service;

import static chess.position.File.A;
import static chess.position.File.B;
import static chess.position.File.C;
import static chess.position.File.D;
import static chess.position.File.E;
import static chess.position.File.F;
import static chess.position.File.G;
import static chess.position.File.H;
import static chess.position.Rank.EIGHT;
import static chess.position.Rank.ONE;
import static chess.position.Rank.SEVEN;
import static chess.position.Rank.TWO;
import static java.util.stream.Collectors.toMap;

import chess.ChessBoard;
import chess.piece.Bishop;
import chess.piece.Color;
import chess.piece.King;
import chess.piece.Knight;
import chess.piece.Pawn;
import chess.piece.Piece;
import chess.piece.Queen;
import chess.piece.Rook;
import chess.position.Position;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import web.controller.Movement;
import web.dao.ChessGameDao;
import web.dao.PieceDao;
import web.dto.ChessGameDto;
import web.dto.GameStatus;
import web.dto.PieceDto;
import web.exception.ChessGameException;

@Service
public class ChessGameService {

    private final PieceDao pieceDao;
    private final ChessGameDao chessGameDao;

    public ChessGameService(PieceDao pieceDao, ChessGameDao chessGameDao) {
        this.pieceDao = pieceDao;
        this.chessGameDao = chessGameDao;
    }

    public ChessGameDto move(int chessGameId, Movement movement) {
        List<PieceDto> pieces = pieceDao.findPieces(chessGameId);
        ChessGameDto chessGameDto = chessGameDao.findById(chessGameId);
        ChessBoard chessBoard = createChessBoard(pieces, chessGameDto);
        movePiece(chessGameId, movement, chessBoard);
        return updateChessBoard(chessBoard, movement, chessGameDto);
    }

    private ChessBoard createChessBoard(List<PieceDto> pieces, ChessGameDto chessGameDto) {
        return new ChessBoard(createBoard(pieces), chessGameDto.getCurrentColor());
    }

    private Map<Position, Piece> createBoard(List<PieceDto> pieces) {
        return pieces.stream()
            .collect(toMap(PieceDto::getPosition, PieceDto::createPiece));
    }

    private void movePiece(int chessGameId, Movement movement, ChessBoard chessBoard) {
        try {
            chessBoard.move(movement.getFrom(), movement.getTo());
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ChessGameException(chessGameId, e.getMessage());
        }
    }

    private ChessGameDto updateChessBoard(ChessBoard chessBoard, Movement movement,
                                          ChessGameDto chessGameDto) {
        updatePieces(chessGameDto, chessBoard, movement);
        return updateChessGame(chessBoard, chessGameDto);
    }

    private void updatePieces(ChessGameDto chessGameDto, ChessBoard chessBoard, Movement movement) {
        Map<Position, Piece> board = chessBoard.getBoard();
        pieceDao.deletePieceByPosition(chessGameDto.getId(), movement.getFrom());
        pieceDao.deletePieceByPosition(chessGameDto.getId(), movement.getTo());
        pieceDao.savePiece(chessGameDto.getId(),
            new PieceDto(movement.getTo(), board.get(movement.getTo())));
    }

    private ChessGameDto updateChessGame(ChessBoard chessBoard, ChessGameDto chessGameDto) {
        ChessGameDto newChessGameDto = createNewChessGameDto(chessBoard, chessGameDto);
        chessGameDao.updateChessGame(newChessGameDto);
        return newChessGameDto;
    }

    private ChessGameDto createNewChessGameDto(ChessBoard chessBoard, ChessGameDto chessGameDto) {
        GameStatus status = GameStatus.RUNNING;
        String winner = chessGameDto.getWinner();

        if (chessBoard.isFinished()) {
            status = GameStatus.FINISHED;
            winner = chessBoard.getWinner().name();
        }

        return new ChessGameDto(chessGameDto.getId(), status, chessBoard.getScore(Color.BLACK),
            chessBoard.getScore(Color.WHITE), chessBoard.getCurrentColor(), winner);
    }

    public void prepareNewChessGame(int chessGameId) {
        pieceDao.deleteByChessGameId(chessGameId);
        pieceDao.savePieces(chessGameId, createPieces());
    }

    private List<PieceDto> createPieces() {
        return Stream.concat(createWhitePieces().stream(), createBlackPieces().stream())
            .collect(Collectors.toList());
    }

    private static List<PieceDto> createWhitePieces() {
        return List.of(
            new PieceDto(new Position(A, ONE), new Rook(Color.WHITE)),
            new PieceDto(new Position(B, ONE), new Knight(Color.WHITE)),
            new PieceDto(new Position(C, ONE), new Bishop(Color.WHITE)),
            new PieceDto(new Position(D, ONE), new Queen(Color.WHITE)),
            new PieceDto(new Position(E, ONE), new King(Color.WHITE)),
            new PieceDto(new Position(F, ONE), new Bishop(Color.WHITE)),
            new PieceDto(new Position(G, ONE), new Knight(Color.WHITE)),
            new PieceDto(new Position(H, ONE), new Rook(Color.WHITE)),
            new PieceDto(new Position(A, TWO), new Pawn(Color.WHITE)),
            new PieceDto(new Position(B, TWO), new Pawn(Color.WHITE)),
            new PieceDto(new Position(C, TWO), new Pawn(Color.WHITE)),
            new PieceDto(new Position(D, TWO), new Pawn(Color.WHITE)),
            new PieceDto(new Position(E, TWO), new Pawn(Color.WHITE)),
            new PieceDto(new Position(F, TWO), new Pawn(Color.WHITE)),
            new PieceDto(new Position(G, TWO), new Pawn(Color.WHITE)),
            new PieceDto(new Position(H, TWO), new Pawn(Color.WHITE)));
    }

    private static List<PieceDto> createBlackPieces() {
        return List.of(
            new PieceDto(new Position(A, EIGHT), new Rook(Color.BLACK)),
            new PieceDto(new Position(B, EIGHT), new Knight(Color.BLACK)),
            new PieceDto(new Position(C, EIGHT), new Bishop(Color.BLACK)),
            new PieceDto(new Position(D, EIGHT), new Queen(Color.BLACK)),
            new PieceDto(new Position(E, EIGHT), new King(Color.BLACK)),
            new PieceDto(new Position(F, EIGHT), new Bishop(Color.BLACK)),
            new PieceDto(new Position(G, EIGHT), new Knight(Color.BLACK)),
            new PieceDto(new Position(H, EIGHT), new Rook(Color.BLACK)),
            new PieceDto(new Position(A, SEVEN), new Pawn(Color.BLACK)),
            new PieceDto(new Position(B, SEVEN), new Pawn(Color.BLACK)),
            new PieceDto(new Position(C, SEVEN), new Pawn(Color.BLACK)),
            new PieceDto(new Position(D, SEVEN), new Pawn(Color.BLACK)),
            new PieceDto(new Position(E, SEVEN), new Pawn(Color.BLACK)),
            new PieceDto(new Position(F, SEVEN), new Pawn(Color.BLACK)),
            new PieceDto(new Position(G, SEVEN), new Pawn(Color.BLACK)),
            new PieceDto(new Position(H, SEVEN), new Pawn(Color.BLACK)));
    }
}
