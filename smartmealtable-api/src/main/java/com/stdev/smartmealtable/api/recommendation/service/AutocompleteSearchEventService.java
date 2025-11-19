package com.stdev.smartmealtable.api.recommendation.service;

import com.stdev.smartmealtable.domain.search.SearchKeywordEvent;
import com.stdev.smartmealtable.domain.search.SearchKeywordEventRepository;
import com.stdev.smartmealtable.storage.db.search.SearchKeywordSupport;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;

/**
 * 자동완성 검색 이벤트 로깅 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AutocompleteSearchEventService {

    private final SearchKeywordEventRepository searchKeywordEventRepository;

    @Async("searchKeywordEventExecutor")
    public void logSearchEvent(AutocompleteSearchEventCommand command) {
        Assert.notNull(command, "command must not be null");

        try {
            String normalizedKeyword = normalizeKeyword(command.rawKeyword());

            SearchKeywordEvent event = SearchKeywordEvent.of(
                    command.memberId(),
                    command.rawKeyword(),
                    normalizedKeyword,
                    command.clickedFoodId(),
                    command.latitude(),
                    command.longitude()
            );

            searchKeywordEventRepository.save(event);
            log.debug("자동완성 검색 이벤트 저장 완료 - keyword: {}", normalizedKeyword);
        } catch (Exception e) {
            log.warn("자동완성 검색 이벤트 저장 실패 - keyword: {}, reason: {}", command.rawKeyword(), e.getMessage());
        }
    }

    private String normalizeKeyword(String keyword) {
        String normalized = SearchKeywordSupport.normalize(keyword);
        return normalized.isEmpty() ? keyword.trim().toLowerCase() : normalized;
    }

    @Builder
    public record AutocompleteSearchEventCommand(
            String rawKeyword,
            Long memberId,
            Long clickedFoodId,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        public AutocompleteSearchEventCommand {
            Assert.hasText(rawKeyword, "rawKeyword must not be blank");
        }
    }
}
