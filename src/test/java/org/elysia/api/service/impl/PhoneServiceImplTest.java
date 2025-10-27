package org.elysia.api.service.impl;

import org.elysia.api.DTO.phone.PhoneCreateRequest;
import org.elysia.api.DTO.phone.PhoneDTO;
import org.elysia.api.DTO.phone.PhoneUpdateRequest;
import org.elysia.api.entity.PhoneNumber;
import org.elysia.api.exception.ResourceNotFoundException;
import org.elysia.api.mapper.PhoneMapper;
import org.elysia.api.repository.PhoneRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@ExtendWith(MockitoExtension.class)
class PhoneServiceImplTest {
    @Spy
    private PhoneRepository phoneRepository = Mockito.mock(PhoneRepository.class,
            withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @InjectMocks
    private PhoneServiceImpl phoneService;

    @Spy
    private PhoneMapper phoneMapper = new PhoneMapper();

    @Test
    void testAddPhone() {
        PhoneCreateRequest phoneCreateRequest = new PhoneCreateRequest("Elysia", "0812952949495");
        int id = 1;
        LocalDateTime date = LocalDateTime.now();

        PhoneNumber savedEntity = new PhoneNumber();
        savedEntity.setId(id);
        savedEntity.setName(phoneCreateRequest.name());
        savedEntity.setNumber(phoneCreateRequest.number());
        savedEntity.setCreated_at(date);

        when(phoneRepository.save(any(PhoneNumber.class))).thenReturn(savedEntity);
        PhoneDTO result = phoneService.savePhoneNumber(phoneCreateRequest);

        assertEquals(id, result.id());
        assertEquals(phoneCreateRequest.name(), result.name());
        assertEquals(phoneCreateRequest.number(), result.number());
        assertEquals(date, result.created_at());

        verify(phoneRepository, times(1)).save(any(PhoneNumber.class));
        verify(phoneRepository, times(1)).saveData(any(PhoneNumber.class));
    }

    @Nested
    class testGetListPhoneNumber {
        @Test
        void testEmptyListPhoneNumber() {
            when(phoneRepository.findAll()).thenReturn(List.of());
            List<PhoneDTO> phoneNumbers = phoneService.getPhoneNumbers();
            assertTrue(phoneNumbers.isEmpty());
            verify(phoneRepository, times(1)).getData();
            verify(phoneRepository, times(1)).findAll();
        }

        @Test
        void testListPhoneNumber() {
            List<PhoneNumber> phoneNumbers = new ArrayList<>();
            phoneNumbers.add(constructPhoneNumber(1, "elysia0", "01234567890"));
            phoneNumbers.add(constructPhoneNumber(2, "elysia1", "01234567891"));
            phoneNumbers.add(constructPhoneNumber(3, "elysia2", "01234567892"));

            when(phoneRepository.findAll()).thenReturn(phoneNumbers);
            List<PhoneNumber> result = phoneService.getPhoneNumbers().stream().map(phoneMapper::toEntity).toList();

            assertEquals(phoneNumbers.size(), result.size());
            assertEquals(phoneNumbers.get(0).getId(), result.get(0).getId());
            assertEquals(phoneNumbers.get(1).getId(), result.get(1).getId());
            assertEquals(phoneNumbers.get(2).getId(), result.get(2).getId());
            assertEquals(phoneNumbers.get(0).getName(), result.get(0).getName());
            assertEquals(phoneNumbers.get(1).getName(), result.get(1).getName());
            assertEquals(phoneNumbers.get(2).getName(), result.get(2).getName());
            assertEquals(phoneNumbers.get(0).getNumber(), result.get(0).getNumber());
            assertEquals(phoneNumbers.get(1).getNumber(), result.get(1).getNumber());
            assertEquals(phoneNumbers.get(2).getNumber(), result.get(2).getNumber());
            verify(phoneRepository, times(1)).getData();
            verify(phoneRepository, times(1)).findAll();
        }
    }

    @Nested
    class testGetPhoneNumberById {
        @Test
        void testNotFound() {
            when(phoneRepository.findById(anyInt())).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> phoneService.getPhoneNumber(1));
            verify(phoneRepository, times(1)).getDataOrThrow(anyInt());
            verify(phoneRepository, times(1)).findById(anyInt());
        }

        @Test
        void foundPhoneNumber() {
            int id = 1;
            String name = "elysia0";
            String number = "01234567891";

            PhoneNumber phoneNumber = constructPhoneNumber(id,  name, number);
            when(phoneRepository.findById(1)).thenReturn(Optional.of(phoneNumber));
            PhoneDTO result = phoneService.getPhoneNumber(1);

            assertNotNull(result);
            assertEquals(phoneNumber.getName(), result.name());
            assertEquals(phoneNumber.getNumber(), result.number());
            verify(phoneRepository, times(1)).getDataOrThrow(anyInt());
            verify(phoneRepository, times(1)).findById(anyInt());
        }
    }

