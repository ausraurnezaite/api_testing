package org.themoviedb;

import io.qameta.allure.Description;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.themoviedb.properties.PropertiesStorage.*;

public class RequestSpecificationBuilderApiTest {

    private static final int MOVIE_ID = 496243;
    private static String requestToken;
    private static String sessionId;

    @BeforeClass
    public void startSession() {
        initSession();
    }

    @Test
    @Description("Verify the ability to receive movie information with GET request.")
    public void testGet() {

        given(createBaseRequestSpecification())
                .pathParams("movie_id", MOVIE_ID)
                .when()
                .get("/movie/{movie_id}")
                .then()
                .statusCode(200)
                .body("title", equalTo("Parasite"))
                .log().body();
    }

    @Test
    @Description("Verify the ability to rate the movie with POST request.")
    public void testPostRateMovie() {

        JSONObject requestBody = new JSONObject();
        requestBody.put("value", 10.0);

        given(createFullRequestSpecification())
                .body(requestBody.toJSONString())
                .pathParams("movie_id", MOVIE_ID)
                .when()
                .post("/movie/{movie_id}/rating")
                .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .log().body();
    }

    @Test
    @Description("Verify the ability to add movie to watchlist with POST request.")
    public void testPostAddToWatchList() {

        String accountId = getAccountId();

        JSONObject requestBody = new JSONObject();
        requestBody.put("media_type", "movie");
        requestBody.put("media_id", MOVIE_ID);
        requestBody.put("watchlist", true);

        given(createFullRequestSpecification())
                .pathParams("account_id", accountId)
                .body(requestBody.toJSONString())
                .when()
                .post("/account/{account_id}/watchlist")
                .then()
                .statusCode(201)
                .body("success", equalTo(true))
                .log().body();
    }

    private String getAccountId() {
        return given(createFullRequestSpecification())
                .when()
                .get("/account")
                .then()
                .statusCode(200)
                .log().body().extract().path("id").toString();
    }

    @Test
    @Description("Verify the ability to delete the movie rating with POST request.")
    public void testDeleteMovieRating() {

        given(createFullRequestSpecification())
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
        JSONObject requestBody = new JSONObject();
        requestBody.put("session_id", sessionId);

        given(createBaseRequestSpecification())
                .body(requestBody.toJSONString())
                .when()
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

    private RequestSpecification createBaseRequestSpecification() {
        RequestSpecBuilder reqBuilder = new RequestSpecBuilder();

        reqBuilder.setBaseUri(API_BASE_URI);
        reqBuilder.setContentType(ContentType.JSON);
        reqBuilder.addQueryParam("api_key", API_KEY);
        reqBuilder.addFilter(new AllureRestAssured());
        return reqBuilder.build();
    }

    private RequestSpecification createFullRequestSpecification() {
        RequestSpecBuilder reqBuilder = new RequestSpecBuilder();

        reqBuilder.setBaseUri(API_BASE_URI);
        reqBuilder.setContentType(ContentType.JSON);
        reqBuilder.addQueryParam("api_key", API_KEY);
        reqBuilder.addQueryParam("session_id", sessionId);
        reqBuilder.addFilter(new AllureRestAssured());
        return reqBuilder.build();
    }

    private String getRequestToken() {
        return given(createBaseRequestSpecification())
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

        given(createBaseRequestSpecification())
                .body(requestBody.toJSONString())
                .post("/authentication/token/validate_with_login")
                .then()
                .statusCode(200)
                .log().body();
    }

    private String getSessionId() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("request_token", requestToken);

        return given(createBaseRequestSpecification())
                .body(requestBody.toJSONString())
                .when()
                .post("/authentication/session/new")
                .then()
                .statusCode(200)
                .log().body().extract().path("session_id").toString();
    }
}