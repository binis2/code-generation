package net.binis.demo.entity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;

@Slf4j
public class BaseEntityModifier {

    public void save() {
        log.info("save called!");
    }

    public void doSomething(JpaRepository repo) {
        log.info("doSomething called! {}", repo);
    }

}
