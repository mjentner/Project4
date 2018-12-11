package tomasulogui;

public class BranchUnit
        extends FunctionalUnit {

    public static final int EXEC_CYCLES = 1;

    public BranchUnit(PipelineSimulator sim) {
        super(sim);
    }

	@Override
    public int calculateResult(int station) {
		ReservationStation stat = stations[station];
		int data1 = stat.getData1();
		int data2 = stat.getData2();
		switch (stat.getFunction()) {
			// For true branch instructions, return a "boolean" flag
			// indicating whether the branch was taken
			case BEQ : {
				return data1 == data2 ? 1 : 0;
			}
			case BNE : {
				return data1 != data2 ? 1 : 0;
			}
			case BLTZ : {
				return data1 < 0 ? 1 : 0;
			}
			case BLEZ : {
				return data1 <= 0 ? 1 : 0;
			}
			case BGEZ : {
				return data1 >= 0 ? 1 : 0;
			}
			case BGTZ : {
				return data1 > 0 ? 1 : 0;
			}
			// J and JR are treated as always taken branchs. Return "true"
			case J : {
				return 1;
			}
			// JR and JALR need to set the branch target in the reorder buffer
			case JR : {
				simulator.getROB().getEntryByTag(stat.getDestTag())
				                  .setTarget(data1);
				return 1;
			}
			// JALR and JAL return the value to be written to R31
			case JALR : {
				simulator.getROB().getEntryByTag(stat.getDestTag())
				                  .setTarget(data1);
				return data2;
			}
			case JAL : {
				return data1;
			}
		}
        return 0;
    }

	@Override
    public int getExecCycles() {
        return EXEC_CYCLES;
    }
}
