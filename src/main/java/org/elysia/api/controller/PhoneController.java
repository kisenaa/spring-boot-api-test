package org.elysia.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.elysia.api.DTO.phone.PhoneCreateRequest;
import org.elysia.api.DTO.phone.PhoneDTO;
import org.elysia.api.DTO.phone.PhoneUpdateRequest;
import org.elysia.api.service.PhoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name ="phone number", description = "Operation related to phone")
@RequestMapping("/phone")
@CrossOrigin
public class PhoneController {
    private final PhoneService phoneService;

    @Autowired
    public PhoneController(PhoneService phoneService) {
        this.phoneService = phoneService;
    }

    @GetMapping("/")
    public String index(){
        return "Hello World";
    }

    @PostMapping("/add")
    public ResponseEntity<PhoneDTO> createPhone(@Valid @RequestBody PhoneCreateRequest phoneCreateRequest){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(phoneService.savePhoneNumber(phoneCreateRequest));
    }


    @GetMapping("/list/all")
    public ResponseEntity<List<PhoneDTO>> listAllPhones(){
        return ResponseEntity.ok(phoneService.getPhoneNumbers());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> listPhoneById(@PathVariable("id") int id) {
        return ResponseEntity.ok(phoneService.getPhoneNumber(id));
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updatePhone(
            @PathVariable("id") int id,
            @Valid @RequestBody PhoneUpdateRequest phoneUpdateRequest) {

        return phoneService.updatePhoneNumber(id, phoneUpdateRequest)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Phone number with id %d not found".formatted(id))));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String,String>> deletePhone(@PathVariable("id") int id){
        phoneService.deletePhoneNumber(id);
        String msg = "Delete phone number with id: %d".formatted(id);
        return ResponseEntity.ok(Map.of("message", msg));
    }

}

