package com.electroverse.controller;

import com.electroverse.dto.request.ShoppingRequests;
import com.electroverse.dto.response.ShoppingResponses;
import com.electroverse.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<List<ShoppingResponses.Address>> list() {
        return ResponseEntity.ok(addressService.listMine());
    }

    @PostMapping
    public ResponseEntity<ShoppingResponses.Address> create(@Valid @RequestBody ShoppingRequests.Address request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShoppingResponses.Address> update(@PathVariable Long id,
                                                            @Valid @RequestBody ShoppingRequests.Address request) {
        return ResponseEntity.ok(addressService.update(id, request));
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<ShoppingResponses.Address> setDefault(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.setDefault(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
