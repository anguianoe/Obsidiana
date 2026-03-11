package com.nexcoyo.knowledge.obsidiana.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/book")
public class BookController {

    @GetMapping("/list")
    public ResponseEntity<List<Object>> listBooks() {
        return ResponseEntity.ok(Collections.emptyList());
    }
}
