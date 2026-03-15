package com.competition.competition.controller;

import com.competition.competition.common.Result;
import com.competition.competition.common.ResultCode;
import com.competition.competition.dto.*;
import com.competition.competition.service.RecognitionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 上传识别、识别记录、个体列表与详细报告。
 */
@RestController
@RequestMapping("/api/recognition")
@RequiredArgsConstructor
public class RecognitionController {

    private final RecognitionService recognitionService;

    /** 上传识别：type=human|non_human，可选；操作者从登录态取 */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<RecognitionResultDto> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "human") String type,
            HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.fail(ResultCode.BAD_REQUEST, "请选择图片文件");
        }
        Long operatorId = (Long) request.getAttribute("currentUserId");
        RecognitionResultDto dto = recognitionService.submit(file, type, operatorId);
        return Result.ok(dto);
    }

    @GetMapping("/result/{taskId}")
    public Result<RecognitionResultDto> result(@PathVariable String taskId) {
        Optional<RecognitionResultDto> opt = recognitionService.getResult(taskId);
        return opt.map(Result::ok).orElseGet(() -> Result.fail(ResultCode.NOT_FOUND, "任务不存在"));
    }

    /** 识别记录列表：按识别时间、识别编号、类型筛选 */
    @GetMapping("/records")
    public Result<List<RecordListDto>> records(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Long recordId) {
        List<RecordListDto> list = recognitionService.listRecords(type, startTime, endTime, recordId);
        return Result.ok(list);
    }

    /** 单条识别详细报告（弹窗/下载） */
    @GetMapping("/records/{id}/report")
    public Result<RecordReportDto> recordReport(@PathVariable Long id) {
        return recognitionService.getRecordReport(id)
                .map(Result::ok)
                .orElseGet(() -> Result.fail(ResultCode.NOT_FOUND, "记录不存在"));
    }

    /** 同一物种个体列表：id + 封面图 + 详细报告链接用 individualId */
    @GetMapping("/individuals")
    public Result<List<IndividualListDto>> individuals(@RequestParam(value = "type", defaultValue = "human") String type) {
        List<IndividualListDto> list = recognitionService.listIndividuals(type);
        return Result.ok(list);
    }

    /** 个体详细报告：按时间排序的全部图片 */
    @GetMapping("/individuals/{individualId}/report")
    public Result<IndividualReportDto> individualReport(@PathVariable Long individualId) {
        return recognitionService.getIndividualReport(individualId)
                .map(Result::ok)
                .orElseGet(() -> Result.fail(ResultCode.NOT_FOUND, "个体不存在"));
    }
}
