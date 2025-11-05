package com.stdev.smartmealtable.storage.db.category;

import com.stdev.smartmealtable.domain.category.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CategoryRepositoryImplTest {

    @Mock
    private CategoryJpaRepository jpaRepository;

    @Mock
    private com.querydsl.jpa.impl.JPAQueryFactory queryFactory;

    private CategoryRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new CategoryRepositoryImpl(jpaRepository, queryFactory);
    }

    @Test
    void save_and_findById_and_findAll_and_findByIdIn() {
        Category c = Category.reconstitute(null, "Food");
        when(jpaRepository.save(any())).thenAnswer(invocation -> CategoryJpaEntity.fromDomain(Category.reconstitute(7L, "Food")));
        when(jpaRepository.findById(7L)).thenReturn(Optional.of(CategoryJpaEntity.fromDomain(Category.reconstitute(7L, "Food"))));
        when(jpaRepository.findAll()).thenReturn(List.of(CategoryJpaEntity.fromDomain(Category.reconstitute(7L, "Food"))));
        when(jpaRepository.findAllById(List.of(7L))).thenReturn(List.of(CategoryJpaEntity.fromDomain(Category.reconstitute(7L, "Food"))));

        Category saved = repository.save(c);
        assertThat(saved.getCategoryId()).isEqualTo(7L);

        Optional<Category> found = repository.findById(7L);
        assertThat(found).isPresent();

        List<Category> all = repository.findAll();
        assertThat(all).hasSize(1);

        List<Category> byIds = repository.findByIdIn(List.of(7L));
        assertThat(byIds).hasSize(1);
    }
}
