package org.elysia.api.mapper;

import org.elysia.api.DTO.phone.PhoneDTO;
import org.elysia.api.entity.PhoneNumber;
import org.springframework.stereotype.Component;

@Component
public class PhoneMapper {
    public PhoneDTO toDTO (PhoneNumber phoneNumber){
        return new PhoneDTO(
                phoneNumber.getId(),
                phoneNumber.getName(),
                phoneNumber.getNumber(),
                phoneNumber.getCreated_at()
        );
    }
    public PhoneNumber toEntity (PhoneDTO phoneDTO){
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setId(phoneDTO.id());
        phoneNumber.setName(phoneDTO.name());
        phoneNumber.setNumber(phoneDTO.number());
        phoneNumber.setCreated_at(phoneDTO.created_at());
        return phoneNumber;
    }
}
