package com.stdev.smartmealtable.domain.expenditure.sms;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * KB국민카드 SMS 파서
 * 예시: [KB국민카드] 10/08 12:30 승인 13,500원 일시불 맘스터치강남점
 */
public class KbCardSmsParser implements SmsParser {

    private static final Pattern KB_PATTERN = Pattern.compile(
            // 1: MM/dd, 2: HH:mm, 3: amount, 4: trade name
            "\\[KB국민카드]\\s*(\\d{2}/\\d{2})\\s*(\\d{2}:\\d{2})\\s*승인\\s*([\\d,]+)원\\s*[가-힣A-Za-z]*\\s*(.+)"
    );

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    @Override
    public boolean canParse(String smsContent) {
        return smsContent != null && smsContent.contains("KB국민카드");
    }

    @Override
    public ParsedSmsResult parse(String smsContent) {
        if (smsContent == null || smsContent.isBlank()) {
            return ParsedSmsResult.failure();
        }

        Matcher matcher = KB_PATTERN.matcher(smsContent);
        if (!matcher.find()) {
            return ParsedSmsResult.failure();
        }

        try {
            String datePart = matcher.group(1);
            String timePart = matcher.group(2);
            String amountPart = matcher.group(3).replace(",", ""); // "13,500" → "13500"
            String storeName = matcher.group(4).trim();

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