    @Nested
    class testDeletePhoneNumber {
        @Test
        void testValidDeletePhoneNumber() {
            // mock to return an entity to delete
            int id = 1;
            when(phoneRepository.findById(id)).thenReturn(Optional.of(new PhoneNumber()));
            phoneService.deletePhoneNumber(id);
            verify(phoneRepository, times(1)).findById(id);
            verify(phoneRepository, times(1)).deleteDataOrThrow(id);
        }

        @Test
        void testInvalidDeletePhoneNumber() {
            int id = 1;
            assertThrows(ResourceNotFoundException.class, () -> phoneService.deletePhoneNumber(id));
            verify(phoneRepository, times(1)).findById(id);
            verify(phoneRepository, times(1)).deleteDataOrThrow(id);

            assertThrows(ResourceNotFoundException.class, () -> phoneService.deletePhoneNumber(id));
            verify(phoneRepository, times(2)).deleteDataOrThrow(id);
        }
    }

    @Nested
    class testUpdatePhoneNumber {
        @Test
        void testInvalidUpdatePhoneNumber() {
            int id = 1;
            PhoneUpdateRequest phoneUpdateRequest = new PhoneUpdateRequest("elysia0", "01234567891");
            Optional<PhoneDTO> result = phoneService.updatePhoneNumber(id, phoneUpdateRequest);
            assertTrue(result.isEmpty());
        }

        @Test
        void testValidUpdatePhoneNumber() {
            int id = 1;
            PhoneNumber savedEntity = constructPhoneNumber(id, "elysia0", "01234567890");

            doAnswer(invocation -> {
                Consumer<PhoneNumber> updater = invocation.getArgument(1);
                updater.accept(savedEntity);
                return savedEntity;
            }).when(phoneRepository).updateData(eq(id), any());

            // create update request
            PhoneUpdateRequest updatePnRequest = new PhoneUpdateRequest("elysia1",  "01234567891");
            Optional<PhoneDTO> updatedEntity = phoneService.updatePhoneNumber(id, updatePnRequest);

            assertTrue(updatedEntity.isPresent());
            assertEquals(updatePnRequest.name(), updatedEntity.get().name());
            assertEquals(updatePnRequest.number(), updatedEntity.get().number());
            assertEquals(id, updatedEntity.get().id());
            assertEquals(savedEntity.getCreated_at(), updatedEntity.get().created_at());

            // only name
            updatePnRequest = new PhoneUpdateRequest("elysia2", null);
            updatedEntity = phoneService.updatePhoneNumber(id, updatePnRequest);
            assertTrue(updatedEntity.isPresent());
            assertEquals(updatePnRequest.name(), updatedEntity.get().name());
            assertEquals(savedEntity.getNumber(), updatedEntity.get().number());

            // only number
            updatePnRequest = new PhoneUpdateRequest(null, "0240504504055959");
            updatedEntity = phoneService.updatePhoneNumber(id, updatePnRequest);
            assertTrue(updatedEntity.isPresent());
            assertEquals(updatePnRequest.number(), updatedEntity.get().number());
            assertEquals(savedEntity.getName(), updatedEntity.get().name());

            // empty request
            updatePnRequest = new PhoneUpdateRequest(null, null);
            updatedEntity = phoneService.updatePhoneNumber(id, updatePnRequest);
            assertTrue(updatedEntity.isPresent());
            assertEquals(savedEntity.getNumber(), updatedEntity.get().number());
            assertEquals(savedEntity.getName(), updatedEntity.get().name());
            assertEquals(savedEntity.getId(), updatedEntity.get().id());
            assertEquals(savedEntity.getCreated_at(), updatedEntity.get().created_at());
        }
    }

    PhoneNumber constructPhoneNumber(int id, String name, String number) {
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setId(id);
        phoneNumber.setName(name);
        phoneNumber.setNumber(number);
        phoneNumber.setCreated_at(LocalDateTime.now());
        return phoneNumber;
    }
}