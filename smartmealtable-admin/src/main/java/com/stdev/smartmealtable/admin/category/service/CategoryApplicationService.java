package com.stdev.smartmealtable.admin.category.service;

import com.stdev.smartmealtable.admin.category.service.dto.*;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryPageResult;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.stdev.smartmealtable.core.error.ErrorType.*;

/**
 * 카테고리 관리 Application Service
 * 
 * <p>트랜잭션 관리와 유즈케이스에 집중합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryApplicationService {

    private final CategoryRepository categoryRepository;

    /**
     * 카테고리 목록 조회 (페이징)
     */
    public CategoryListServiceResponse getCategories(CategoryListServiceRequest request) {
        log.info("[ADMIN] 카테고리 목록 조회 - name: {}, page: {}, size: {}", 
                request.name(), request.page(), request.size());
        
        CategoryPageResult pageResult = categoryRepository.searchByName(
                request.name(), 
                request.page(), 
                request.size()
        );
        
        List<CategoryServiceResponse> categories = pageResult.content().stream()
                .map(CategoryServiceResponse::from)
                .collect(Collectors.toList());
        
        log.info("[ADMIN] 카테고리 목록 조회 완료 - 총 {}개, {}페이지", 
                pageResult.totalElements(), pageResult.totalPages());
        
        return CategoryListServiceResponse.of(
                categories,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages()
        );
    }

    /**
     * 카테고리 상세 조회
     */
    public CategoryServiceResponse getCategory(Long categoryId) {
        log.info("[ADMIN] 카테고리 상세 조회 - categoryId: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
        
        log.info("[ADMIN] 카테고리 상세 조회 완료 - name: {}", category.getName());
        
        return CategoryServiceResponse.from(category);
    }

    /**
     * 카테고리 생성
     */
    @Transactional
    public CategoryServiceResponse createCategory(CreateCategoryServiceRequest request) {
        log.info("[ADMIN] 카테고리 생성 요청 - name: {}", request.name());
        
        // 이름 중복 검증
        if (categoryRepository.existsByName(request.name())) {
            throw new BusinessException(DUPLICATE_CATEGORY_NAME);
        }
        
        Category category = Category.create(request.name());
        Category savedCategory = categoryRepository.save(category);
        
        log.info("[ADMIN] 카테고리 생성 완료 - categoryId: {}, name: {}", 
                savedCategory.getCategoryId(), savedCategory.getName());
        
        return CategoryServiceResponse.from(savedCategory);
    }

    /**
     * 카테고리 수정
     */
    @Transactional
    public CategoryServiceResponse updateCategory(Long categoryId, UpdateCategoryServiceRequest request) {
        log.info("[ADMIN] 카테고리 수정 요청 - categoryId: {}, name: {}", categoryId, request.name());
        
        // 카테고리 존재 여부 확인
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
        
        // 이름 중복 검증 (자신 제외)
        if (categoryRepository.existsByNameAndIdNot(request.name(), categoryId)) {
            throw new BusinessException(DUPLICATE_CATEGORY_NAME);
        }
        
        // 수정
        Category updatedCategory = Category.reconstitute(categoryId, request.name());
        Category savedCategory = categoryRepository.save(updatedCategory);
        
        log.info("[ADMIN] 카테고리 수정 완료 - categoryId: {}, name: {}", 
                savedCategory.getCategoryId(), savedCategory.getName());
        
        return CategoryServiceResponse.from(savedCategory);
    }

    /**
     * 카테고리 삭제 (물리적 삭제)
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("[ADMIN] 카테고리 삭제 요청 - categoryId: {}", categoryId);
        
        // 카테고리 존재 여부 확인
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
        
        // 사용 중인지 확인
        if (categoryRepository.isUsedInStoreOrFood(categoryId)) {
            throw new BusinessException(CATEGORY_IN_USE);
        }
        
        categoryRepository.deleteById(categoryId);
        
        log.info("[ADMIN] 카테고리 삭제 완료 - categoryId: {}, name: {}", 
                categoryId, category.getName());
    }
}
