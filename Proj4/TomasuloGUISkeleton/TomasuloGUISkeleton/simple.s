-- If things work correctly, R29 should hold 8000 at termination
Begin Assembly
ADDI R30, R0, 5000
ADDI R29, R30, 3000
HALT
End Assembly
Begin Data 4000 44
End Data
-- stack
Begin Data 5000 100
End Data
