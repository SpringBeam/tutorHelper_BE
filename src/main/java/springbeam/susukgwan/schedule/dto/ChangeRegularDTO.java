package springbeam.susukgwan.schedule.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeRegularDTO {
    private Long tutoringId;
    private String dayTime;
}
