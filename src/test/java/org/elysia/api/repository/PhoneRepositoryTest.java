package org.elysia.api.repository;

import jakarta.validation.ConstraintViolationException;
import org.elysia.api.DTO.phone.PhoneDTO;
import org.elysia.api.entity.PhoneNumber;
import org.elysia.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class PhoneRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PhoneRepository phoneRepository;

    @Nested
    class SaveTests {
        @Test
        void testValidSavePhoneNumber() {
            PhoneNumber phone = new PhoneNumber();
            phone.setName("elysia");
            phone.setNumber("08123456789");

            PhoneNumber saved = phoneRepository.save(phone);
            PhoneNumber found = entityManager.find(PhoneNumber.class, saved.getId());
            assertEquals(phone.getName(), found.getName());
            assertEquals(phone.getNumber(), found.getNumber());
            assertEquals(phone.getId(), found.getId());
            // H2 Database precision != java localdatetime precision
            assertTrue(
                    Duration.between(phone.getCreated_at(), found.getCreated_at()).abs().toMillis() < 1
            );
        }

        @Test
        void testInvalidSavePhoneNumber() {
            PhoneNumber phone = new PhoneNumber();

            phone.setName("elysia");
            phone.setNumber("01");
            assertThrows(ConstraintViolationException.class, () -> {
                phoneRepository.save(phone);
                entityManager.flush();
            });
            entityManager.clear();

            phone.setName(null);
            phone.setNumber("0812948593949");
            assertThrows(ConstraintViolationException.class, () -> {
                phoneRepository.save(phone);
                entityManager.flush();
            });
            entityManager.clear();

            phone.setName("elysia");
            phone.setNumber(null);
            assertThrows(ConstraintViolationException.class, () -> {
                phoneRepository.save(phone);
                entityManager.flush();
            });
            entityManager.clear();

            phone.setName("elysia");
            phone.setNumber("0130030300030304040404004040404040404040404");
            assertThrows(ConstraintViolationException.class, () -> {
                phoneRepository.save(phone);
                entityManager.flush();
            });
            entityManager.clear();

            phone.setName("Super Long Nameeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
            phone.setNumber("01204059204004");
            assertThrows(ConstraintViolationException.class, () -> {
                phoneRepository.save(phone);
                entityManager.flush();
            });
            entityManager.clear();
        }

    }

    @Nested
    class ReadTests {
        @Test
        void testGetDataById() {
            PhoneNumber phone = new PhoneNumber();
            phone.setName("elysia");
            phone.setNumber("08987654321");
            PhoneNumber saved = phoneRepository.save(phone);

            Optional<PhoneNumber> retrieved = phoneRepository.getData(saved.getId());
            assertTrue(retrieved.isPresent());
            assertEquals("elysia", retrieved.get().getName());
            assertEquals("08987654321", retrieved.get().getNumber());
        }

        @Test
        void testGetDataByIdNotFound() {
            Optional<PhoneNumber> retrieved = phoneRepository.getData(0);
            assertFalse(retrieved.isPresent());
        }

        @Test
        void testGetListData() {
            PhoneNumber phone = new PhoneNumber();
            phone.setName("elysia");
            phone.setNumber("0898765432");
            PhoneNumber saved = phoneRepository.save(phone);

            List<PhoneNumber> phoneNumbers = phoneRepository.getData();
            assertTrue(!phoneNumbers.isEmpty());
        }

    }

    @Nested
    class UpdateTests {
        @Test
        void testUpdatePhoneNumber() {
            PhoneNumber phone = new PhoneNumber();
            phone.setName("elysia");
            phone.setNumber("08222222222");
            PhoneNumber saved = phoneRepository.save(phone);

            phoneRepository.updateData(saved.getId(), p -> p.setNumber("08333333333"));
            PhoneNumber updated = phoneRepository.getDataOrThrow(saved.getId());
            assertEquals("08333333333", updated.getNumber());

            phoneRepository.updateData(saved.getId(), p -> p.setName("elysia1"));
            updated = phoneRepository.getDataOrThrow(saved.getId());
            assertEquals("elysia1", updated.getName());

            phoneRepository.updateData(saved.getId(), p -> {
                p.setName("elysia2");
                p.setNumber("08222222223");
            } );
            updated = phoneRepository.getDataOrThrow(saved.getId());
            assertEquals("elysia2", updated.getName());
            assertEquals("08222222223", updated.getNumber());

            PhoneNumber existingEntity =  phoneRepository.getDataOrThrow(saved.getId());
            updated = phoneRepository.updateData(saved.getId(), p -> {});
            assertEquals("elysia2", updated.getName());
            assertEquals("08222222223", updated.getNumber());
            assertEquals(existingEntity.getCreated_at(), updated.getCreated_at());

            entityManager.clear();
        }

        @Test
        void testInvalidUpdatePhoneNumber() {
            assertThrows(
                    ResourceNotFoundException.class, () -> phoneRepository.updateData(1, p -> p.setNumber("01"))
            );
            assertThrows(
                    ResourceNotFoundException.class, () -> phoneRepository.updateData(1, p -> p.setNumber("notphonenumber"))
            );
            assertThrows(
                    ResourceNotFoundException.class, () -> phoneRepository.updateData(1, p -> p.setNumber(null))
            );
            assertThrows(
                    ResourceNotFoundException.class, () -> phoneRepository.updateData(1, p -> p.setName(" "))
            );
            assertThrows(
                    ResourceNotFoundException.class, () -> phoneRepository.updateData(1, p -> p.setName(null))
            );

            PhoneNumber phone = new PhoneNumber();
            phone.setName("elysia");
            phone.setNumber("08222222222");
            PhoneNumber saved = phoneRepository.save(phone);

            assertThrows(ConstraintViolationException.class, () -> {
                phoneRepository.updateData(saved.getId(), p -> p.setNumber("020"));
                entityManager.flush();
            });
            assertThrows(ConstraintViolationException.class, () -> {
                phoneRepository.updateData(saved.getId(), p -> p.setNumber(null));
                entityManager.flush();
            });
            assertThrows(ConstraintViolationException.class, () -> {
                phoneRepository.updateData(saved.getId(), p -> p.setNumber("08333333333083333333330833333333308333333333"));
                entityManager.flush();
            });
            assertThrows(ConstraintViolationException.class, () -> {
                phoneRepository.updateData(saved.getId(), p -> p.setName(null));
                entityManager.flush();
            });

            assertThrows(ConstraintViolationException.class, () -> {
                phoneRepository.updateData(saved.getId(), p -> p.setName(" "));
                entityManager.flush();
            });
            assertThrows(ConstraintViolationException.class, () -> {
                phoneRepository.updateData(saved.getId(), p -> p.setName("null"));
                entityManager.flush();
            });
            assertThrows(ConstraintViolationException.class, () -> {
                phoneRepository.updateData(saved.getId(), p -> p.setName("super long nameeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"));
                entityManager.flush();
            });

        }
    }

    @Nested
    class DeleteTests {
        @Test
        void testDeletePhoneNumber() {
            PhoneNumber phone = new PhoneNumber();
            phone.setName("elysia");
            phone.setNumber("08444444444");
            PhoneNumber saved = phoneRepository.save(phone);

            phoneRepository.deleteDataOrThrow(saved.getId());
            var optional = phoneRepository.getData(saved.getId());
            assertFalse(optional.isPresent());
        }

        @Test
        void testDeletePhoneNumberNotFound() {
            assertThrows(ResourceNotFoundException.class, () -> phoneRepository.deleteDataOrThrow(99999999));

        }
    }

}