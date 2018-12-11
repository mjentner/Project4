package tomasulogui;

public class ROBEntry {
	ReorderBuffer rob;

	// TODO - add many more fields into entry
	// I deleted most, and only kept those necessary to compile GUI
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

	public void readCDB(CDB cdb) {
		int tag = cdb.getDataTag();
		int cdbValue = cdb.getDataValue();
		if (opcode == IssuedInst.INST_TYPE.BEQ
		 || opcode == IssuedInst.INST_TYPE.BNE
		 || opcode == IssuedInst.INST_TYPE.BGEZ
		 || opcode == IssuedInst.INST_TYPE.BGTZ
		 || opcode == IssuedInst.INST_TYPE.BLEZ
		 || opcode == IssuedInst.INST_TYPE.BLTZ) {
			mispredicted = cdbValue != 0;
		}
		else {
			writeValue = cdbValue;
		}
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
		instPC = inst.getPC();
		opcode = inst.getOpcode();
		predictTaken = inst.getBranchPrediction();
		mispredicted = false;

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

		storeDataTag = reg2Tag;
		storeAddressTag = reg1Tag;
		target = inst.getBranchTgt();
		writeReg = inst.getRegDest();

		complete = opcode == IssuedInst.INST_TYPE.NOP
		        || opcode == IssuedInst.INST_TYPE.HALT
		        || opcode == IssuedInst.INST_TYPE.STORE
		            && haveStoreAddress && haveStoreData;
		inst.setRegDestTag(frontQ);

		// TODO - This is a long and complicated method, probably the most complex
		// of the project.  It does 2 things:
		// 1. update the instruction, as shown in 2nd line of code above
		// 2. update the fields of the ROBEntry, as shown in the 1st line of code above

		}

	}
