package tomasulogui;

public class ROBEntry {
	ReorderBuffer rob;

	boolean complete = false;
	boolean predictTaken = false;
	boolean mispredicted = false;
	boolean haveStoreAddress = false;
	boolean haveStoreData = false;
	int storeDataTag = -1;
	int storeAddressTag = -1;
	int target = -1;
	int instPC = -1;
	int writeReg = -1;
	int writeValue = -1;
	int writeAddress = -1;

	IssuedInst.INST_TYPE opcode;

	public ROBEntry(ReorderBuffer buffer) {
		rob = buffer;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean c) {
		complete = c;
	}

	public boolean branchMispredicted() {
		return mispredicted;
	}

	public boolean getPredictTaken() {
		return predictTaken;
	}

	public int getInstPC() {
		return instPC;
	}

	public int getTarget() {
		return target;
	}

	public IssuedInst.INST_TYPE getOpcode () {
		return opcode;
	}

	public void setTarget(int targ) {
		if (targ != target) {
			target = targ;
			mispredicted = true;
		}
	}

	// This method is used to allow stores to update their address and data
	// fields from the cdb
	public void snoopCDB(CDB cdb) {
		if (opcode == IssuedInst.INST_TYPE.STORE) {
			int tag = cdb.getDataTag();
			int value = cdb.getDataValue();
			if (storeAddressTag == tag) {
				writeAddress += value;
				haveStoreAddress = true;
			}
			if (storeDataTag == tag) {
				writeValue = value;
				haveStoreData = true;
			}
			complete = haveStoreAddress && haveStoreData;
		}
	}

	// This method is used to allow the entry referenced by the cdb tag
	// to update its write value and be marked as complete
	public void readCDB(CDB cdb) {
		int tag = cdb.getDataTag();
		int cdbValue = cdb.getDataValue();

		// If true branch, set mispredicted based on comparison
		// of predictTaken and mispredicted
		if (opcode == IssuedInst.INST_TYPE.BEQ
		 || opcode == IssuedInst.INST_TYPE.BNE
		 || opcode == IssuedInst.INST_TYPE.BGEZ
		 || opcode == IssuedInst.INST_TYPE.BGTZ
		 || opcode == IssuedInst.INST_TYPE.BLEZ
		 || opcode == IssuedInst.INST_TYPE.BLTZ) {
			mispredicted = (cdbValue != 0) != predictTaken;
		}
		// Set writeValue based on cdbValue
		else {
			writeValue = cdbValue;
		}
		// Instruction is now complete
		complete = true;
	}

	public boolean isHaltOpcode() {
		return (opcode == IssuedInst.INST_TYPE.HALT);
	}

	public void setBranchTaken(boolean result) {
		complete = true;
		mispredicted = predictTaken == result;
	}

	public int getWriteAddress() {
		return writeAddress;
	}

	public int getWriteReg() {
		return writeReg;
	}

	public int getWriteValue() {
		return writeValue;
	}

	public void setWriteValue(int value) {
		writeValue = value;
	}


	public void copyInstData(IssuedInst inst, int frontQ) {

		// Set some fields that can be copied directly from inst
		instPC = inst.getPC();
		opcode = inst.getOpcode();
		predictTaken = inst.getBranchPrediction();

		// predictions are assumed correct until the branch unit says otherwise
		mispredicted = false;

		// Set fields related to regSrc1
		int reg1 = inst.getRegSrc1();
		int reg1Tag = -1;
		int reg1Val;
		if (reg1 != -1) {
			reg1Tag = rob.getTagForReg(reg1);
			if (reg1Tag != -1) {
				ROBEntry r = rob.getEntryByTag(reg1Tag);
				reg1Val = r.getWriteValue();
				haveStoreAddress = r.isComplete();
			}
			else {
				reg1Val = rob.getDataForReg(reg1);
				haveStoreAddress = true;
			}
			writeAddress = haveStoreAddress ? 
			               inst.getImmediate() + reg1Val :
			               inst.getImmediate();
			inst.setRegSrc1Value(reg1Val);
			inst.setRegSrc1Tag(reg1Tag);
			if (haveStoreAddress) {
				inst.setRegSrc1Valid();
			}
		}

		// Set fields related to regSrc2
		int reg2 = inst.getRegSrc2();
		int reg2Tag = -1;
		if (reg2 != -1) {
			reg2Tag = rob.getTagForReg(reg2);
			if (reg2Tag != -1) {
				ROBEntry r = rob.getEntryByTag(reg2Tag);
				writeValue = r.getWriteValue();
				haveStoreData = r.isComplete();
			}
			else {
				writeValue = rob.getDataForReg(reg2);
				haveStoreData = true;
			}
			inst.setRegSrc2Value(writeValue);
			inst.setRegSrc2Tag(reg2Tag);
			if (haveStoreData) {
				inst.setRegSrc2Valid();
			}
		}

		// If JAL or JALR, set their available src field to pc+4
		if (opcode == IssuedInst.INST_TYPE.JAL) {
			inst.setRegSrc1Valid();
			inst.setRegSrc1Used();
			inst.setRegSrc1Value(inst.getPC() + 4);
		}
		if (opcode == IssuedInst.INST_TYPE.JALR) {
			inst.setRegSrc2Used();
			inst.setRegSrc2Valid();
			inst.setRegSrc2Value(inst.getPC() + 4);
		}

		// Set values used by store and branch instructions
		// these values are ignored by other instructions, so setting them is
		// harmless
		storeDataTag = reg2Tag;
		storeAddressTag = reg1Tag;
		target = inst.getBranchTgt();
		writeReg = inst.getRegDest();

		// Instruction is complete if 
		//  a. It doesn't use a functional unit (NOP, HALT, and STORE) AND
		//  b. It isn't waiting on any registers (storeAddress and storeData)
		complete = opcode == IssuedInst.INST_TYPE.NOP
		        || opcode == IssuedInst.INST_TYPE.HALT
		        || opcode == IssuedInst.INST_TYPE.STORE
		            && haveStoreAddress && haveStoreData;

		// Set destination tag of instruction
		inst.setRegDestTag(frontQ);
	}

}
