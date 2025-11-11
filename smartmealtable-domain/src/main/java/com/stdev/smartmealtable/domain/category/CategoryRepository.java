package com.stdev.smartmealtable.domain.category;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 Repository 인터페이스 (순수 Java 인터페이스)
 * 
 * <p>도메인 모듈은 POJO를 유지하기 위해 Spring Data 의존성을 사용하지 않습니다.</p>
 */
public interface CategoryRepository {

    /**
     * 카테고리 저장
     */
    Category save(Category category);

    /**
     * 카테고리 ID로 조회
     */
    Optional<Category> findById(Long categoryId);

    /**
     * 모든 카테고리 조회
     */
    List<Category> findAll();

    /**
     * 여러 카테고리 ID로 조회
     */
    List<Category> findByIdIn(List<Long> categoryIds);

    /**
     * 카테고리 이름으로 검색 (페이징)
     * 
     * @param name 검색할 이름 (부분 일치)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 페이징된 카테고리 결과
     */
    CategoryPageResult searchByName(String name, int page, int size);

    /**
     * 카테고리 이름으로 정확히 조회
     */
    Optional<Category> findByName(String name);

    /**
     * 카테고리 이름 중복 여부 확인
     */
    boolean existsByName(String name);

    /**
     * 특정 ID를 제외한 이름 중복 여부 확인 (수정 시 사용)
     */
    boolean existsByNameAndIdNot(String name, Long categoryId);

    /**
     * 카테고리 삭제
     */
    void deleteById(Long categoryId);

    /**
     * 카테고리가 음식점이나 음식에서 사용 중인지 확인
     */
    boolean isUsedInStoreOrFood(Long categoryId);

    /**
     * 카테고리 이름에 keyword가 포함되는 카테고리 ID 조회 (자동완성용)
     *
     * @param keyword 검색 키워드
     * @param limit 결과 제한 수
     * @return 카테고리 ID 리스트
     */
    List<Long> findByNameContains(String keyword, int limit);

    /**
     * 주어진 카테고리 ID 목록에 속하는 가게 ID 조회 (자동완성용)
     *
     * @param categoryIds 카테고리 ID 리스트
     * @param limit 결과 제한 수
     * @return 가게 ID 리스트
     */
    List<Long> findStoreIdsByCategories(List<Long> categoryIds, int limit);
}
