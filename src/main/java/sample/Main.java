package sample;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Random;

public class Main extends Application {
    @FXML
    private Button btnShoot;
    @FXML
    private Button btnNewGame;
    @FXML
    private Button btnOpponent;
    @FXML
    private TextArea textArea;

    @FXML
    private TextArea textAreaOpponent;


    @FXML
    private TextField textField;

    String userId;
    Stage window;
    Scene newGameScene, mainScene;
    String gameId;
    Boolean startFirst;

    String userName;


    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("mainBoard.fxml")));
        mainScene = new Scene(root, 580, 430);
        btnShoot = (Button) root.getChildrenUnmodifiable().get(0);
        btnOpponent = (Button) root.getChildrenUnmodifiable().get(2);
        textArea = (TextArea) root.getChildrenUnmodifiable().get(3);
        textAreaOpponent = (TextArea) root.getChildrenUnmodifiable().get(4);
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("welcomeBoard.fxml")));
        btnNewGame = (Button) root.getChildrenUnmodifiable().get(0);
        textField = (TextField) root.getChildrenUnmodifiable().get(1);

        btnNewGame.setOnAction(e ->
        {
            Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
            alert1.setHeaderText("Ooops");
            if(textField.getText().equals("") || textField.getText().equals("user name")){
                alert1.setContentText("You need to type your name to continue Spaceship game.");
                alert1.showAndWait();
            }else{
                userName = textField.getText();
                gameId = createGame();
                if(gameId == null || gameId.equals("")){
                    alert1.setContentText("You need to type different name to continue Spaceship game.");
                    alert1.showAndWait();
                }else{
                    setBoard();
                    if (startFirst) {
                        btnOpponent.setDisable(true);
                    } else {
                        btnShoot.setDisable(true);
                    }
                    window.setScene(mainScene);
                }
            }
        });
        newGameScene = new Scene(root, 400, 250);
        window.setTitle("XL Spaceship Game");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hey There ...");
        alert.setHeaderText("Info");
        alert.setContentText("This functionality you are trying to use is under construction.");
        btnShoot.setOnAction(e -> {
            //sendSalvo(gameId);

            alert.showAndWait();
            btnShoot.setDisable(true);
            btnOpponent.setDisable(false);

        });
        btnOpponent.setOnAction(e -> {
            alert.showAndWait();
            //TODO : implement method
            btnShoot.setDisable(false);
            btnOpponent.setDisable(true);
        });
        window.setScene(newGameScene);
        window.show();
    }

    private void setBoard() {
        String gameStatusResponse = getGameStatus(gameId);
        JSONObject gameStatusJson = new JSONObject(gameStatusResponse);
        String board = ((JSONObject) gameStatusJson.get("self")).get("board").toString();
        String boardOpponent = ((JSONObject) gameStatusJson.get("opponent")).get("board").toString();
        textArea.setText(board);
        textAreaOpponent.setText(boardOpponent);
    }

    private String getGameStatus(String gameId) {
        String response = "";
        try {
            String link = "http://localhost:8080/xl-spaceship/user/game/" + gameId + "?userID=" + userId;

            HttpURLConnection con = getHttpURLConnection(link, "GET", null);
            response = readResponse(con);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private String sendSalvo(String gameId) {
        String response = "";
        try {
            String link = "http://localhost:8080/xl-spaceship/protocol/game/" + gameId;
            String body = "{\n" +
                    "\"salvo\": [\"0x0\", \"8x4\", \"DxA\", \"AxA\", \"7xF\"]\n" +
                    "}";
            HttpURLConnection con = getHttpURLConnection(link, "PUT", body);
            response = readResponse(con);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private HttpURLConnection getHttpURLConnection(String link, String requestType, String body) throws IOException {
        URL url = new URL(link);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(requestType);
        con.setRequestProperty("Content-Type", "application/json");
        if (body != null) {
            con.setDoOutput(true);
            OutputStream outputStream = con.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
            outputStreamWriter.write(body);
            outputStreamWriter.flush();
            outputStreamWriter.close();
            outputStream.close();
        }

        return con;
    }

    private String createGame() {
        String response = "";
        try {
            Random random = new Random();
            String link = "http://localhost:8080/xl-spaceship/protocol/game/new";
            String body = "{\n" +
                    "\"user_id\": \"xebialabs-" + userName + "\",\n" +
                    "\"full_name\": \"XebiaLabs Opponent2\",\n" +
                    "\"spaceship_protocol\": {\n" +
                    "\"hostname\": \"127.0.0.1\",\n" +
                    "\"port\": 9001\n" +
                    "}\n" +
                    "}";
            HttpURLConnection con = getHttpURLConnection(link, "POST", body);
            JSONObject obj = new JSONObject(readResponse(con));
            System.out.println(con.getRequestMethod());
            response = obj.get("game_id").toString();
            startFirst = obj.get("starting").toString().equals(obj.get("user_id").toString());
            userId = obj.get("user_id").toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;

    }

    private String readResponse(HttpURLConnection con) throws IOException {
        String response = "";
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String line;
            StringBuilder content = new StringBuilder();

            while ((line = in.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
            response = content.toString();
        }
        return response;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
