package org.elysia.api.service;

import org.elysia.api.DTO.phone.PhoneCreateRequest;
import org.elysia.api.DTO.phone.PhoneDTO;
import org.elysia.api.DTO.phone.PhoneUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface PhoneService {
    List<PhoneDTO> getPhoneNumbers();
    void deletePhoneNumber(int id);
    PhoneDTO savePhoneNumber(PhoneCreateRequest phoneCreateRequest);
    PhoneDTO getPhoneNumber(int id);
    Optional<PhoneDTO> updatePhoneNumber(int id, PhoneUpdateRequest phoneUpdateRequest);
}