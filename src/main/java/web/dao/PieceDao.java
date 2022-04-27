package web.dao;

import static chess.position.File.A;
import static chess.position.File.B;
import static chess.position.File.C;
import static chess.position.File.D;
import static chess.position.File.E;
import static chess.position.File.F;
import static chess.position.File.G;
import static chess.position.File.H;
import static chess.position.Rank.EIGHT;
import static chess.position.Rank.FIVE;
import static chess.position.Rank.FOUR;
import static chess.position.Rank.ONE;
import static chess.position.Rank.SEVEN;
import static chess.position.Rank.SIX;
import static chess.position.Rank.THREE;
import static chess.position.Rank.TWO;

import chess.piece.Color;
import chess.position.File;
import chess.position.Position;
import chess.position.Rank;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import web.dto.PieceDto;
import web.dto.PieceType;

@Repository
public class PieceDao {

    private static final Map<String, Rank> RANKS = Map.of(
            "1", ONE, "2", TWO, "3", THREE, "4", FOUR, "5", FIVE, "6", SIX, "7", SEVEN, "8", EIGHT
    );
    private static final Map<String, File> FILES = Map.of(
            "A", A, "B", B, "C", C, "D", D, "E", E, "F", F, "G", G, "H", H
    );

    private final JdbcTemplate jdbcTemplate;

    public PieceDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PieceDto> findPieces(int chessGameId) {
        return jdbcTemplate.query("SELECT chess_game_id, position, type, color FROM piece WHERE chess_game_id = ?",
                this::mapToPieceDTO, chessGameId);
    }

    private PieceDto mapToPieceDTO(ResultSet rs, int rowNum) throws SQLException {
        Position position = createPosition(rs.getString("position"));
        PieceType type = PieceType.valueOf(rs.getString("type"));
        Color color = Color.valueOf(rs.getString("color"));
        return new PieceDto(position, type, color);
    }

    private Position createPosition(String position) {
        File file = FILES.get(position.substring(0, 1));
        Rank rank = RANKS.get(position.substring(1, 2));
        return new Position(file, rank);
    }

    public void deletePieceByPosition(int chessGameId, Position position) {
        jdbcTemplate
                .update("DELETE FROM piece WHERE chess_game_id = ? and position = ?", chessGameId, position.toString());
    }

    public void savePiece(int chessGameId, PieceDto pieceDto) {
        jdbcTemplate.update("INSERT INTO piece(chess_game_id, position, type, color) VALUES(?, ?, ?, ?)",
                chessGameId, pieceDto.getPosition().toString(), pieceDto.getType().name(), pieceDto.getColor().name());
    }

    public void savePieces(int chessGameId, List<PieceDto> pieceDtos) {
        for (PieceDto pieceDTO : pieceDtos) {
            savePiece(chessGameId, pieceDTO);
        }
    }

    public void deleteByChessGameId(int chessGameId) {
        jdbcTemplate.update("DELETE FROM piece WHERE chess_game_id = ?", chessGameId);
    }

    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM piece");
    }
}
