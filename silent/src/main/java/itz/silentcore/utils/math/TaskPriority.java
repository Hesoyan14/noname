package itz.silentcore.utils.math;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskPriority {
    CRITICAL_FOR_USER_PROTECTION(60),
    CRUCIAL_FOR_PLAYER_LIFE(40),
    HIGH_IMPORTANCE_3(35),
    HIGH_IMPORTANCE_2(30),
    HIGH_IMPORTANCE_1(20),
    STANDARD(0),
    LOW_PRIORITY(-20);

    private final int priority;
}
