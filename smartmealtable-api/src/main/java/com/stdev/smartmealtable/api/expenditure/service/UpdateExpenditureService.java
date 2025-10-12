package com.stdev.smartmealtable.api.expenditure.service;

import com.stdev.smartmealtable.api.expenditure.service.dto.UpdateExpenditureServiceRequest;
import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureItem;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 지출 내역 수정 Application Service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpdateExpenditureService {

    private final ExpenditureRepository expenditureRepository;

    /**
     * 지출 내역 수정
     *
     * @param expenditureId 지출 내역 ID
     * @param memberId      회원 ID
     * @param request       수정 요청 정보
     */
    @Transactional
    public void updateExpenditure(
            Long expenditureId,
            Long memberId,
            UpdateExpenditureServiceRequest request
    ) {
        // 1. 지출 내역 조회
        Expenditure expenditure = expenditureRepository.findByIdAndNotDeleted(expenditureId)
                .orElseThrow(() -> new IllegalArgumentException("지출 내역을 찾을 수 없습니다."));

        // 2. 소유권 검증
        if (!expenditure.isOwnedBy(memberId)) {
            throw new SecurityException("해당 지출 내역에 접근할 권한이 없습니다.");
        }

        // 3. 지출 항목 도메인 객체 생성
        List<ExpenditureItem> items = request.items() != null
                ? request.items().stream()
                .map(item -> ExpenditureItem.create(
                        item.foodId(),
                        item.quantity(),
                        item.price()
                ))
                .collect(Collectors.toList())
                : List.of();

        // 4. 도메인 로직으로 수정 (검증 포함)
        expenditure.update(
                request.storeName(),
                request.amount(),
                request.expendedDate(),
                request.expendedTime(),
                request.categoryId(),
                request.mealType(),
                request.memo(),
                items
        );

        // 5. 저장 (변경 감지를 통해 자동 저장)
        expenditureRepository.save(expenditure);
    }
}
