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
  int storeDataReg = -1;
  int storeAddressReg = -1;
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

  public void readCDB(CDB cdb) {
    if (opcode == IssuedInst.INST_TYPE.STORE) {
        int tag = cdb.getDataTag();
        int value = cdb.getDataValue();
        if (storeAddressReg == tag) {
            writeAddress += value;
            haveStoreAddress = true;
        }
        if (storeDataReg == tag) {
            writeValue = value;
            haveStoreData = true;
        }
        complete = haveStoreAddress && haveStoreData;
    }
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
    complete = opcode == IssuedInst.INST_TYPE.NOP
            || opcode == IssuedInst.INST_TYPE.HALT;
    predictTaken = inst.getBranchPrediction();
    mispredicted = false;
    
    int reg1 = inst.getRegSrc1();
	int reg1Tag;
	int reg1Val;
	boolean reg1Valid = reg1 != -1;
	if (reg1Valid) {
		reg1Tag = rob.getTagForReg(reg1);
		reg1Val = rob.getDataForReg(reg1);
		haveStoreAddress = reg1Tag == -1;
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
	int reg2Tag;
	int reg2Val;
	boolean reg2Valid = reg2 != -1;
	if (reg2Valid) {
		reg2Tag = rob.getTagForReg(reg2);
		reg2Val = rob.getDataForReg(reg2);
		haveStoreData = reg2Tag == -1;
		writeValue = reg2Val;
		inst.setRegSrc2Value(reg2Val);
		inst.setRegSrc2Tag(reg2Tag);
	}
    
    storeDataReg = reg2;
    storeAddressReg = reg1;
    target = inst.getBranchTgt();
    writeReg = inst.getRegDest();

    inst.setRegDestTag(frontQ);
    if (haveStoreAddress) {
        inst.setRegSrc1Valid();
    }
    if (haveStoreData) {
        inst.setRegSrc2Valid();
    }

    // TODO - This is a long and complicated method, probably the most complex
    // of the project.  It does 2 things:
    // 1. update the instruction, as shown in 2nd line of code above
    // 2. update the fields of the ROBEntry, as shown in the 1st line of code above

  }

}
