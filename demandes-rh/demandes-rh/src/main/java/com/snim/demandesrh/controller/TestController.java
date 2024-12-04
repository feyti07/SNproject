package com.snim.demandesrh.controller;

import com.snim.demandesrh.entities.dto.DemandeDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping(path = "/hello")
    public ResponseEntity<String> sayHello(){
        return   ResponseEntity.status(HttpStatus.OK).body("Hello every one");
    }

    @PostMapping(path = "/create")
    public ResponseEntity<?> save(
            @RequestBody DemandeDto request
    ) {
        return ResponseEntity.status(200).body("okey");
    }
}
