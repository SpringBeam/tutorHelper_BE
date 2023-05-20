package springbeam.susukgwan.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.schedule.dto.ChangeRegularDTO;
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
}
