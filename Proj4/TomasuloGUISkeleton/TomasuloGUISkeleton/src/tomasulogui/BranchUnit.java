package tomasulogui;

public class BranchUnit
        extends FunctionalUnit {

    public static final int EXEC_CYCLES = 1;

    public BranchUnit(PipelineSimulator sim) {
        super(sim);
    }

//    ADD, ADDI, SUB, MUL, DIV, AND, ANDI, OR, ORI, XOR, XORI, SLL, SRL, SRA,
//        LOAD, STORE, HALT,
//        NOP, BEQ, BNE, BLTZ, BLEZ, BGEZ, BGTZ, J, JAL, JR, JALR} ;
    public int calculateResult(int station) {
		ReservationStation stat = stations[station];
		int data1 = stat.getData1();
		int data2 = stat.getData2();
		switch (stat.getFunction()) {
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
			case J : {
				return 1;
			}
			case JAL :
			case JR : {
				return data1;
			}
			case JALR : {
				return data2;
			}
		}
        // todo fill in
        return 0;
    }

    public int getExecCycles() {
        return EXEC_CYCLES;
    }
}
