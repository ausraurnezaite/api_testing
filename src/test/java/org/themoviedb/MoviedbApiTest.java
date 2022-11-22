package org.themoviedb;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.themoviedb.properties.PropertiesStorage.*;

public class MoviedbApiTest {

    private static final int MOVIE_ID = 496243;
    private static String requestToken;
    private static String sessionId;

    static {
        baseURI = API_BASE_URI;
    }

    @BeforeClass
    public void startSession() {
        initSession();
    }

    @Test
    public void testGet() {
        given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .queryParam("api_key", API_KEY)
                .pathParams("movie_id", MOVIE_ID)
                .when()
                .get("/movie/{movie_id}")
                .then()
                .statusCode(200)
                .body("title", equalTo("Parasite"))
                .log().body();
    }

    @Test
    public void testPostRateMovie() {
        JSONObject request = new JSONObject();
        request.put("value", 10.0);

        given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .body(request.toJSONString())
                .queryParam("api_key", API_KEY)
                .queryParam("session_id", sessionId)
                .pathParams("movie_id", MOVIE_ID)
                .when()
                .post("/movie/{movie_id}/rating")
                .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .log().body();
    }

    @Test
    public void testPostAddToWatchList() {
        String accountId = getAccountId();

        JSONObject request = new JSONObject();
        request.put("media_type", "movie");
        request.put("media_id", MOVIE_ID);
        request.put("watchlist", true);

        given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .body(request.toJSONString())
                .queryParam("api_key", API_KEY)
                .queryParam("session_id", sessionId)
                .pathParams("account_id", accountId)
                .when()
                .post("/account/{account_id}/watchlist")
                .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .log().body();
    }

    @Test
    public void testDeleteMovieRating() {
        given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .queryParam("api_key", API_KEY)
                .queryParam("session_id", sessionId)
                .pathParams("movie_id", MOVIE_ID)
                .when()
                .delete("/movie/{movie_id}/rating")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .log().body();
    }

    @AfterClass
    public void deleteSession() {
        JSONObject request = new JSONObject();
        request.put("session_id", sessionId);

        given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .body(request.toJSONString())
                .when()
                .queryParam("api_key", API_KEY)
                .delete("/authentication/session")
                .then()
                .log()
                .body();
    }

    private void initSession() {
        requestToken = getRequestToken();
        verifyRequestToken();
        sessionId = getSessionId();
    }

    private String getRequestToken() {
        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .queryParam("api_key", API_KEY)
                .when()
                .get("/authentication/token/new")
                .then()
                .statusCode(200)
                .log().body().extract().path("request_token").toString();
    }


    private void verifyRequestToken() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", MOVIEDB_USERNAME);
        requestBody.put("password", MOVIEDB_PASSWORD);
        requestBody.put("request_token", requestToken);

        given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .body(requestBody.toJSONString())
                .queryParam("api_key", API_KEY)
                .when()
                .post("/authentication/token/validate_with_login")
                .then()
                .statusCode(200)
                .log()
                .body();
    }

    private String getSessionId() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("request_token", requestToken);

        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .body(requestBody.toJSONString())
                .queryParam("api_key", API_KEY)
                .when()
                .post("/authentication/session/new")
                .then()
                .statusCode(200)
                .log().body().extract().path("session_id").toString();
    }

    private String getAccountId() {
        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .queryParam("api_key", API_KEY)
                .queryParam("session_id", sessionId)
                .when()
                .get("/account")
                .then()
                .statusCode(200)
                .log().body().extract().path("id").toString();
    }
}