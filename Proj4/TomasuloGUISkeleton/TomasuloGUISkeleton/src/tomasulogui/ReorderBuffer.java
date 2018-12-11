package tomasulogui;

public class ReorderBuffer {
  public static final int size = 30;
  int frontQ = 0;
  int rearQ = 0;
  ROBEntry[] buff = new ROBEntry[size];
  int numRetirees = 0;

  PipelineSimulator simulator;
  RegisterFile regs;
  boolean halted = false;

  public ReorderBuffer(PipelineSimulator sim, RegisterFile registers) {
    simulator = sim;
    regs = registers;
  }

  public ROBEntry getEntryByTag(int tag) {
    return buff[tag];
  }

  public int getInstPC(int tag) {
    return buff[tag].getInstPC();
  }

  public boolean isHalted() {
    return halted;
  }

  public boolean isFull() {
    return (frontQ == rearQ && buff[frontQ] != null);
  }

  public int getNumRetirees() {
    return numRetirees;
  }

  public boolean retireInst() {
    // 3 cases
    // 1. regular reg dest inst
    // 2. isBranch w/ mispredict
    // 3. isStore
    ROBEntry retiree = buff[frontQ];

    if (retiree == null) {
      return false;
    }

    if (retiree.isHaltOpcode()) {
      halted = true;
      return true;
    }

    boolean shouldAdvance = true;

	IssuedInst.INST_TYPE opcode = retiree.getOpcode();
	int pc = retiree.getInstPC();
	int target = retiree.getTarget();
	boolean mispredict = retiree.branchMispredicted();
	boolean predictTaken = retiree.getPredictTaken();

	// If it's a branch update the BTB's information
	// Note that the BTB always predicts taken for JR and JALR
	// therefore, feeding it with our version of mispredict for those to inst's
	// doesn't mess anything up
	if (opcode == IssuedInst.INST_TYPE.BEQ
	 || opcode == IssuedInst.INST_TYPE.BNE
	 || opcode == IssuedInst.INST_TYPE.BGTZ
	 || opcode == IssuedInst.INST_TYPE.BLTZ
	 || opcode == IssuedInst.INST_TYPE.BGEZ
	 || opcode == IssuedInst.INST_TYPE.BLEZ
	 || opcode == IssuedInst.INST_TYPE.J
	 || opcode == IssuedInst.INST_TYPE.JR
	 || opcode == IssuedInst.INST_TYPE.JAL
	 || opcode == IssuedInst.INST_TYPE.JALR) {
		BranchPredictor btb = simulator.getBTB();
		btb.setBranchAddress(pc, target);
		btb.setBranchResult(pc, mispredict ^ predictTaken);
	}

	int writeReg = retiree.getWriteReg();
    if (!retiree.isComplete()) {
        shouldAdvance = false;
    }
	// On a mispredict, squash everything, update the program counter,
	// and flush the reorder buffer
    else if (mispredict) {
        shouldAdvance = false;
        frontQ = 0;
        rearQ = 0;
        simulator.squashAllInsts();
        simulator.pc.setPC(retiree.predictTaken
		  				   && opcode != IssuedInst.INST_TYPE.JR
		                   && opcode != IssuedInst.INST_TYPE.JALR
		                   ?  pc + 4
		  	               : target);
		for (int i = 0; i < size; i++) {
			buff[i] = null;
		}
    }
	// For stores and writeback instructions write to register or memory
    else if (retiree.getOpcode() == IssuedInst.INST_TYPE.STORE) {
        simulator.memory.setIntDataAtAddr(retiree.getWriteAddress(),
                                          retiree.getWriteValue());
    }
    else if (writeReg != -1 && regs.getSlotForReg(writeReg) == frontQ) {
        regs.setReg(writeReg, retiree.getWriteValue());
        setTagForReg(writeReg, -1);
    }
      // if mispredict branch, won't do normal advance
      if (shouldAdvance) {
        numRetirees++;
        buff[frontQ] = null;
        frontQ = (frontQ + 1) % size;
      }

    return false;
  }

  public void readCDB(CDB cdb) {
    // check entire CDB for someone waiting on this data
    // could be destination reg
    // could be store address source
    
    if (cdb.getDataValid()) {
        for (int i = frontQ; i != rearQ; i = (i+1) % size) {
            buff[i].snoopCDB(cdb);
        }
		ROBEntry entry = buff[cdb.getDataTag()];
		if (entry != null) {
			entry.readCDB(cdb);
		}
    }
  }

  public void updateInstForIssue(IssuedInst inst) {
    // the task is to simply annotate the register fields
    // the dest reg will be assigned a tag, which is just our slot#
    // all src regs will either be assigned a tag, read from reg, or forwarded from ROB

    // TODO - possibly nothing if you use my model
    // I use the call to copyInstData below to do 2 things:
    // 1. update the Issued Inst
    // 2. fill in the ROB entry

    // first get a ROB slot
    if (buff[rearQ] != null) {
      throw new MIPSException("updateInstForIssue: no ROB slot avail");
    }
    ROBEntry newEntry = new ROBEntry(this);
    buff[rearQ] = newEntry;
    newEntry.copyInstData(inst, rearQ);
	if (inst.getRegDestUsed()) {
		regs.setSlotForReg(inst.getRegDest(), rearQ);
	}

    rearQ = (rearQ + 1) % size;
  }

  public int getTagForReg(int regNum) {
    return (regs.getSlotForReg(regNum));
  }

  public int getDataForReg(int regNum) {
    return (regs.getReg(regNum));
  }

  public void setTagForReg(int regNum, int tag) {
    regs.setSlotForReg(regNum, tag);
  }

}
