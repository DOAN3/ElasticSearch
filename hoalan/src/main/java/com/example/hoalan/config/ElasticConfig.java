package com.example.hoalan.config;

//import javafx.scene.NodeBuilder;
import org.elasticsearch.common.settings.Settings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.io.File;
import java.io.IOException;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.hoalan.repository")
public class ElasticConfig {
    @Bean
    NodeBuilder nodeBuilder(){
        return new NodeBuilde();
    }

    @Bean
    void elasticsearchOperations(){
        File tmpDir = null;
        try {
            tmpDir = File.createTempFile("temp-elastic", Long.toString(System.nanoTime()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Settings.Builder elasticsearchSettings =
                Settings.settingsBuilder()
                        .put("http.enabled", "true")
                        .put("index.number_of_shards", "1")
                        .put("path.data", new File(tmpDir, "data").getAbsolutePath())
                        .put("path.logs", new File(tmpDir, "logs").getAbsolutePath())
                        .put("path.work", new File(tmpDir, "work").getAbsolutePath())
                        .put("path.home", tmpDir);

        return new ElasticsearchTemplate(nodeBuilder().local(true).settings(elasticsearchSettings.build()).node().client());
    }
}
