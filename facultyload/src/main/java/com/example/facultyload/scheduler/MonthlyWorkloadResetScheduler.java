package com.example.facultyload.scheduler;

import com.example.facultyload.entity.Faculty;
import com.example.facultyload.entity.MonthlyWorkloadHistory;
import com.example.facultyload.repository.FacultyRepository;
import com.example.facultyload.repository.MonthlyWorkloadHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlyWorkloadResetScheduler {

    private final FacultyRepository facultyRepository;
    private final MonthlyWorkloadHistoryRepository historyRepository;

    @Scheduled(cron = "0 0 0 1 * *", zone = "UTC")
    @Transactional
    public void resetMonthlyAssignedHours() {
        LocalDate thisMonthStart = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1);
        LocalDate prevMonthStart = thisMonthStart.minusMonths(1);

        List<Faculty> all = facultyRepository.findAll();
        for (Faculty f : all) {
            Integer hours = f.getMonthlyAssignedHours() == null ? 0 : f.getMonthlyAssignedHours();
            historyRepository.findByFacultyIdAndMonthStartDate(f.getId(), prevMonthStart)
                    .orElseGet(() -> historyRepository.save(MonthlyWorkloadHistory.builder()
                            .faculty(f)
                            .monthStartDate(prevMonthStart)
                            .monthlyAssignedHours(hours)
                            .build()));
            f.setMonthlyAssignedHours(0);
        }
        facultyRepository.saveAll(all);
    }
}

