package com.electroverse.service;

import com.electroverse.dto.request.ShoppingRequests;
import com.electroverse.dto.response.ShoppingResponses;
import com.electroverse.entity.Address;
import com.electroverse.entity.User;
import com.electroverse.exception.ResourceNotFoundException;
import com.electroverse.mapper.DtoMapper;
import com.electroverse.repository.AddressRepository;
import com.electroverse.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final SecurityUtils securityUtils;

    public AddressService(AddressRepository addressRepository, SecurityUtils securityUtils) {
        this.addressRepository = addressRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<ShoppingResponses.Address> listMine() {
        User user = securityUtils.getCurrentUser();
        return addressRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(DtoMapper::toAddress).toList();
    }

    @Transactional
    public ShoppingResponses.Address create(ShoppingRequests.Address request) {
        User user = securityUtils.getCurrentUser();
        Address address = new Address();
        address.setUser(user);
        apply(address, request);
        boolean isFirst = addressRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).isEmpty();
        if (Boolean.TRUE.equals(request.defaultAddress()) || isFirst) {
            addressRepository.clearDefaultForUser(user.getId());
            address.setDefaultAddress(true);
        }
        return DtoMapper.toAddress(addressRepository.save(address));
    }

    @Transactional
    public ShoppingResponses.Address update(Long id, ShoppingRequests.Address request) {
        User user = securityUtils.getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + id));
        apply(address, request);
        if (Boolean.TRUE.equals(request.defaultAddress())) {
            addressRepository.clearDefaultForUser(user.getId());
            address.setDefaultAddress(true);
        }
        return DtoMapper.toAddress(addressRepository.save(address));
    }

    @Transactional
    public void delete(Long id) {
        User user = securityUtils.getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + id));
        addressRepository.delete(address);
    }

    @Transactional
    public ShoppingResponses.Address setDefault(Long id) {
        User user = securityUtils.getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + id));
        addressRepository.clearDefaultForUser(user.getId());
        address.setDefaultAddress(true);
        return DtoMapper.toAddress(addressRepository.save(address));
    }

    private void apply(Address address, ShoppingRequests.Address request) {
        address.setFullName(request.fullName().trim());
        address.setPhone(request.phone());
        address.setLine1(request.line1());
        address.setLine2(request.line2());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country());
    }
}
