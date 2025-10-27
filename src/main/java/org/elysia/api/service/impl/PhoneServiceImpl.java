package org.elysia.api.service.impl;

import jakarta.transaction.Transactional;
import org.elysia.api.DTO.phone.PhoneCreateRequest;
import org.elysia.api.DTO.phone.PhoneDTO;
import org.elysia.api.DTO.phone.PhoneUpdateRequest;
import org.elysia.api.entity.PhoneNumber;
import org.elysia.api.exception.ResourceNotFoundException;
import org.elysia.api.mapper.PhoneMapper;
import org.elysia.api.repository.PhoneRepository;
import org.elysia.api.service.PhoneService;
import org.elysia.api.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class PhoneServiceImpl implements PhoneService {
    private final PhoneRepository phoneRepository;
    private final PhoneMapper phoneMapper;
    private final CacheManager cacheManager;
    private final RedisService redisService;


    @Autowired
    public PhoneServiceImpl (PhoneRepository phoneRepository, PhoneMapper phoneMapper, CacheManager cacheManager, RedisService redisService) {
        this.phoneRepository = phoneRepository;
        this.phoneMapper = phoneMapper;
        this.cacheManager = cacheManager;
        this.redisService = redisService;
    }

    @Override
    @Cacheable(value = "PhoneNumbersList")
    public List<PhoneDTO> getPhoneNumbers() {
        var result = phoneRepository.getData();
        Stream<PhoneNumber> stream = result.size() > 1000 ? result.parallelStream() : result.stream();
        return stream.map(phoneMapper::toDTO).toList();
    }

    @Override
    @Cacheable(value = "PhoneNumbers", key = "#id")
    public PhoneDTO getPhoneNumber(int id) {
        var result = phoneRepository.getDataOrThrow(id);
        return phoneMapper.toDTO(result);
    }


    @Override
    @Transactional
    @CachePut(value = "PhoneNumbers", key = "#result.id")
    @CacheEvict(value = "PhoneNumbersList", allEntries = true)
    public PhoneDTO savePhoneNumber(PhoneCreateRequest phoneCreateRequest) {
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber(phoneCreateRequest.number());
        phoneNumber.setName(phoneCreateRequest.name());
        var result = phoneRepository.saveData(phoneNumber);
        return phoneMapper.toDTO(result);
    }


    @Override
    @Caching(evict = {
            @CacheEvict(value = "PhoneNumbers", key = "#id"),
            @CacheEvict(value = "PhoneNumbersList", allEntries = true)
    })
    @Transactional
    public void deletePhoneNumber(int id) {
        phoneRepository.deleteDataOrThrow(id);
    }

    @Override
    @Transactional
    public Optional<PhoneDTO> updatePhoneNumber(int id, PhoneUpdateRequest phoneUpdateRequest) {
        PhoneNumber updatedEntity;
        try {
            updatedEntity = phoneRepository.updateData(id, entity -> {
                var number = phoneUpdateRequest.number();
                var name = phoneUpdateRequest.name();

                if ((number!= null) && !number.isEmpty()) {
                    entity.setNumber(number);
                }
                if (name != null && !name.isEmpty()) {
                    entity.setName(name);
                }
            });
        }  catch (ResourceNotFoundException | InvalidDataAccessApiUsageException e) {
            return Optional.empty();
        }

        var updatedEntityDTO = phoneMapper.toDTO(updatedEntity);
        assert updatedEntityDTO != null;
        cacheManager.getCache("PhoneNumbers").put(id, updatedEntityDTO);
        cacheManager.getCache("PhoneNumbersList").clear();
        return Optional.of(updatedEntityDTO);
    }
}

