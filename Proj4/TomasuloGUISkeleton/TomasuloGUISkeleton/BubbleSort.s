-- Main Function
Begin Assembly
ADDI R1, R0, 4000
ADDI R2, R0, 40
JAL bubbleSort
HALT
--
--
--
-- Bubble Sort Subroutine
--r1: addr of beginning of array
--r2: size of array
--r3: current address of end of the array (i) 
--r4: current beginning of the array (j)
--r5: next value in array after j
--r6: temp value
--r7: value from r4
--r8: value from r5
--r0: hard coded as 0
--
LABEL bubbleSort
--location @ length of array 
ADD R3, R1, R2
ADDI R3, R3, -4
--Load j value
--
LABEL OutLoop
-- Set next and j
ADD R5, R1, R0
ADD R4, R1, R0
--
--Start of inner loop
LABEL InLoop
--end for loop if R4 == R3
--R5++
ADDI R5, R5, 4
--
-- Check if A[j] > A[next]
LW R7, 0(R4)
LW R8, 0(R5)
SUB R6, R7, R8
BLEZ R6, postSwap
--
--Swap function
SW R8, 0(R4)
SW R7, 0(R5)
--
LABEL postSwap
ADD R4, R0, R5
--Jump to inner loop
BNE R4, R3, InLoop
ADDI R3, R3, -4
BNE R3, R1, OutLoop
--
--
LABEL END
JR R31
--
End Assembly
Begin Data 4000 40
0
4
6
8
1
3
5
2
9
7
End Data

