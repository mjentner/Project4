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
  // TODO - maybe more than simple set
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
    inst.setRegDestTag(frontQ);

    // TODO - This is a long and complicated method, probably the most complex
    // of the project.  It does 2 things:
    // 1. update the instruction, as shown in 2nd line of code above
    // 2. update the fields of the ROBEntry, as shown in the 1st line of code above

  }

}
