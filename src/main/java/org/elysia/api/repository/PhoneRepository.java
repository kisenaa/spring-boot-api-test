package org.elysia.api.repository;

import org.elysia.api.entity.PhoneNumber;
import org.springframework.stereotype.Repository;

@Repository("phoneRepository")
public interface PhoneRepository extends BaseRepository<PhoneNumber, Integer> {
}
