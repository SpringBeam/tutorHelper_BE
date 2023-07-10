package springbeam.susukgwan.schedule.dto;

import lombok.*;
import springbeam.susukgwan.tutoring.dto.DayTimeDTO;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeRegularDTO {
    private Long tutoringId;
    private List<DayTimeDTO> dayTimeList;
}
