package com.letuc.tester;

import com.letuc.tester.sender.SimpleSender;
import com.letuc.tester.task.SimpleTask;
import com.letuc.tester.task.TaskFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.Thread.sleep;

@SpringBootApplication
@Data
@AllArgsConstructor
public class TesterApplication implements CommandLineRunner {

    ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(TesterApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        TemplateLoader templateLoader = new TemplateLoader(Path.of("user.dir").resolve("autodoc").resolve("autodoc_config.yaml"));
        Path jsonFile = Path.of(System.getProperty("user.dir")).resolve("autodoc").resolve("AutoDoc.json");
        String JSON = Files.readString(jsonFile);
        System.out.println(JSON);
        TaskFactory taskFactory = new TaskFactory("http://localhost:8000");
        SimpleSender simpleSender = applicationContext.getBean(SimpleSender.class);
        List<SimpleTask> tasks = taskFactory.createTasksFromJson(JSON);
        while (true) {
            for (SimpleTask task : tasks) {
                try {
                    String finalRequestBody = null;
                    String template = task.getRequestBodyTemplate();
                    if (template != null) {
                        finalRequestBody = template
                                .replace("\"string_placeholder\"", "\"runner_test_user\"")
                                .replace("0", "99")
                                .replace("false", "true");
                    }
                    ResponseEntity<String> response = simpleSender.sendGenericRequest(
                            task.getUrl(),
                            task.getMethod(),
                            task.getHeaders(),
                            finalRequestBody
                    );
                    if (response != null) {
                        System.out.println(response.getBody());
                    } else {
                        System.err.println("warning: 响应是空的");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    sleep(2000);
                }
            }
        }
    }
}
