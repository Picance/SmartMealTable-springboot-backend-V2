package com.stdev.smartmealtable.admin.storeimage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.admin.common.AbstractAdminContainerTest;
import com.stdev.smartmealtable.admin.config.AdminTestConfiguration;
import com.stdev.smartmealtable.admin.storeimage.controller.request.CreateStoreImageRequest;
import com.stdev.smartmealtable.admin.storeimage.controller.request.UpdateStoreImageRequest;
import com.stdev.smartmealtable.domain.category.Category;
import com.stdev.smartmealtable.domain.category.CategoryRepository;
import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreImage;
import com.stdev.smartmealtable.domain.store.StoreImageRepository;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 가게 이미지 관리 API 통합 테스트 (ADMIN)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AdminTestConfiguration.class)
class StoreImageControllerTest extends AbstractAdminContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreImageRepository storeImageRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testStoreId;
    private Long testCategoryId;

    @BeforeEach
    void setUp() {
        // 카테고리 생성
        Category category = Category.create("한식");
        Category savedCategory = categoryRepository.save(category);
        testCategoryId = savedCategory.getCategoryId();

        // 테스트 가게 생성
        Store store = Store.create(
                "테스트 음식점",
                testCategoryId,
                "서울시 강남구 테헤란로 123",
                "서울시 강남구 역삼동 456",
                new BigDecimal("37.4979"),
                new BigDecimal("127.0276"),
                "02-1234-5678",
                "맛있는 테스트 음식점",
                15000,
                0,
                0,
                0,
                StoreType.RESTAURANT
        );
        Store savedStore = storeRepository.save(store);
        testStoreId = savedStore.getStoreId();

        entityManager.flush();
        entityManager.clear();
    }

    // ==================== 이미지 추가 테스트 ====================

    @Test
    @DisplayName("[성공] 가게 이미지 추가 - 첫 번째 이미지 (자동 대표 이미지)")
    void createStoreImage_FirstImage_AutoMain() throws Exception {
        // Given
        CreateStoreImageRequest request = new CreateStoreImageRequest(
                "https://example.com/store1.jpg",
                false, // isMain을 false로 설정해도
                1
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/stores/{storeId}/images", testStoreId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.imageUrl").value("https://example.com/store1.jpg"))
                .andExpect(jsonPath("$.data.isMain").value(true)) // 첫 이미지는 자동으로 대표 이미지
                .andExpect(jsonPath("$.data.displayOrder").value(1))
                .andExpect(jsonPath("$.data.storeImageId").isNotEmpty());
    }

    @Test
    @DisplayName("[성공] 가게 이미지 추가 - 대표 이미지 지정")
    void createStoreImage_WithMainFlag_Success() throws Exception {
        // Given
        CreateStoreImageRequest request = new CreateStoreImageRequest(
                "https://example.com/store-main.jpg",
                true, // 대표 이미지로 지정
                1
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/stores/{storeId}/images", testStoreId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.isMain").value(true));
    }

    @Test
    @DisplayName("[성공] 가게 이미지 추가 - 여러 이미지")
    void createStoreImage_MultipleImages_Success() throws Exception {
        // Given - 첫 번째 이미지 (대표)
        CreateStoreImageRequest request1 = new CreateStoreImageRequest(
                "https://example.com/store1.jpg",
                true,
                1
        );
        mockMvc.perform(post("/api/v1/admin/stores/{storeId}/images", testStoreId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request1)))
                .andExpect(status().isCreated());

        // When - 두 번째 이미지 (일반)
        CreateStoreImageRequest request2 = new CreateStoreImageRequest(
                "https://example.com/store2.jpg",
                false,
                2
        );

        // Then
        mockMvc.perform(post("/api/v1/admin/stores/{storeId}/images", testStoreId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request2)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.isMain").value(false))
                .andExpect(jsonPath("$.data.displayOrder").value(2));
    }

    @Test
    @DisplayName("[실패] 가게 이미지 추가 - 존재하지 않는 가게")
    void createStoreImage_StoreNotFound() throws Exception {
        // Given
        CreateStoreImageRequest request = new CreateStoreImageRequest(
                "https://example.com/store.jpg",
                false,
                1
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/stores/{storeId}/images", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"));
    }

    @Test
    @DisplayName("[실패] 가게 이미지 추가 - 이미지 URL 누락")
    void createStoreImage_MissingImageUrl() throws Exception {
        // Given
        CreateStoreImageRequest request = new CreateStoreImageRequest(
                null, // imageUrl 누락
                false,
                1
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/stores/{storeId}/images", testStoreId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.result").value("ERROR"));
    }

    // ==================== 이미지 수정 테스트 ====================

    @Test
    @DisplayName("[성공] 가게 이미지 수정 - 일반 정보 변경")
    void updateStoreImage_Success() throws Exception {
        // Given - 이미지 생성
        StoreImage image = StoreImage.create(
                testStoreId,
                "https://example.com/old.jpg",
                true,
                1
        );
        StoreImage savedImage = storeImageRepository.save(image);
        entityManager.flush();
        entityManager.clear();

        // When - 이미지 수정
        UpdateStoreImageRequest request = new UpdateStoreImageRequest(
                "https://example.com/new.jpg",
                true,
                1
        );

        // Then
        mockMvc.perform(put("/api/v1/admin/stores/{storeId}/images/{imageId}", testStoreId, savedImage.getStoreImageId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.imageUrl").value("https://example.com/new.jpg"));
    }

    @Test
    @DisplayName("[성공] 가게 이미지 수정 - 대표 이미지 전환")
    void updateStoreImage_ChangeMainImage_Success() throws Exception {
        // Given - 두 개의 이미지 생성
        StoreImage image1 = storeImageRepository.save(
                StoreImage.create(testStoreId, "https://example.com/img1.jpg", true, 1)
        );
        StoreImage image2 = storeImageRepository.save(
                StoreImage.create(testStoreId, "https://example.com/img2.jpg", false, 2)
        );
        entityManager.flush();
        entityManager.clear();

        // When - image2를 대표 이미지로 변경
        UpdateStoreImageRequest request = new UpdateStoreImageRequest(
                "https://example.com/img2.jpg",
                true, // 대표 이미지로 변경
                2
        );

        // Then
        mockMvc.perform(put("/api/v1/admin/stores/{storeId}/images/{imageId}", testStoreId, image2.getStoreImageId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.isMain").value(true));

        // 기존 대표 이미지가 자동으로 false로 변경되었는지 확인
        entityManager.flush();
        entityManager.clear();
        
        StoreImage updatedImage1 = storeImageRepository.findById(image1.getStoreImageId());
        assertThat(updatedImage1.isMain()).isFalse();
    }

    @Test
    @DisplayName("[실패] 가게 이미지 수정 - 존재하지 않는 이미지")
    void updateStoreImage_ImageNotFound() throws Exception {
        // Given
        UpdateStoreImageRequest request = new UpdateStoreImageRequest(
                "https://example.com/new.jpg",
                true,
                1
        );

        // When & Then
        mockMvc.perform(put("/api/v1/admin/stores/{storeId}/images/{imageId}", testStoreId, 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"));
    }

    // ==================== 이미지 삭제 테스트 ====================

    @Test
    @DisplayName("[성공] 가게 이미지 삭제")
    void deleteStoreImage_Success() throws Exception {
        // Given
        StoreImage image = storeImageRepository.save(
                StoreImage.create(testStoreId, "https://example.com/img.jpg", false, 1)
        );
        entityManager.flush();
        entityManager.clear();

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/stores/{storeId}/images/{imageId}", testStoreId, image.getStoreImageId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // 삭제 확인
        StoreImage deletedImage = storeImageRepository.findById(image.getStoreImageId());
        assertThat(deletedImage).isNull();
    }

    @Test
    @DisplayName("[성공] 가게 이미지 삭제 - 대표 이미지 삭제 시 다음 이미지가 대표로")
    void deleteStoreImage_MainImage_NextBecomesMain() throws Exception {
        // Given - 여러 이미지 생성
        StoreImage mainImage = storeImageRepository.save(
                StoreImage.create(testStoreId, "https://example.com/main.jpg", true, 1)
        );
        StoreImage image2 = storeImageRepository.save(
                StoreImage.create(testStoreId, "https://example.com/img2.jpg", false, 2)
        );
        StoreImage image3 = storeImageRepository.save(
                StoreImage.create(testStoreId, "https://example.com/img3.jpg", false, 3)
        );
        entityManager.flush();
        entityManager.clear();

        // When - 대표 이미지 삭제
        mockMvc.perform(delete("/api/v1/admin/stores/{storeId}/images/{imageId}", testStoreId, mainImage.getStoreImageId()))
                .andExpect(status().isNoContent());

        // Then - 다음 이미지(displayOrder가 가장 작은)가 대표 이미지로 변경
        entityManager.flush();
        entityManager.clear();
        
        List<StoreImage> remainingImages = storeImageRepository.findByStoreId(testStoreId);
        assertThat(remainingImages).hasSize(2);
        
        StoreImage newMainImage = remainingImages.stream()
                .filter(img -> img.getDisplayOrder() != null)
                .min((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                .orElseThrow();
        
        assertThat(newMainImage.isMain()).isTrue();
    }

    @Test
    @DisplayName("[실패] 가게 이미지 삭제 - 존재하지 않는 이미지")
    void deleteStoreImage_ImageNotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/admin/stores/{storeId}/images/{imageId}", testStoreId, 999999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"));
    }

    // ==================== Helper Methods ====================

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
