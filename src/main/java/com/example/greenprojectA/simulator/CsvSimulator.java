package com.example.greenprojectA.simulator;

import com.example.greenprojectA.entity.Device;
import com.example.greenprojectA.entity.EventLog;
import com.example.greenprojectA.entity.Sensor;
import com.example.greenprojectA.entity.SensorThreshold;
import com.example.greenprojectA.repository.DeviceRepository;
import com.example.greenprojectA.repository.EventLogRepository;
import com.example.greenprojectA.repository.SensorRepository;
import com.example.greenprojectA.repository.ThresholdRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CsvSimulator {

    private final SensorRepository sensorRepository;

    private final ThresholdRepository thresholdRepository;

    private final EventLogRepository eventLogRepository;

    private final DeviceRepository deviceRepository;

    // 현재 읽고 있는 CSV 라인 인덱스
    private int currentIndex = 0;

    // 전체 CSV 데이터를 메모리에 로딩해두는 리스트
    private List<String> lines = new ArrayList<>();

    // 애플리케이션 시작 시 CSV를 메모리에 로드
    @PostConstruct
    public void initCsv() {
        try {
            String realPath = ResourceUtils.getFile("classpath:static/csv/sensor_data_sample.csv").getAbsolutePath();
            BufferedReader br = new BufferedReader(new FileReader(realPath));

            // 첫 줄은 헤더이므로 스킵
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            System.out.println("CSV 파일 로딩 완료. 총 라인 수: " + lines.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

//         장치 등록
//        String[] locations = {"1F", "2F", "자재실", "포장실"};
//
//        for (int i = 1; i <= 4; i++) {
//            Device device = new Device();
//            device.setDeviceCode("ENV_V2_" + i);
//            device.setDeviceLocation(locations[i - 1]);
//            device.setCompanyId(1000003);
//            deviceRepository.save(device);
//        }
    }


    // 10초마다 측정 시간이 같은 데이터(1분 단위)를 묶어서 한 번에 DB에 저장
    @Scheduled(fixedDelay = 10000) // 10초마다 실행
    public void insertByMinuteGroup() {
        if (currentIndex >= lines.size()) {
            System.out.println("모든 시뮬레이션 데이터 삽입 완료");
            return;
        }

        List<Sensor> sensorList = new ArrayList<>();
        List<EventLog> eventLogList = new ArrayList<>();

        // 기준 측정 시간(measure_datetime)을 현재 줄에서 가져오기
        String[] firstLineArr = lines.get(currentIndex).split(",");
        String currentTimeStr = firstLineArr[3].replaceAll("\"", "").trim();
        LocalDateTime currentMeasureTime = LocalDateTime.parse(currentTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 동일한 측정 시간인 데이터들을 한 번에 모아서 저장
        while (currentIndex < lines.size()) {
            String[] arr = lines.get(currentIndex).split(",");
            LocalDateTime measureTime = LocalDateTime.parse(arr[3].replaceAll("\"", "").trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 측정 시간이 다르면 루프 중단
            if (!measureTime.equals(currentMeasureTime)) {
                break;
            }

            Sensor sensor = arrToSensor(arr);
            sensorList.add(sensor);

            // 장치코드 + companyId로 각 value별 임계값 비교
            for (int i = 1; i <= 10; i++) {
                Double value = getSensorValue(sensor, i);
                // 냉장고 온도(value 8~10)는 특수 예외 처리
                if ((i >= 8 && i <= 10) && (value == -242 || value == 988.7)) {
                    continue;
                }

                if (value != null) {
                    // 최신 임계값 가져오기
                    SensorThreshold threshold = thresholdRepository.findTopByCompanyIdAndDeviceCodeAndValueNoOrderByCreatedAtDesc(sensor.getCompanyId(), sensor.getDeviceCode(), i);

                    if (threshold != null) {
                        if (value < threshold.getMinValue() || value > threshold.getMaxValue()) {
                            // 이벤트 로그 생성
                            String description = "";
                            if (value < threshold.getMinValue()) {
                                description = "하한 임계값 미만";
                            } else if(value > threshold.getMaxValue()) {
                                description = "상한 임계값 초과";
                            }

                            EventLog eventLog = new EventLog();
                            eventLog.setCompanyId(sensor.getCompanyId());
                            eventLog.setDeviceCode(sensor.getDeviceCode());
                            eventLog.setValueNo(i);
                            eventLog.setMeasuredValue(value);
                            eventLog.setMinValue(threshold.getMinValue());
                            eventLog.setMaxValue(threshold.getMaxValue());
                            eventLog.setMeasureDatetime(sensor.getMeasureDatetime());
                            eventLog.setEventDatetime(LocalDateTime.now()); // 이벤트 발생 시각
                            eventLog.setDescription(description); // 이벤트 설명
                            eventLogList.add(eventLog);
                        }
                    }
                }
            }
            currentIndex++;
        }

        // DB에 한 번에 저장
        sensorRepository.saveAll(sensorList);

        // 임계값 초과 이벤트 로그 저장
        if (!eventLogList.isEmpty()) {
            eventLogRepository.saveAll(eventLogList);
        }

        System.out.println(currentMeasureTime + " 데이터 " + sensorList.size() + "건 저장 완료 (이벤트: " + eventLogList.size() + ")");
    }


    // CSV 한 줄 데이터를 Sensor 객체로 변환
    private Sensor arrToSensor(String[] arr) {
        int k = 1; // 0번 인덱스(sensor_id)는 무시
        Sensor sensor = new Sensor();

        sensor.setCompanyId(safeIntParse(arr[k++]));     // 회사 ID
        sensor.setDeviceCode(arr[k++].replaceAll("\"", "").trim());   // 장비 코드
        sensor.setMeasureDatetime(LocalDateTime.parse(arr[k++].replaceAll("\"", "").trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))); // 측정일시


        sensor.setValue1(safeParse(arr[k++]));  // 실내온도
        sensor.setValue2(safeParse(arr[k++]));  // 상대습도
        sensor.setValue3(safeParse(arr[k++]));  // 이산화탄소
        sensor.setValue4(safeParse(arr[k++]));  // 유기화합물VOC
        sensor.setValue5(safeParse(arr[k++]));  // 초미세먼지 PM1.0
        sensor.setValue6(safeParse(arr[k++]));  // 초미세먼지 PM2.5
        sensor.setValue7(safeParse(arr[k++]));  // 초미세먼지 PM10
        sensor.setValue8(safeParse(arr[k++]));  // 온도_1
        sensor.setValue9(safeParse(arr[k++]));  // 온도_2
        sensor.setValue10(safeParse(arr[k++])); // 온도_3
        sensor.setValue11(safeParse(arr[k++])); // 소음
        sensor.setValue12(safeParse(arr[k++])); // 온도(비접촉)
        sensor.setValue13(safeParse(arr[k++])); // 조도

        return sensor;
    }

    // 문자열을 Double로 변환할 때, 따옴표 처리
    private Double safeParse(String value) {
        if (value == null || value.isBlank()) return null;
        value = value.trim().replaceAll("\"", "");
        return Double.parseDouble(value);
    }

    // 문자열을 Integer로 변환할 때, 따옴표 처리
    private Integer safeIntParse(String value) {
        if (value == null || value.isBlank()) return null;
        value = value.trim().replaceAll("\"", "");
        return Integer.parseInt(value);
    }

   // @PostConstruct
    public void initThresholds() {
        // 예) 임계값이 이미 존재하는지 확인
        if (thresholdRepository.count() == 0) {
            List<SensorThreshold> SensorThresholdList = new ArrayList<>();

            String[] devices = {"ENV_V2_1", "ENV_V2_2", "ENV_V2_3", "ENV_V2_4"};

            for (String device : devices) {
                for (int i = 1; i <= 13; i++) {
                    SensorThreshold sensorThreshold = new SensorThreshold();
                    sensorThreshold.setCompanyId(1000003);
                    sensorThreshold.setDeviceCode(device);
                    sensorThreshold.setValueNo(i);
                    sensorThreshold.setMinValue(defaultMinForValue(i)); // 기본 최소 임계값 설정
                    sensorThreshold.setMaxValue(defaultMaxForValue(i)); // 기본 최대 임계값 설정
                    sensorThreshold.setCreatedAt(LocalDateTime.now());
                    sensorThreshold.setCreatedBy("system"); // 생성자 정보 설정
                    SensorThresholdList.add(sensorThreshold);
                }
            }
            thresholdRepository.saveAll(SensorThresholdList);
        }
    }

    private Double defaultMinForValue(int valueNumber) {
        // valueNumber에 따른 기본 최소 임계값 반환
        switch (valueNumber) {
            case 1: return 10.0; // 실내온도 최소값
            case 2: return 28.0; // 상대습도 최소값
            case 3: return 400.0; // 이산화탄소 최소값
            case 4: return 0.0; // 유기화합물VOC 최소값
            case 5: return 0.0; // 초미세먼지 PM1.0 최소값
            case 6: return 0.0; // 초미세먼지 PM2.5 최소값
            case 7: return 0.0; // 초미세먼지 PM10 최소값
            case 8: return 8.0; // 온도_1 최소값
            case 9: return 8.0; // 온도_2 최소값
            case 10: return 8.0; // 온도_3 최소값
            default: return 0.0;
        }
    }

    private Double defaultMaxForValue(int valueNumber) {
        // valueNumber에 따른 기본 최대 임계값 반환
        switch (valueNumber) {
            case 1: return 28.0; // 실내온도 최대값
            case 2: return 60.0; // 상대습도 최대값
            case 3: return 1000.0; // 이산화탄소 최대값
            case 4: return 500.0; // 유기화합물VOC 최대값
            case 5: return 35.0; // 초미세먼지 PM1.0 최대값
            case 6: return 35.0; // 초미세먼지 PM2.5 최대값
            case 7: return 50.0; // 초미세먼지 PM10 최대값
            case 8: return 20.0; // 온도_1 최대값
            case 9: return 20.0; // 온도_2 최대값
            case 10: return 20.0; // 온도_3 최대값
            default: return 1000.0;
        }
    }


    // Sensor 객체의 value1~value13 중 해당 인덱스 값 반환
    private Double getSensorValue(Sensor sensor, int index) {
        switch (index) {
            case 1: return sensor.getValue1();
            case 2: return sensor.getValue2();
            case 3: return sensor.getValue3();
            case 4: return sensor.getValue4();
            case 5: return sensor.getValue5();
            case 6: return sensor.getValue6();
            case 7: return sensor.getValue7();
            case 8: return sensor.getValue8();
            case 9: return sensor.getValue9();
            case 10: return sensor.getValue10();
            case 11: return sensor.getValue11();
            case 12: return sensor.getValue12();
            case 13: return sensor.getValue13();
            default: return null;
        }
    }



}
