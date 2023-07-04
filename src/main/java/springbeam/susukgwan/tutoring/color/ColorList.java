package springbeam.susukgwan.tutoring.color;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ColorList {
    c0(0,"default"),
    c1(1,""),
    c2(2,""),
    c3(3,""),
    c4(4,""),
    c5(5,""),
    c6(6,""),
    c7(7,""),
    c8(8,""),
    c9(9,""),
    c10(10, "");
    private final int value;
    private final String Color;
}
