package com.stdev.smartmealtable.admin.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 그룹 데이터 배치 작업 관리 컨트롤러
 * Admin 포털에서 배치 서버의 그룹 데이터 작업을 관리
 */
@Slf4j
@Controller
@RequestMapping("/admin/batch/group")
@RequiredArgsConstructor
public class BatchGroupController {

    private final BatchGroupService batchGroupService;

    /**
     * 그룹 데이터 업로드 폼 페이지
     */
    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        return "batch/group-upload";
    }

    /**
     * JSON 파일을 업로드하여 배치 서버로 전송
     *
     * @param file JSON 파일 (MultipartFile)
     */
    @PostMapping("/upload")
    public String uploadAndImportGroupData(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        // 파일 검증
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "파일이 비어있습니다.");
            return "redirect:/admin/batch/group/upload";
        }

        // JSON 파일 확인
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".json")) {
            redirectAttributes.addFlashAttribute("error", "JSON 파일만 업로드 가능합니다.");
            return "redirect:/admin/batch/group/upload";
        }

        try {
            // 배치 서버로 파일 전송 및 작업 시작
            BatchJobResponse response = batchGroupService.uploadAndStartBatch(file);

            log.info("Group batch job started successfully: {}", response);

            redirectAttributes.addFlashAttribute("success", response.getMessage());
            redirectAttributes.addFlashAttribute("jobId", response.getJobId());
            return "redirect:/admin/batch/group/upload";

        } catch (Exception e) {
            log.error("Failed to upload file and start group batch job", e);
            redirectAttributes.addFlashAttribute("error",
                    "배치 작업 시작 실패: " + e.getMessage());
            return "redirect:/admin/batch/group/upload";
        }
    }
}
