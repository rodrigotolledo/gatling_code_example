package com.myapplication.app

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import assertions._

class UploadFileScenario extends Simulation {

  val httpConf = httpConfig
    .baseURL("http://my-application:8080")
    .acceptHeader("image/png,image/*;q=0.8,*/*;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .connection("keep-alive")
    .userAgentHeader("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36")

    val scn = scenario("Upload a file")

    .exec(http("homepage_GET")
        .get("/my-application/app/service/workgroup/latest")
        .header("Content-Type", "application/json")
        )

    .exec(http("attach_txt_file_POST")
        .post("/my-application/app/service/file/upload/")
        .header("Accept", "text/html")
        .upload("myFile", "file.txt", "text/plain")
        .check(regex("<div id=\'fileId\'>(.*?)<\\/div>")
        .saveAs("fileId"))
        )

    .exec(http("upload_txt_file_POST")
        .post("/my-application/app/service/workgroup/")
        .body( """{"name": "my-perf-test-${fileId}", "fileId": "${fileId}"}""").asJSON
        .check(jsonPath("workgroupId")
        .saveAs("workgroupId"))
        )

    .exec(http("run_content_match_GET")
        .get("/my-application/app/service/workgroup/contentmatch/${workgroupId}")
        .header("Content-Type", "application/json")
        )

    setUp(
        scn.users(10).ramp(30).protocolConfig(httpConf)
        )

    assertThat(
        global.failedRequests.count.is(0)
        )
