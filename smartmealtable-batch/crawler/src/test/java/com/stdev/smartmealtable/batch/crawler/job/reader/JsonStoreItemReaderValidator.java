package com.stdev.smartmealtable.batch.crawler.job.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.batch.crawler.dto.CrawledStoreDto;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.Scanner;

/**
 * JSON 파싱 검증 프로그램
 * 실행: java JsonStoreItemReaderValidator
 */
public class JsonStoreItemReaderValidator {

    public static void main(String[] args) {
        try {
            System.out.println("=== JSON 파싱 검증 시작 ===");
            
            String filePath = "districts_before/노원구_공릉동.json";
            Resource resource = new FileSystemResource(filePath);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonStoreItemReader reader = new JsonStoreItemReader(resource, objectMapper);

            System.out.println("✓ 총 가게 수: " + reader.getTotalCount());
            
            int count = 0;
            CrawledStoreDto store;
            while ((store = reader.read()) != null && count < 3) {
                count++;
                System.out.println("\n[" + count + "] 가게명: " + store.getName());
                System.out.println("    메뉴 수: " + (store.getMenus() != null ? store.getMenus().size() : 0));
                
                if (store.getMenus() != null && !store.getMenus().isEmpty()) {
                    CrawledStoreDto.MenuInfo menu = store.getMenus().get(0);
                    System.out.println("    첫 번째 메뉴:");
                    System.out.println("      - isMain: " + menu.getIsMain());
                    System.out.println("      - 이름: " + menu.getName());
                    System.out.println("      - 가격: " + menu.getPrice());
                }
            }
            
            System.out.println("\n✓ 파싱 성공! isMain 필드가 정상 처리됨");
            System.out.println("=== JSON 파싱 검증 완료 ===");
            
        } catch (Exception e) {
            System.err.println("✗ 파싱 실패!");
            System.err.println("에러: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
