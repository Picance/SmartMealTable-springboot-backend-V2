package com.stdev.smartmealtable.api.expenditure.service;

import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 지출 내역 삭제 Application Service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeleteExpenditureService {

    private final ExpenditureRepository expenditureRepository;

    /**
     * 지출 내역 삭제 (소프트 삭제)
     *
     * @param expenditureId 지출 내역 ID
     * @param memberId      회원 ID
     */
    @Transactional
    public void deleteExpenditure(Long expenditureId, Long memberId) {
        // 1. 지출 내역 조회
        Expenditure expenditure = expenditureRepository.findByIdAndNotDeleted(expenditureId)
                .orElseThrow(() -> new IllegalArgumentException("지출 내역을 찾을 수 없습니다."));

        // 2. 소유권 검증
        if (!expenditure.isOwnedBy(memberId)) {
            throw new SecurityException("해당 지출 내역에 접근할 권한이 없습니다.");
        }

        // 3. 도메인 로직으로 삭제 (소프트 삭제)
        expenditure.delete();

        // 4. 저장 (변경 감지를 통해 자동 저장)
        expenditureRepository.save(expenditure);
    }
}
