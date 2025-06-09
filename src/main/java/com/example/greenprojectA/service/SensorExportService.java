package com.example.greenprojectA.service;

import com.example.greenprojectA.entity.Sensor;
import com.example.greenprojectA.repository.SensorChartRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorExportService {

    private final SensorChartRepository sensorChartRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ResponseEntity<Resource> exportToExcel(String startDateStr, String endDateStr, String deviceCode) {
        try {
            // 1. 문자열 날짜 파싱
            LocalDateTime startDate = LocalDateTime.parse(startDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime endDate = LocalDateTime.parse(endDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // 2. 데이터 조회
            List<Sensor> sensorList = (deviceCode != null && !deviceCode.isBlank())
                    ? sensorChartRepository.findByDeviceCodeAndMeasureDatetimeBetween(deviceCode, startDate, endDate)
                    : sensorChartRepository.findByMeasureDatetimeBetween(startDate, endDate);

            // 3. 워크북 생성 및 시트 작성
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Sensor Data");

            // 4. 헤더
            String[] headers = {
                    "측정일시", "디바이스코드", "실내온도", "상대습도", "이산화탄소", "VOC",
                    "PM1.0", "PM2.5", "PM10", "온도1", "온도2", "온도3", "소음", "비접촉온도", "조도"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                sheet.setColumnWidth(i, 4000);
            }

            // 5. 데이터 입력
            int rowIdx = 1;
            for (Sensor s : sensorList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.getMeasureDatetime().format(formatter));
                row.createCell(1).setCellValue(s.getDeviceCode());
                row.createCell(2).setCellValue(s.getValue1() != null ? s.getValue1() : 0);
                row.createCell(3).setCellValue(s.getValue2() != null ? s.getValue2() : 0);
                row.createCell(4).setCellValue(s.getValue3() != null ? s.getValue3() : 0);
                row.createCell(5).setCellValue(s.getValue4() != null ? s.getValue4() : 0);
                row.createCell(6).setCellValue(s.getValue5() != null ? s.getValue5() : 0);
                row.createCell(7).setCellValue(s.getValue6() != null ? s.getValue6() : 0);
                row.createCell(8).setCellValue(s.getValue7() != null ? s.getValue7() : 0);
                row.createCell(9).setCellValue(s.getValue8() != null ? s.getValue8() : 0);
                row.createCell(10).setCellValue(s.getValue9() != null ? s.getValue9() : 0);
                row.createCell(11).setCellValue(s.getValue10() != null ? s.getValue10() : 0);
                row.createCell(12).setCellValue(s.getValue11() != null ? s.getValue11() : 0);
                row.createCell(13).setCellValue(s.getValue12() != null ? s.getValue12() : 0);
                row.createCell(14).setCellValue(s.getValue13() != null ? s.getValue13() : 0);
            }

            // 6. 파일 생성
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());

            // 7. 파일 이름 설정 (한글깨짐 방지)
            String filename = "sensor_data_" + startDate.toLocalDate() + "_to_" + endDate.toLocalDate() + ".xlsx";
            String encodedFilename = new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);

            // 8. 응답 반환
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + encodedFilename)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (Exception e) {
            throw new RuntimeException("엑셀 생성 중 오류 발생", e);
        }
    }

    public List<Sensor> getSensorData(String startDateStr, String endDateStr, String deviceCode) {
        LocalDateTime startDate = LocalDateTime.parse(startDateStr);
        LocalDateTime endDate = LocalDateTime.parse(endDateStr);

        if (deviceCode != null && !deviceCode.isBlank()) {
            return sensorChartRepository.findByDeviceCodeAndMeasureDatetimeBetween(deviceCode, startDate, endDate);
        } else {
            return sensorChartRepository.findByMeasureDatetimeBetween(startDate, endDate);
        }
    }
}
