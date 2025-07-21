package org.mobelite.editormanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EditorManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EditorManagerApplication.class, args);
        System.out.println("EditorManager Application started");
    }

}
