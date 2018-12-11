package tomasulogui;

public class IntAlu extends FunctionalUnit{
  public static final int EXEC_CYCLES = 1;

  public IntAlu(PipelineSimulator sim) {
    super(sim);
  }


  public int calculateResult(int station) {
     // just placeholder code
	ReservationStation stat = stations[station];
	IssuedInst.INST_TYPE opcode = stat.getFunction();
	int data1 = stat.getData1();
	int data2 = stat.getData2();
	switch (opcode) {
		case SLL : {
			return data1 << data2;
		}
		default : {
			return data1 + data2;
		}
	}
  }

  public int getExecCycles() {
    return EXEC_CYCLES;
  }
}
