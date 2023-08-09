package springbeam.susukgwan.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.schedule.dto.ChangeRegularDTO;
import springbeam.susukgwan.schedule.dto.GetScheduleDTO;
import springbeam.susukgwan.schedule.dto.ReplaceScheduleDTO;
import springbeam.susukgwan.schedule.dto.ScheduleDTO;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;

    @PostMapping("")
    public ResponseEntity newIrregularSchedule(@RequestBody ScheduleDTO scheduleDTO) {
        return scheduleService.newIrregularSchedule(scheduleDTO);
    }
    @DeleteMapping("")
    public ResponseEntity cancelSchedule(@RequestBody ScheduleDTO scheduleDTO) {
        return scheduleService.cancelSchedule(scheduleDTO);
    }
    @PutMapping("/regular")
    public ResponseEntity changeSchedule(@RequestBody ChangeRegularDTO changeRegularDTO) {
        return scheduleService.changeRegularSchedule(changeRegularDTO);
    }
    @GetMapping("/list/{tutoringId}/{year}/{month}")
    public ResponseEntity getScheduleList(@PathVariable("tutoringId") Long tutoringId, @PathVariable("year") int year, @PathVariable("month") int month) {
        return scheduleService.getScheduleListYearMonth(tutoringId, year, month);
    }
    @GetMapping("/list/tutorings/{year}/{month}")
    public ResponseEntity getAllScheduleList(@PathVariable("year") int year, @PathVariable("month") int month) {
        return scheduleService.getAllScheduleListYearMonth(year, month);
    }
    @GetMapping("/{year}/{month}/{day}")
    public ResponseEntity getTutoringListByDay(@PathVariable("year") int year, @PathVariable("month") int month, @PathVariable("day") int day) {
        return scheduleService.getAllScheduleListYearMonthDay(year, month, day);
    }
    @PutMapping("")
    public ResponseEntity replaceSchedule(@RequestBody ReplaceScheduleDTO replaceScheduleDTO) {
        return scheduleService.replaceSchedule(replaceScheduleDTO);
    }
}
