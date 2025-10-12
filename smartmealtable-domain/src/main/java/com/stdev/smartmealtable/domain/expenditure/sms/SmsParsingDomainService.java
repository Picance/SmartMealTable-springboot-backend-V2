package com.stdev.smartmealtable.domain.expenditure.sms;

import java.util.List;

/**
 * SMS 파싱 도메인 서비스
 * 여러 카드사 파서를 관리하고 적절한 파서를 선택하여 SMS를 파싱합니다.
 */
public class SmsParsingDomainService {

    private final List<SmsParser> parsers;

    public SmsParsingDomainService() {
        this.parsers = List.of(
                new KbCardSmsParser(),
                new NhCardSmsParser(),
                new ShinhanCardSmsParser()
        );
    }

    /**
     * SMS 메시지를 파싱합니다.
     * 등록된 파서 중 처리 가능한 파서를 찾아 파싱을 시도합니다.
     *
     * @param smsContent SMS 메시지 내용
     * @return 파싱 결과
     */
    public ParsedSmsResult parse(String smsContent) {
        if (smsContent == null || smsContent.isBlank()) {
            return ParsedSmsResult.failure();
        }

        for (SmsParser parser : parsers) {
            if (parser.canParse(smsContent)) {
                ParsedSmsResult result = parser.parse(smsContent);
                if (result.isParsed()) {
                    return result;
                }
            }
        }

        // 모든 파서가 실패한 경우
        return ParsedSmsResult.failure();
    }
}
