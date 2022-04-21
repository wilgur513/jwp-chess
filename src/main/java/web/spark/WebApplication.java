package web.spark;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;
import web.spark.controller.ChessGameController;
import web.spark.controller.LobbyController;
import web.spark.dao.ChessGameDao;
import web.spark.dao.JdbcTemplate;
import web.spark.dao.PieceDao;
import web.spark.service.ChessGameService;

public class WebApplication {

    public static void main(String[] args) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        PieceDao pieceDao = new PieceDao(jdbcTemplate);
        ChessGameDao chessGameDao = new ChessGameDao(jdbcTemplate);

        ChessGameService chessGameService = new ChessGameService(pieceDao, chessGameDao);

        ChessGameController chessGameController = new ChessGameController(chessGameService, chessGameDao, pieceDao);
        LobbyController lobbyController = new LobbyController(chessGameDao);

        port(8080);
        staticFileLocation("/static");

        get("/", (req, res) -> render(lobbyController.lobby(req, res)));
        post("/create-chess-game", (req, res) -> render(lobbyController.createChessGame(req, res)));

        get("/chess-game", (req, res) -> render(chessGameController.chessGame(req, res)));
        post("/chess-game/move", (req, res) -> render(chessGameController.move(req, res)));
    }

    private static String render(ModelAndView modelAndView) {
        return new HandlebarsTemplateEngine().render(modelAndView);
    }

}
