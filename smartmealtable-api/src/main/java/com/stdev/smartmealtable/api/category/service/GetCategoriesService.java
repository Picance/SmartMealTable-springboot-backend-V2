package com.stdev.smartmealtable.api.category.service;

import com.stdev.smartmealtable.api.category.service.dto.GetCategoriesServiceResponse;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 카테고리 조회 Application Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetCategoriesService {

    private final CategoryRepository categoryRepository;

    /**
     * 모든 카테고리 목록 조회
     *
     * @return 카테고리 목록
     */
    public GetCategoriesServiceResponse getCategories() {
        log.info("카테고리 목록 조회 서비스 호출");

        List<Category> categories = categoryRepository.findAll();

        List<GetCategoriesServiceResponse.CategoryInfo> categoryInfos = categories.stream()
                .map(GetCategoriesServiceResponse.CategoryInfo::from)
                .collect(Collectors.toList());

        return new GetCategoriesServiceResponse(categoryInfos);
    }
}
