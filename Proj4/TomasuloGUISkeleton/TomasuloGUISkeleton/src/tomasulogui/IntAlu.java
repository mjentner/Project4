package tomasulogui;

public class IntAlu extends FunctionalUnit{
  public static final int EXEC_CYCLES = 1;

  public IntAlu(PipelineSimulator sim) {
    super(sim);
  }

  @Override
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
                case SRL : {
                    return data1 >>> data2;
                }
                case SRA : {
                    return data1 >> data2;
                }
                case ADDI : {
			return data1 + data2;
		}
                case SUB : {
                    return data1 - data2;
                }
                
                //falls through to both
                case ANDI :
                case AND : {
                    return data1 & data2;
                }
                //falls through to both
                case ORI :
                case OR : {
                    return data1 | data2;
                }
                
                case XORI :
                case XOR : {
                    return data1 ^ data2;
                }
                
                
                
                //default is ADD
                default: {
                    return data1 + data2;
                }
	}
  }

  @Override
  public int getExecCycles() {
    return EXEC_CYCLES;
  }
}
