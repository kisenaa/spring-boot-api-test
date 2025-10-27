package org.elysia.api.repository;

import org.elysia.api.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    default List<T> getData() {
        return this.findAll();
    }

    default Optional<T> getData(ID id) {
        return findById(id);
    }

    default T getDataOrThrow(ID id) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("Entity %s not found".formatted(id.toString())));
    }

    default T saveData(T entity) {
        return this.save(entity);
    }

    default void deleteData(T entity) {
        this.delete(entity);
    }

    default void deleteDataById(ID id) {
        this.deleteById(id);
    }

    default void deleteDataOrThrow(ID id) {
        T entityResult = this.getDataOrThrow(id);
        this.delete(entityResult);
    }

    default T updateData(ID id, Consumer<T> updater) {
        T entity = getDataOrThrow(id);
        updater.accept(entity);
        return this.save(entity);
    }
}
