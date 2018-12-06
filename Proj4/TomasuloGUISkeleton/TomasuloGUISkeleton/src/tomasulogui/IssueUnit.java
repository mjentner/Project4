package tomasulogui;

public class IssueUnit {
	private enum EXEC_TYPE {
		NONE, LOAD, ALU, MULT, DIV, BRANCH} ;

	PipelineSimulator simulator;
	IssuedInst issuee;
	Object fu;

	public IssueUnit(PipelineSimulator sim) {
		simulator = sim;
	}

	public void execCycle() {
		// an execution cycle involves:
		// 1. checking if ROB and Reservation Station avail
		// 2. issuing to reservation station, if no structural hazard

		// to issue, we make an IssuedInst, filling in what we know
		// We check the BTB, and put prediction if branch, updating PC
		//     if pred taken, incr PC otherwise
		// We then send this to the ROB, which fills in the data fields
		// We then check the CDB, and see if it is broadcasting data we need,
		//    so that we can forward during issue

		// We then send this to the FU, who stores in reservation station

		// Make sure there is a reorder buffer slot
		ReorderBuffer rob = simulator.getROB();
		if (rob.isFull()) {
			return;
		}

		// Get IssuedInst
		int pc = simulator.getPC();
		Instruction inst = simulator.getMemory().getInstAtAddr(pc);
		issuee = IssuedInst.createIssuedInst(inst);
		issuee.setPC(pc);

		// Set fu based on needed functional unit
		IssuedInst.INST_TYPE opcode = issuee.getOpcode();
		fu = null;
		switch (opcode) {
			case ADD:
			case ADDI:
			case SUB:
			case AND:
			case ANDI:
			case OR:
			case ORI:
			case XOR:
			case XORI:
			case SLL:
			case SRL:
			case SRA: {
				fu = simulator.getALU();
				break;
			}
			case MUL: {
				fu = simulator.getMult();
				break;
			}
			case DIV: {
				fu = simulator.getDivider();
				break;
			}
			case BEQ:
			case BNE:
			case BLTZ:
			case BLEZ:
			case BGEZ:
			case BGTZ:
			case J:
			case JAL:
			case JR:
			case JALR: {
				simulator.getBTB().predictBranch(issuee);
				fu = simulator.getBranchUnit();
				break;
			}
			default :
		}

		// Make sure there is a reservation station available
		if (fu != null) {
			if (((FunctionalUnit)fu).full()) {
				return;
			}
		}

		// Send to reorder buffer
		simulator.getROB().updateInstForIssue(issuee);

		// TODO check CDB for forward data
		CDB cdb = simulator.getCDB();
		if (cdb.getDataValid()) {
			int cdbTag = cdb.getDataTag();
			int cdbValue = cdb.getDataValue();
			if (!issuee.getRegSrc1Valid() && issuee.getRegSrc1Tag() == cdbTag) {
				issuee.setRegSrc1Valid();
				issuee.setRegSrc1Value(cdbValue);
			}
			if (!issuee.getRegSrc2Valid() && issuee.getRegSrc2Tag() == cdbTag) {
				issuee.setRegSrc2Valid();
				issuee.setRegSrc2Value(cdbValue);
			}
		}
		
		
		// Issue instruction
		if (fu != null) {
			((FunctionalUnit)fu).acceptIssue(issuee);
		}

	}

}
