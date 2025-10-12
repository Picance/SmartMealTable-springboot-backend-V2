package com.stdev.smartmealtable.domain.expenditure.sms;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 신한카드 SMS 파서
 * 예시: 신한카드 승인 13,500원 10/08 12:30 맘스터치강남점
 */
public class ShinhanCardSmsParser implements SmsParser {

    private static final Pattern SH_PATTERN = Pattern.compile(
            "신한카드.*?승인.*?([\\d,]+)원(?:\\([^)]*\\))?\\s*(\\d{2}/\\d{2})\\s*(\\d{2}:\\d{2})\\s+(.+?)(?:\\s+(?:누적|잔여).*)?$"
    );

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    @Override
    public boolean canParse(String smsContent) {
        return smsContent != null && smsContent.contains("신한카드");
    }

    @Override
    public ParsedSmsResult parse(String smsContent) {
        if (smsContent == null || smsContent.isBlank()) {
            return ParsedSmsResult.failure();
        }

        Matcher matcher = SH_PATTERN.matcher(smsContent);
        if (!matcher.find()) {
            return ParsedSmsResult.failure();
        }

        try {
            String amountPart = matcher.group(1).replace(",", "");
            String datePart = matcher.group(2);
            String timePart = matcher.group(3);
            String storeName = matcher.group(4).trim()
                    .replaceAll("\\s*(누적|잔여|잔액).*", "")
                    .trim();

            int currentYear = LocalDate.now().getYear();
            LocalDateTime dateTime = LocalDateTime.parse(
                    currentYear + "/" + datePart + " " + timePart, FORMATTER
            );

            long amount = Long.parseLong(amountPart);

            return ParsedSmsResult.success(
                    storeName,
                    amount,
                    dateTime.toLocalDate(),
                    dateTime.toLocalTime()
            );
        } catch (Exception e) {
            return ParsedSmsResult.failure();
        }
    }
}
