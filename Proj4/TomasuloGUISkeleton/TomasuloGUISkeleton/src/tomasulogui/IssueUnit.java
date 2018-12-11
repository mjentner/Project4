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
			// Branches need some special handling. A branch flag needs set.
			// JAL and JALR need to be set to write to R31
			case JAL:
			case JALR: {
				issuee.setRegDest(31);
				issuee.setRegDestUsed();
			}
			case BEQ:
			case BNE:
			case BLTZ:
			case BLEZ:
			case BGEZ:
			case BGTZ:
			case J:
			case JR: {
				issuee.setBranch();
				fu = simulator.getBranchUnit();
				break;
			}
			case LOAD: {
				fu = simulator.getLoader();
			}
			default :
		}

		// Make sure a reservation station is available
		if (fu instanceof FunctionalUnit &&
		    !((FunctionalUnit)fu).isReservationStationAvail() ||
		    fu instanceof LoadBuffer &&
		    !((LoadBuffer)fu).isReservationStationAvail()) {
			return;
		}

		// Set program counter. This is done with the BTB for branches
		if (issuee.isBranch()) {
			simulator.getBTB().predictBranch(issuee);
		}
		else {
			simulator.setPC(pc + 4);
		}

		// Send to reorder buffer
		simulator.getROB().updateInstForIssue(issuee);

		// check CDB for forward data
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
			if (fu instanceof FunctionalUnit) {
				((FunctionalUnit)fu).acceptIssue(issuee);
			}
			else if (fu instanceof LoadBuffer) {
				((LoadBuffer)fu).acceptIssue(issuee);
			}
		}
	}

}
